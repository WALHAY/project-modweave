import argparse
import atexit
import socket
import subprocess
import time
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.patches import Patch
from matplotlib.ticker import FuncFormatter
import pandas as pd
import psycopg2

ROOT = Path(__file__).resolve().parents[1]
INIT_SQL = ROOT / "src" / "main" / "resources" / "init.sql"

DB_USER = "postgres"
DB_PASSWORD = "postgres"
DB_NAME = "postgres"
CONTAINER_IMAGE = "postgres:16"
ROW_COUNT = 1_000_000

INDEXES = {
    "btree": """
        CREATE INDEX idx_btree_mod_id
        ON modweave.comments(mod_id);
    """,
    "hash": """
        CREATE INDEX idx_hash_mod_id
        ON modweave.comments USING HASH(mod_id);
    """,
    "gin": """
        CREATE INDEX idx_gin_description
        ON modweave.mods USING GIN(
            to_tsvector('english', description)
        );
    """,
    "gist": """
        CREATE INDEX idx_gist_description
        ON modweave.mods USING GIST(
            to_tsvector('english', description)
        );
    """,
    "brin": """
        CREATE INDEX idx_brin_upload_date
        ON modweave.mod_versions USING BRIN(upload_date);
    """,
}

DROP_INDEXES = [
    "DROP INDEX IF EXISTS modweave.idx_btree_mod_id;",
    "DROP INDEX IF EXISTS modweave.idx_hash_mod_id;",
    "DROP INDEX IF EXISTS modweave.idx_gin_description;",
    "DROP INDEX IF EXISTS modweave.idx_gist_description;",
    "DROP INDEX IF EXISTS modweave.idx_brin_upload_date;",
]

QUERIES = {
    "equality_search": """
        SELECT *
        FROM modweave.comments
        WHERE mod_id = 'mod_100';
    """,
    "fulltext_search": """
        SELECT *
        FROM modweave.mods
        WHERE to_tsvector('english', coalesce(description, ''))
        @@ to_tsquery('english', 'graphics');
    """,
    "range_search": """
        SELECT *
        FROM modweave.mod_versions
        WHERE upload_date BETWEEN
        '2024-01-01' AND '2024-12-31';
    """,
}

container_id = None
db_port = None

PLOT_COLORS = ["#4C78A8", "#F58518", "#54A24B", "#E45756", "#72B7B2"]
LINE_MARKERS = ["o", "s", "^", "D", "X", "P", "v", "<", ">", "*"]
BAR_HATCHES = ["", "//", "\\\\", "xx", "..", "++", "--", "**", "oo", "||"]


def configure_plotting():
    plt.style.use("seaborn-v0_8-whitegrid")
    plt.rcParams.update(
        {
            "figure.figsize": (10, 6),
            "axes.titlesize": 14,
            "axes.labelsize": 12,
            "legend.fontsize": 10,
            "xtick.labelsize": 10,
            "ytick.labelsize": 10,
            "axes.titlepad": 12,
        }
    )


def format_bytes(value, _pos=None):
    if value >= 1_000_000_000:
        return f"{value / 1_000_000_000:.1f} GB"
    if value >= 1_000_000:
        return f"{value / 1_000_000:.1f} MB"
    if value >= 1_000:
        return f"{value / 1_000:.1f} KB"
    return f"{value:.0f} B"


def free_port():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def run(command):
    subprocess.run(command, check=True)


def start_container():
    global container_id, db_port

    db_port = free_port()
    result = subprocess.run(
        [
            "docker",
            "run",
            "-d",
            "--rm",
            "--name",
            f"modweave-research-{int(time.time())}",
            "-e",
            f"POSTGRES_USER={DB_USER}",
            "-e",
            f"POSTGRES_PASSWORD={DB_PASSWORD}",
            "-e",
            f"POSTGRES_DB={DB_NAME}",
            "-p",
            f"{db_port}:5432",
            CONTAINER_IMAGE,
        ],
        check=True,
        capture_output=True,
        text=True,
    )
    container_id = result.stdout.strip()
    atexit.register(stop_container)


def stop_container():
    if not container_id:
        return

    subprocess.run(["docker", "rm", "-f", container_id], check=False, capture_output=True)


def wait_for_database():
    connection = None
    deadline = time.time() + 60
    while time.time() < deadline:
        try:
            connection = psycopg2.connect(
                dbname=DB_NAME,
                user=DB_USER,
                password=DB_PASSWORD,
                host="127.0.0.1",
                port=db_port,
            )
            connection.close()
            return
        except psycopg2.OperationalError:
            time.sleep(1)
    raise RuntimeError("PostgreSQL container did not become ready in time")


