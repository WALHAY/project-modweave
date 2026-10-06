"""Validate the contract/examples; optionally exercise a Prism mock without the backend."""

import argparse
import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

import yaml
from jsonschema import Draft202012Validator, FormatChecker
from openapi_spec_validator import validate


def resolve(document, value):
    while isinstance(value, dict) and "$ref" in value:
        reference = value["$ref"]
        assert reference.startswith("#/"), "Only local references are used by this contract"
        value = document
        for part in reference[2:].split("/"):
            value = value[part.replace("~1", "/").replace("~0", "~")]
    return value


def validate_value(document, schema, value):
    Draft202012Validator(
        {"components": document["components"], **schema}, format_checker=FormatChecker()
    ).validate(value)


def check_examples(document):
    count = 0

    def visit(node):
        nonlocal count
        if isinstance(node, dict):
            if "schema" in node:
                values = ([node["example"]] if "example" in node else []) + [
                    resolve(document, example)["value"]
                    for example in node.get("examples", {}).values()
                ]
                for value in values:
                    validate_value(document, node["schema"], value)
                    count += 1
            elif isinstance(node.get("examples"), list):
                for value in node["examples"]:
                    validate_value(document, node, value)
                    count += 1
            for value in node.values():
                visit(value)
        elif isinstance(node, list):
            for value in node:
                visit(value)

    visit(document)
    return count


def check_mock(document, base_url):
    """Validate a multi-step category scenario. Prism examples are stateless."""
    operations = {
        op["operationId"]: (path, method, op)
        for path, methods in document["paths"].items()
        for method, op in methods.items()
    }

    def call(operation_id, expected, data=None, token=None, category=None):
        path, method, operation = operations[operation_id]
        if category:
            from urllib.parse import quote

            path = path.replace("{categoryName}", quote(category, safe=""))
        headers = {}
        payload = None
        if token:
            headers["Authorization"] = "Bearer " + token
        if data is not None:
            media = next(iter(operation["requestBody"]["content"]))
            headers["Content-Type"] = media
            payload = (json.dumps(data) if media == "application/json" else urlencode(data)).encode()
        request = Request(base_url.rstrip("/") + path, data=payload, headers=headers, method=method.upper())
        try:
            result = urlopen(request, timeout=10)
        except HTTPError as error:
            result = error
        with result:
            raw = result.read()
            assert result.status == expected, (operation_id, result.status, raw.decode())
            response = resolve(document, operation["responses"][str(expected)])
            for name, header in response.get("headers", {}).items():
                if header.get("required"):
                    assert result.headers.get(name), (operation_id, "Missing header", name)
            if "content" in response:
                media = result.headers.get_content_type()
                assert media in response["content"], (operation_id, media)
                value = json.loads(raw)
                validate_value(document, response["content"][media]["schema"], value)
                print(f"{operation_id}: {result.status}, response matches schema")
                return value
            assert not raw if expected == 204 else True
            print(f"{operation_id}: {result.status}")

    call("registerUser", 201, document["components"]["schemas"]["UserCreateRequest"]["examples"][0])
    tokens = call("login", 200, {"username": "author", "password": "demo-password"})
    category = call("createCategory", 201, {"name": "Utility", "description": "Quality-of-life mods"}, tokens["accessToken"])
    call("getCategory", 200, category=category["name"])
    call("updateCategory", 200, {"description": "Updated description"}, tokens["accessToken"], category["name"])
    call("deleteCategory", 204, token=tokens["accessToken"], category=category["name"])
    print("Mock scenario passed; persistence and administrator permissions belong to live API tests.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mock-url", help="Prism URL, e.g. http://127.0.0.1:4010")
    args = parser.parse_args()
    document = yaml.safe_load(Path(__file__).with_name("openapi.yaml").read_text())
    validate(document)
    ids = []
    for path, methods in document["paths"].items():
        assert path.startswith("/api/v2/"), path
        for operation in methods.values():
            ids.append(operation["operationId"])
    assert len(ids) == len(set(ids)), "Duplicate operationId"
    print(f"OpenAPI valid: {len(ids)} operations, {check_examples(document)} valid examples")
    if args.mock_url:
        check_mock(document, args.mock_url)


if __name__ == "__main__":
    main()
