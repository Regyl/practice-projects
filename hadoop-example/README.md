# Fluent Bit hostname counts

This Hadoop MapReduce job counts log records by hostname. It reads JSON Lines from the Airflow Cassandra export. Each record's `log` field contains JSON text. The mapper uses `source_hostname`, falls back to `_HOSTNAME`, and groups records missing both under `undefined`. The reducer writes tab-separated hostname and count pairs.

## Build and run

Build the production JAR with Maven and JDK 17 or newer.

```bash
mvn clean package -Pprod
```

On a Hadoop 3.5.0 submission host configured for HDFS and YARN, run the following commands with the JAR at the path shown. The first argument is the input file or directory. The second is a new output directory. A relative HDFS output path resolves under your HDFS home directory. Hadoop rejects an existing output directory and preserves its contents.

```bash
output="hostname-counts-$(date +%Y%m%d-%H%M%S)"
hadoop jar target/hadoop-example-1.0-prod.jar /user/airflow/fluent_bit_logs.jsonl "$output"
hdfs dfs -cat "$output/part-r-*"
```

Look for **Fluent Bit hostname counts** in the YARN ResourceManager. To override a cluster setting for one submission, place a generic Hadoop option such as `-Dmapreduce.job.reduces=2` between the JAR name and the input path.

## Run from VS Code

Open this project folder in VS Code and select **Hadoop hostname counts** in the Run and Debug configuration dropdown. The Java **Run** button then launches `com.github.regyl.Main` with the input and output paths in `.vscode/launch.json`. Edit its `args` array to change them; the first value is the input file or directory, and the second is a new output directory. Hadoop does not overwrite an existing output directory, so choose a new output path or remove the previous output before rerunning.

To pass a Hadoop generic option, add it before the paths in `args`, for example:

```json
"args": [
    "-Dmapreduce.job.reduces=2",
    "${workspaceFolder}/src/main/resources/fluent_bit_logs.jsonl",
    "${workspaceFolder}/target/hostname-counts-output"
]
```

These are application arguments parsed by Hadoop's `ToolRunner`; use `vmArgs` only for JVM options such as `-Xmx1g`.