def load_schema():
    connection = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host="127.0.0.1",
        port=db_port,
    )
    try:
        with connection, connection.cursor() as cursor:
            cursor.execute(INIT_SQL.read_text())
    finally:
        connection.close()


def seed_database():
    connection = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host="127.0.0.1",
        port=db_port,
    )
    try:
        with connection, connection.cursor() as cursor:
            cursor.execute(
                f"""
                INSERT INTO modweave.users (username, name, email, password, is_admin)
                SELECT
                    'user_' || gs,
                    'User ' || gs,
                    'user_' || gs || '@example.com',
                    'password-' || gs,
                    (gs % 10 = 0)
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.games (id, name, description, image_path)
                SELECT
                    'game_' || gs,
                    'Game ' || gs,
                    'Game description ' || gs,
                    '/images/games/' || gs || '.png'
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.categories (name, description)
                SELECT
                    'category_' || gs,
                    'Category description ' || gs
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.mods (id, name, description, image_path, game_id, publisher_id)
                SELECT
                    'mod_' || gs,
                    'Mod ' || gs,
                    'Mod description ' || gs,
                    '/images/mods/' || gs || '.png',
                    'game_' || (((gs - 1) % {ROW_COUNT}) + 1),
                    'user_' || (((gs - 1) % {ROW_COUNT}) + 1)
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.mod_versions (id, name, changes, status, mod_id)
                SELECT
                    (
                        substr(md5(gs::text), 1, 8) || '-' ||
                        substr(md5(gs::text), 9, 4) || '-' ||
                        substr(md5(gs::text), 13, 4) || '-' ||
                        substr(md5(gs::text), 17, 4) || '-' ||
                        substr(md5(gs::text), 21, 12)
                    )::uuid,
                    'Version ' || gs,
                    'Changes ' || gs,
                    CASE
                        WHEN gs % 3 = 0 THEN 'APPROVED'
                        WHEN gs % 3 = 1 THEN 'PENDING'
                        ELSE 'REJECTED'
                    END::modweave.version_status,
                    'mod_' || (((gs - 1) % {ROW_COUNT}) + 1)
                FROM generate_series(1, {ROW_COUNT}) AS gs;
                INSERT INTO modweave.mod_files (filename, file_path, downloads, mod_version_id)
                SELECT
                    'file_' || gs || '.zip',
                    '/files/' || gs || '.zip',
                    gs % 1000,
                    (
                        substr(md5(gs::text), 1, 8) || '-' ||
                        substr(md5(gs::text), 9, 4) || '-' ||
                        substr(md5(gs::text), 13, 4) || '-' ||
                        substr(md5(gs::text), 17, 4) || '-' ||
                        substr(md5(gs::text), 21, 12)
                    )::uuid
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.comments (content, user_id, mod_id)
                SELECT
                    'Comment ' || gs,
                    'user_' || (((gs - 1) % {ROW_COUNT}) + 1),
                    'mod_' || (((gs - 1) % {ROW_COUNT}) + 1)
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.collections (name, description, owner)
                SELECT
                    'Collection ' || gs,
                    'Collection description ' || gs,
                    'user_' || (((gs - 1) % {ROW_COUNT}) + 1)
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.mods_categories (mod_id, category_name)
                SELECT
                    'mod_' || (((gs - 1) % {ROW_COUNT}) + 1),
                    'category_' || (((gs - 1) % {ROW_COUNT}) + 1)
                FROM generate_series(1, {ROW_COUNT}) AS gs;

                INSERT INTO modweave.collections_mods (collection_id, order_index, mod_id)
                SELECT
                    gs,
                    1,
                    'mod_' || gs
                FROM generate_series(1, {ROW_COUNT}) AS gs;
                """
            )
    finally:
        connection.close()


def clear_data():
    connection = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host="127.0.0.1",
        port=db_port,
    )
    try:
        with connection, connection.cursor() as cursor:
            cursor.execute("DROP SCHEMA IF EXISTS modweave CASCADE;")
    finally:
        connection.close()


def measure_query(cursor, query, repeats=10):
    times = []
    for _ in range(repeats):
        start = time.perf_counter_ns()
        cursor.execute(query)
        cursor.fetchall()
        times.append(time.perf_counter_ns() - start)
    return sum(times) / len(times)


def get_index_size(cursor, index_name):
    cursor.execute(
        """
        SELECT pg_relation_size(%s);
        """,
        (index_name,),
    )
    return cursor.fetchone()[0]


def drop_indexes(cursor):
    for query in DROP_INDEXES:
        cursor.execute(query)


