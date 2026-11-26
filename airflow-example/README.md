# Cassandra logs to HDFS

The `cassandra_logs_to_hdfs` DAG runs every five minutes on `erbox-1`. Each run reads the `log` field from all rows in `dev.fluent_bit_logs` at `127.0.0.1:9042` and replaces `/user/airflow/fluent_bit_logs.jsonl` in HDFS. The file contains one UTF-8 JSON object per row. An empty table produces an empty file.

## Prepare erbox-1

Run these commands on `erbox-1` after installing Airflow and Hadoop with `shell-setup`.

```sh
sudo /opt/airflow/venv/bin/python -m pip install cassandra-driver
sudo -u hadoop /opt/hadoop-3.5.0/bin/hdfs dfs -mkdir -p /user/airflow
sudo -u hadoop /opt/hadoop-3.5.0/bin/hdfs dfs -chown airflow /user/airflow
```

If Cassandra requires authentication, create an Airflow connection named `cassandra_logs` with its login and password. You can set `AIRFLOW_CONN_CASSANDRA_LOGS` in the Airflow service environment using the JSON form `{"conn_type":"cassandra","login":"<username>","password":"<password>"}`. Restart `airflow-standalone.service` after changing its environment. For Cassandra without authentication, leave the connection unset.

## Deploy and verify

From the `shell-setup/ansible` directory on the Ansible control machine, run the existing DAG deployment playbook. Adjust the absolute path if your checkout is elsewhere.

```sh
ansible-playbook playbooks/airflow/deploy-dag.yml -K \
  -e 'dag_path=/home/smith/repos/practice-projects/airflow-example/cassandra_logs_to_hdfs.py'
```

Unpause `cassandra_logs_to_hdfs` in Airflow and trigger a run or wait for the next five-minute tick. Once the task succeeds, read the result on `erbox-1` as the Airflow service account.

```sh
sudo -u airflow /opt/hadoop-3.5.0/bin/hdfs dfs -cat /user/airflow/fluent_bit_logs.jsonl
```

Hadoop uploads to a temporary HDFS path before replacing the previous file. Readers may briefly see the destination missing during replacement. If Cassandra extraction fails, the previous export remains available.
