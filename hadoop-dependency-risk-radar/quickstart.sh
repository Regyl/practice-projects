curl https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz --output hadoop-3.3.6.tar.gz
pid=$!
wait $pid

tar -xvf hadoop-3.3.6.tar.gz
pid=$!
wait $pid

cd hadoop-3.3.6
pid=$!
wait $pid

bin/hadoop
pid=$!
wait $pid

curl https://cf-courses-data.s3.us.cloud-object-storage.appdomain.cloud/IBM-BD0225EN-SkillsNetwork/labs/data/data.txt --output data.txt
pid=$!
wait $pid

bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.6.jar wordcount data.txt output
pid=$!
wait $pid

ls output
pid=$!
wait $pid

cat  output/part-r-00000