def prepare_insert_benchmark(cursor, index_type):
    table_name = f"modweave.bench_{index_type}"
    cursor.execute(f"DROP TABLE IF EXISTS {table_name};")
    if index_type in ("btree", "hash", "no_index"):
        cursor.execute(
            f"""
            CREATE UNLOGGED TABLE {table_name} (
                content text NOT NULL,
                user_id varchar NOT NULL,
                mod_id varchar NOT NULL
            );
            """
        )
        if index_type == "no_index":
            index_sql = None
        else:
            index_sql = (
                f"CREATE INDEX bench_{index_type}_mod_id ON {table_name} USING HASH (mod_id);"
                if index_type == "hash"
                else f"CREATE INDEX bench_{index_type}_mod_id ON {table_name} (mod_id);"
            )
    elif index_type in ("gin", "gist"):
        cursor.execute(
            f"""
            CREATE UNLOGGED TABLE {table_name} (
                id varchar PRIMARY KEY,
                description text
            );
            """
        )
        index_sql = (
            f"CREATE INDEX bench_{index_type}_description ON {table_name} USING GIN (to_tsvector('english', description));"
            if index_type == "gin"
            else f"CREATE INDEX bench_{index_type}_description ON {table_name} USING GIST (to_tsvector('english', description));"
        )
    else:
        cursor.execute(
            f"""
            CREATE UNLOGGED TABLE {table_name} (
                id uuid PRIMARY KEY,
                upload_date timestamp NOT NULL
            );
            """
        )
        index_sql = f"CREATE INDEX bench_{index_type}_upload_date ON {table_name} USING BRIN (upload_date);"

    if index_sql:
        cursor.execute(index_sql)
    return table_name


def measure_insert_benchmark(conn, cursor, index_type, rows, batch_id):
    table_name = prepare_insert_benchmark(cursor, index_type)
    conn.commit()

    if index_type in ("btree", "hash", "no_index"):
        batch_sql = f"""
            INSERT INTO {table_name} (content, user_id, mod_id)
            SELECT
                %s || '_' || gs,
                'user_' || (mod(gs - 1, %s) + 1),
                'mod_' || (mod(gs - 1, %s) + 1)
            FROM generate_series(1, %s) AS gs;
        """
        batch_params = (batch_id, ROW_COUNT, ROW_COUNT, rows)
    elif index_type in ("gin", "gist"):
        batch_sql = f"""
            INSERT INTO {table_name} (id, description)
            SELECT
                %s || '_' || gs,
                'graphics texture pack ' || gs
            FROM generate_series(1, %s) AS gs;
        """
        batch_params = (batch_id, rows)
    else:
        batch_sql = f"""
            INSERT INTO {table_name} (id, upload_date)
            SELECT
                (
                    substr(md5(gs::text), 1, 8) || '-' ||
                    substr(md5(gs::text), 9, 4) || '-' ||
                    substr(md5(gs::text), 13, 4) || '-' ||
                    substr(md5(gs::text), 17, 4) || '-' ||
                    substr(md5(gs::text), 21, 12)
                )::uuid,
                timestamp '2024-01-01' + (gs || ' days')::interval
            FROM generate_series(1, %s) AS gs;
        """
        batch_params = (rows,)

    start = time.perf_counter_ns()
    cursor.execute(batch_sql, batch_params)
    conn.commit()
    batch_elapsed = time.perf_counter_ns() - start

    cursor.execute(f"DROP TABLE IF EXISTS {table_name};")
    conn.commit()

    return batch_elapsed


def build_insert_sizes(max_rows, step_rows):
    step = max(1, step_rows)
    max_value = max(1, max_rows)
    return list(range(step, max_value + 1, step))


