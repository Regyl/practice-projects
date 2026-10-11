import csv
import os
import time
from pathlib import Path

import matplotlib.pyplot as plt
import psycopg
from psycopg import sql

ROOT = Path(__file__).resolve().parent
STATS_PATH = ROOT / "statistics.csv"

ROW_COUNTS = (1_000_000, 10_000_000, 100_000_000, 1_000_000_000)

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


def format_value(metric, value):
    if metric == "index_size":
        return str(int(value))
    return f"{value:.3f}"


def format_row_count(row_count):
    value = int(row_count)
    if value >= 1_000_000_000 and value % 1_000_000_000 == 0:
        return f"{value // 1_000_000_000}B"
    if value >= 1_000_000 and value % 1_000_000 == 0:
        return f"{value // 1_000_000}M"
    return str(value)


def display_value(metric, value):
    if metric == "index_size":
        return value / (1024 ** 2)
    return value / 1000


def display_unit(metric):
    if metric == "index_size":
        return "MiB"
    return "s"


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


def plot_charts(stats_path):
    with stats_path.open(newline="") as stats_file:
        rows = list(csv.DictReader(stats_file))

    metrics = []
    for row in rows:
        if row["metric"] not in metrics:
            metrics.append(row["metric"])

    row_counts = []
    for row in rows:
        if row["rows"] not in row_counts:
            row_counts.append(row["rows"])
    row_counts.sort(key=int)

    for metric in metrics:
        subset = [row for row in rows if row["metric"] == metric]
        width = 0.35
        positions = list(range(len(row_counts)))
        labeled_values = []

        figure, axes = plt.subplots(layout="constrained")
        for offset, uuid_type in ((-width / 2, "v7"), (width / 2, "v4")):
            by_rows = {
                row["rows"]: float(row["value"])
                for row in subset
                if row["type"] == uuid_type
            }
            values = [display_value(metric, by_rows[count]) for count in row_counts]
            labeled_values.extend(values)
            bars = axes.bar(
                [position + offset for position in positions],
                values,
                width,
                label=uuid_type,
            )
            axes.bar_label(bars, labels=[f"{value:.1f}" for value in values], padding=3)

        axes.set_xticks(
            positions,
            [format_row_count(count) for count in row_counts],
        )
        axes.set_ylim(0, max(labeled_values) * 1.15)
        axes.set_title(metric)
        axes.set_xlabel("rows")
        axes.set_ylabel(display_unit(metric))
        axes.legend(loc="upper left")
        figure.savefig(stats_path.with_name(f"{metric}_histogram.png"))

    plt.show()


def main():
    with connect() as conn:
        run_benchmark(conn, ROW_COUNTS, STATS_PATH)
    plot_charts(STATS_PATH)


if __name__ == "__main__":
    main()
