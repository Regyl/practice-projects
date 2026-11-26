"""Refresh the HDFS export of Cassandra's Fluent Bit logs."""

import json
import logging
import shutil
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path
from tempfile import mkdtemp

from airflow.exceptions import AirflowNotFoundException
from airflow.sdk import DAG, Connection, task

HDFS_CLI = "/opt/hadoop-3.5.0/bin/hdfs"
HDFS_EXPORT = "/user/airflow/fluent_bit_logs.jsonl"
LOGGER = logging.getLogger(__name__)


with DAG(
    dag_id="cassandra_logs_to_hdfs",
    description="Replace the HDFS export of Fluent Bit logs from Cassandra",
    schedule="*/5 * * * *",
    start_date=datetime(2026, 1, 1, tzinfo=timezone.utc),
    catchup=False,
    max_active_runs=1,
    default_args={"retries": 1, "retry_delay": timedelta(minutes=1)},
    tags=["cassandra", "hdfs"],
) as dag:

    @task
    def export_logs() -> str:
        from cassandra.auth import PlainTextAuthProvider
        from cassandra.cluster import Cluster
        from cassandra.query import SimpleStatement

        try:
            connection = Connection.get("cassandra_logs")
        except AirflowNotFoundException:
            connection = None

        auth_provider = None
        if connection and (connection.login or connection.password):
            if not connection.login or not connection.password:
                raise ValueError("Set both Login and Password on the cassandra_logs connection")
            auth_provider = PlainTextAuthProvider(connection.login, connection.password)

        query = SimpleStatement("SELECT log FROM dev.fluent_bit_logs", fetch_size=1000)
        directory = Path(mkdtemp(prefix="cassandra_logs_"))
        local_export = directory / Path(HDFS_EXPORT).name
        row_count = 0
        try:
            with Cluster(["127.0.0.1"], port=9042, auth_provider=auth_provider) as cluster:
                session = cluster.connect()
                with local_export.open("w", encoding="utf-8") as output:
                    for row in session.execute(query):
                        output.write(json.dumps({"log": row.log}, ensure_ascii=False) + "\n")
                        row_count += 1

        except BaseException:
            shutil.rmtree(directory, ignore_errors=True)
            raise

        LOGGER.info("Extracted %d logs from Cassandra", row_count)
        return str(local_export)

    @task
    def upload_logs(local_export: str) -> None:
        subprocess.run(
            [HDFS_CLI, "dfs", "-put", "-f", local_export, HDFS_EXPORT],
            check=True,
        )
        shutil.rmtree(Path(local_export).parent, ignore_errors=True)
        LOGGER.info("Uploaded logs to %s", HDFS_EXPORT)

    local_export = export_logs()
    upload_logs(local_export)