def run_research(insert_sizes):
    conn = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host="127.0.0.1",
        port=db_port,
    )
    cursor = conn.cursor()

    results = []
    print("Исследование без индексов")
    drop_indexes(cursor)
    conn.commit()
    for query_name, query in QUERIES.items():
        avg_time = measure_query(cursor, query)
        results.append(
            {
                "index_type": "no_index",
                "query": query_name,
                "time": avg_time,
                "size_bytes": 0,
            }
        )
    for rows in insert_sizes:
        batch_id = f"bench_no_index_{rows}_{time.time_ns()}"
        insert_time = measure_insert_benchmark(
            conn,
            cursor,
            "no_index",
            rows,
            batch_id,
        )
        results.append(
            {
                "index_type": "no_index",
                "query": "insert_benchmark",
                "time": insert_time,
                "size_bytes": 0,
                "rows": rows,
            }
        )
    for index_type, create_query in INDEXES.items():
        print(f"Исследование индекса: {index_type}")
        drop_indexes(cursor)
        conn.commit()

        cursor.execute(create_query)
        conn.commit()

        index_name = f"modweave.{create_query.split()[2]}"
        try:
            size = get_index_size(cursor, index_name)
        except Exception:
            size = 0

        for query_name, query in QUERIES.items():
            avg_time = measure_query(cursor, query)
            results.append(
                {
                    "index_type": index_type,
                    "query": query_name,
                    "time": avg_time,
                    "size_bytes": size,
                }
            )

        for rows in insert_sizes:
            batch_id = f"bench_{index_type}_{rows}_{time.time_ns()}"
            insert_time = measure_insert_benchmark(
                conn,
                cursor,
                index_type,
                rows,
                batch_id,
            )
            results.append(
                {
                    "index_type": index_type,
                    "query": "insert_benchmark",
                    "time": insert_time,
                    "size_bytes": size,
                    "rows": rows,
                }
            )

    cursor.close()
    conn.close()

    return pd.DataFrame(results)


def build_time_graph(df, output_path):
    query_df = df[df["query"].isin(QUERIES.keys())]
    pivot = query_df.pivot(index="index_type", columns="query", values="time")
    ax = pivot.plot(kind="bar", color=PLOT_COLORS[: len(pivot.columns)], width=0.85)
    for container, hatch in zip(ax.containers, BAR_HATCHES):
        for patch in container.patches:
            patch.set_hatch(hatch)
            patch.set_edgecolor("#2f2f2f")
            patch.set_linewidth(0.6)
    ax.set_ylabel("Время (наносекунды)")
    ax.set_xlabel("Тип индекса")
    ax.set_title("Время выполнения запросов")
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)
    ax.legend(title="Запрос", frameon=True, loc="best")
    plt.tight_layout()
    plt.savefig(output_path, format="svg")
    plt.close()


def build_time_graph_no_fulltext(df, output_path):
    filtered = df[df["query"].isin(QUERIES.keys()) & (df["query"] != "fulltext_search")]
    pivot = filtered.pivot(index="index_type", columns="query", values="time")
    ax = pivot.plot(kind="bar", color=PLOT_COLORS[: len(pivot.columns)], width=0.85)
    for container, hatch in zip(ax.containers, BAR_HATCHES):
        for patch in container.patches:
            patch.set_hatch(hatch)
            patch.set_edgecolor("#2f2f2f")
            patch.set_linewidth(0.6)
    ax.set_ylabel("Время (наносекунды)")
    ax.set_xlabel("Тип индекса")
    ax.set_title("Время выполнения запросов")
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)
    ax.legend(title="Запрос", frameon=True, loc="best")
    plt.tight_layout()
    plt.savefig(output_path, format="svg")
    plt.close()


def build_size_graph(df, output_path):
    grouped = df.groupby("index_type", as_index=False)["size_bytes"].max()
    grouped = grouped.sort_values("index_type")

    fig, ax = plt.subplots()
    bar_positions = range(len(grouped))
    colors = [PLOT_COLORS[i % len(PLOT_COLORS)] for i in bar_positions]
    hatches = [BAR_HATCHES[i % len(BAR_HATCHES)] for i in bar_positions]

    bars = ax.bar(bar_positions, grouped["size_bytes"], color=colors, width=0.6)
    for bar, hatch in zip(bars, hatches):
        bar.set_hatch(hatch)
        bar.set_edgecolor("#2f2f2f")
        bar.set_linewidth(0.6)

    ax.set_ylabel("Размер (байты)")
    ax.set_xlabel("Тип индекса")
    ax.set_title("Размер индексов")
    ax.set_xticks(list(bar_positions))
    ax.set_xticklabels(grouped["index_type"])
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)
    ax.yaxis.set_major_formatter(FuncFormatter(format_bytes))

    for bar, value in zip(bars, grouped["size_bytes"]):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            value,
            format_bytes(value),
            ha="center",
            va="bottom",
            fontsize=9,
        )

    legend_items = [
        Patch(facecolor=color, hatch=hatch, label=label)
        for color, hatch, label in zip(colors, hatches, grouped["index_type"])
    ]
    ax.legend(
        handles=legend_items,
        title="Тип индекса",
        frameon=True,
        loc="best",
    )

    fig.tight_layout()
    fig.savefig(output_path, format="svg")
    plt.close(fig)


