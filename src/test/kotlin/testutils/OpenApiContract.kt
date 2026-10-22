package git.walhay.modweave.testutils

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.JsonValidationReportFormat
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.report.ValidationReport
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.net.URLDecoder
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.BeanUtils
import org.springframework.web.util.UriComponentsBuilder

/** Adapts the JDK HTTP client to the standard OpenAPI validator. */
class OpenApiContract {
  private val api =
      OpenAPIV3Parser()
          .readLocation(
              requireNotNull(javaClass.getResource("/static/api/v2/openapi.yaml")).toExternalForm(),
              null,
              ParseOptions().apply {
                isResolve = true
                isResolveFully = true
                isResolveCombinators = false
                isExplicitObjectSchema = false
              })
          .let { result -> requireNotNull(result.openAPI) { result.messages.joinToString("\n") } }
          .apply {
            // Validator 3.0.0 recognizes array parameters by Java class. The 3.1 parser
            // produces JsonSchema instead; retain every schema keyword while adapting its class.
            paths.values.forEach { item ->
              (item.parameters.orEmpty() +
                      item.readOperations().flatMap { it.parameters.orEmpty() })
                  .forEach { parameter ->
                    if (parameter.schema?.types == setOf("array")) {
                      parameter.schema =
                          ArraySchema().also { BeanUtils.copyProperties(parameter.schema, it) }
                    }
                  }
            }
          }

  val validator: OpenApiInteractionValidator =
      OpenApiInteractionValidator.createFor(api)
          .withStrictOperationPathMatching()
          // Respect OpenAPI's default: extra properties are allowed unless explicitly forbidden.
          // This disables schema injection, not validation of declared additionalProperties.
          .withLevelResolver(
              LevelResolver.create()
                  .withLevel(
                      "validation.schema.additionalProperties", ValidationReport.Level.IGNORE)
                  .build())
          .build()

  val operations: Map<String, Endpoint> =
      api.paths
          .flatMap { (path, item) ->
            item.readOperationsMap().map { (method, operation) ->
              operation.operationId to Endpoint(path, method.name, operation)
            }
          }
          .toMap()

  fun validate(request: HttpRequest, body: ByteArray?, response: HttpResponse<ByteArray>) {
    val adaptedRequest = SimpleRequest.Builder(request.method(), request.uri().rawPath, true)
    body?.let { adaptedRequest.withBody(it) }
    request.headers().map().forEach { (name, values) -> adaptedRequest.withHeader(name, values) }
    UriComponentsBuilder.fromUri(request.uri()).build().queryParams.forEach { (name, values) ->
      adaptedRequest.withQueryParam(
          URLDecoder.decode(name, Charsets.UTF_8),
          values.map { URLDecoder.decode(it, Charsets.UTF_8) })
    }
    val adaptedResponse = SimpleResponse.Builder(response.statusCode()).withBody(response.body())
    response.headers().map().forEach { (name, values) -> adaptedResponse.withHeader(name, values) }
    val requestModel = adaptedRequest.build()
    val responseModel = adaptedResponse.build()
    val report =
        if (response.statusCode() < 400) {
          validator.validate(requestModel, responseModel)
        } else {
          // Negative scenarios intentionally send invalid or unauthenticated requests.
          validator.validateResponse(requestModel.path, requestModel.method, responseModel)
        }
    assertValid(report)
  }

  fun assertValid(report: ValidationReport) {
    assertFalse(report.hasErrors()) { JsonValidationReportFormat.getInstance().apply(report) }
  }

  data class Endpoint(val path: String, val method: String, val operation: Operation)
}
