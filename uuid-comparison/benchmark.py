import csv
import json
import os
import statistics
import time
from pathlib import Path

import psycopg
from psycopg import sql

ROOT = Path(__file__).resolve().parent
STATS_PATH = ROOT / "statistics.csv"

ROW_COUNTS = (1_000_000, 10_000_000, 100_000_000, 1_000_000_000)
LOOKUP_REPEATS = 11

VARIANTS = (
    {
        "type": "v7",
        "table": sql.Identifier("test_v7"),
        "index": "test_v7_pkey",
        "fn": sql.Identifier("uuidv7"),
    },
    {
        "type": "v4",
        "table": sql.Identifier("test_v4"),
        "index": "test_v4_pkey",
        "fn": sql.Identifier("uuidv4"),
    },
)


def connect():
    return psycopg.connect(
        host=os.environ.get("PGHOST", "localhost"),
        port=os.environ.get("PGPORT", "5432"),
        dbname=os.environ.get("PGDATABASE", "db"),
        user=os.environ.get("PGUSER", "user"),
        password=os.environ.get("PGPASSWORD", "pwd"),
        autocommit=True,
    )


def reset_tables(conn):
    conn.execute("TRUNCATE TABLE test_v7, test_v4")
    conn.execute("VACUUM (INDEX_CLEANUP ON, FULL) test_v7")
    conn.execute("VACUUM (INDEX_CLEANUP ON, FULL) test_v4")


def insert_rows(conn, variant, row_count):
    statement = sql.SQL(
        "INSERT INTO {} (id) SELECT {}() FROM generate_series(1, %s)"
    ).format(variant["table"], variant["fn"])
    started = time.perf_counter()
    conn.execute(statement, (row_count,))
    return (time.perf_counter() - started) * 1000


def index_size_bytes(conn, variant):
    statement = sql.SQL("SELECT pg_relation_size({}::regclass)").format(
        sql.Literal(variant["index"])
    )
    row = conn.execute(statement).fetchone()
    return int(row[0])


def capture_id(conn, variant):
    sampled = sql.SQL("SELECT id FROM {} TABLESAMPLE SYSTEM (1) LIMIT 1").format(
        variant["table"]
    )
    row = conn.execute(sampled).fetchone()
    if row is None:
        fallback = sql.SQL("SELECT id FROM {} LIMIT 1").format(variant["table"])
        row = conn.execute(fallback).fetchone()
    if row is None:
        raise RuntimeError(f"no id found in {variant['type']} table after insert")
    return row[0]


def lookup_time_ms(conn, variant, lookup_id):
    statement = sql.SQL("SELECT * FROM {} WHERE id = %s").format(variant["table"])
    conn.execute(statement, (lookup_id,)).fetchone()

    explain = sql.SQL(
        "EXPLAIN (ANALYZE, FORMAT JSON) SELECT * FROM {} WHERE id = %s"
    ).format(variant["table"])
    samples = []
    for _ in range(LOOKUP_REPEATS):
        document = conn.execute(explain, (lookup_id,)).fetchone()[0]
        if isinstance(document, str):
            document = json.loads(document)
        samples.append(float(document[0]["Execution Time"]))
    return statistics.median(samples)


def format_value(metric, value):
    if metric == "index_size":
        return str(int(value))
    return f"{value:.3f}"


def write_metric(stats_file, writer, row_count, uuid_type, metric, value, unit):
    rendered = format_value(metric, value)
    writer.writerow([row_count, uuid_type, metric, rendered, unit])
    stats_file.flush()
    print(f"{row_count} {uuid_type} {metric} {rendered} {unit}", flush=True)


def run_benchmark(conn, row_counts, stats_path):
    with stats_path.open("w", newline="") as stats_file:
        writer = csv.writer(stats_file)
        writer.writerow(["rows", "type", "metric", "value", "unit"])
        stats_file.flush()

        for row_count in row_counts:
            for variant in VARIANTS:
                reset_tables(conn)

                insert_ms = insert_rows(conn, variant, row_count)
                write_metric(
                    stats_file, writer, row_count, variant["type"], "insert", insert_ms, "ms"
                )

                size_bytes = index_size_bytes(conn, variant)
                write_metric(
                    stats_file,
                    writer,
                    row_count,
                    variant["type"],
                    "index_size",
                    size_bytes,
                    "bytes",
                )

                lookup_id = capture_id(conn, variant)
                lookup_ms = lookup_time_ms(conn, variant, lookup_id)
                write_metric(
                    stats_file, writer, row_count, variant["type"], "lookup", lookup_ms, "ms"
                )


def main():
    with connect() as conn:
        run_benchmark(conn, ROW_COUNTS, STATS_PATH)


if __name__ == "__main__":
    main()