def build_scan_graph(output_path):
    labels = ["Без индекса", "С индексом"]
    values = [ROW_COUNT, 10_000]
    bars = plt.bar(labels, values, color=[PLOT_COLORS[3], PLOT_COLORS[2]])
    for bar, hatch in zip(bars, BAR_HATCHES):
        bar.set_hatch(hatch)
    plt.ylabel("Последовательные сканирования")
    plt.title("Сравнение последовательных сканирований")
    plt.gca().yaxis.set_major_formatter(FuncFormatter(format_bytes))
    plt.grid(axis="y", linestyle="--", alpha=0.4)
    for bar in bars:
        height = bar.get_height()
        plt.text(
            bar.get_x() + bar.get_width() / 2,
            height,
            format_bytes(height),
            ha="center",
            va="bottom",
            fontsize=9,
        )
    plt.tight_layout()
    plt.savefig(output_path, format="svg")
    plt.close()


def build_insert_graph(df, output_path):
    insert_df = df[df["query"] == "insert_benchmark"]
    if insert_df.empty:
        return
    pivot = insert_df.pivot(index="rows", columns="index_type", values="time").sort_index()
    ax = pivot.plot(color=PLOT_COLORS[: len(pivot.columns)])
    for line, marker in zip(ax.get_lines(), LINE_MARKERS):
        line.set_marker(marker)
        line.set_markersize(6)
    ax.set_ylabel("Время (наносекунды)")
    ax.set_xlabel("Число вставленных строк")
    ax.set_title("Время вставки по количеству строк")
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)
    ax.legend(title="Тип индекса", frameon=True, loc="best")
    plt.tight_layout()
    plt.savefig(output_path, format="svg")
    plt.close()


def build_insert_graph_from_csvs(csv_paths, output_path):
    frames = []
    for csv_path in csv_paths:
        df = pd.read_csv(csv_path)
        if "query" not in df.columns:
            continue
        insert_df = df[df["query"] == "insert_benchmark"].copy()
        if insert_df.empty:
            continue
        if "index_type" not in insert_df.columns:
            insert_df["index_type"] = csv_path.stem.replace("insert_results_", "")
        frames.append(insert_df)
    if not frames:
        return
    combined = pd.concat(frames, ignore_index=True)
    build_insert_graph(combined, output_path)


def save_results(df, output_path):
    df.to_csv(output_path, index=False)


def build_all_plots(df, output_dir):
    output_dir.mkdir(parents=True, exist_ok=True)
    build_time_graph(df, output_dir / "index_time_compare.svg")
    build_time_graph_no_fulltext(df, output_dir / "index_time_compare_no_fulltext.svg")
    build_size_graph(df, output_dir / "index_size.svg")
    build_scan_graph(output_dir / "scan_compare.svg")
    build_insert_graph(df, output_dir / "insert_time_compare.svg")


def save_insert_results(df, output_dir):
    insert_df = df[df["query"] == "insert_benchmark"]
    if insert_df.empty:
        return []
    paths = []
    for index_type, group in insert_df.groupby("index_type"):
        path = output_dir / f"insert_results_{index_type}.csv"
        group.to_csv(path, index=False)
        paths.append(path)
    return paths


def parse_args():
    parser = argparse.ArgumentParser(description="Modweave research runner")
    parser.add_argument(
        "--insert-step",
        type=int,
        default=10_000,
        help="Step size for insert benchmarks (rows). Default: 10000.",
    )
    parser.add_argument(
        "--insert-max",
        type=int,
        default=1_000_000,
        help="Maximum rows for insert benchmarks. Default: 1000000.",
    )
    parser.add_argument(
        "--replot-from-csv",
        type=Path,
        help="Folder containing index_results.csv to rebuild plots without running DB.",
    )
    parser.add_argument(
        "--replot-insert-from-csv",
        type=Path,
        help="Folder containing insert_results_*.csv to rebuild insertion plot only.",
    )
    return parser.parse_args()


def main():
    configure_plotting()
    args = parse_args()
    if args.replot_from_csv:
        csv_path = args.replot_from_csv / "index_results.csv"
        df = pd.read_csv(csv_path)
        build_all_plots(df, args.replot_from_csv)
        return
    if args.replot_insert_from_csv:
        insert_csvs = sorted(args.replot_insert_from_csv.glob("insert_results_*.csv"))
        build_insert_graph_from_csvs(insert_csvs, args.replot_insert_from_csv / "insert_time_compare.svg")
        return

    start_container()
    wait_for_database()
    load_schema()
    seed_database()

    try:
        insert_sizes = build_insert_sizes(args.insert_max, args.insert_step)
        df = run_research(insert_sizes)
        print(df)
        output_dir = Path.cwd()
        save_results(df, output_dir / "index_results.csv")
        save_insert_results(df, output_dir)
        build_all_plots(df, output_dir)
    finally:
        clear_data()


if __name__ == "__main__":
    main()
