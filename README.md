## Useful commands


```bash
#test API with curl
watch -n 2 "curl -v -u john:passwd http://localhost:8080/quote/find/MAR | jq ."
curl -v -u john:passwd http://localhost:8080/quote/all | jq .
curl -v -u john:passwd http://localhost:8080/quote/most-actives?n=6 | jq .
curl -v -u john:passwd http://localhost:8080/quote/gainers?n=6 | jq .
watch -n 1 'curl -s -u john:passwd http://localhost:8080/quote/losers?n=6 | jq .'

#remove all container
docker rm -f `docker ps -qa`

docker exec -it kafka \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic quote --from-beginning \
    --property print.headers=true

docker exec -it kafka \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic volume-feed --from-beginning \
    --property print.headers=true

docker exec -it kafka \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic quote --from-beginning \
    --property print.headers=true \
    --property print.key=true

cat <<EOF | tee client.properties
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="peter" password="peter-secret";
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
EOF

docker exec -it kafka \
    /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server localhost:9092 \
    --command-config client.properties \
    --list

docker exec -it kafka \
    /opt/kafka/bin/kafka-console-producer.sh \
    --bootstrap-server localhost:9092 \
    --producer.config /opt/kafka/bin/client.properties \
    --topic quote "Hello!"

docker exec -it -w /opt/kafka/bin kafka  \
    /bin/bash

helm install stock-quote-kstream ./ \
    --dry-run --debug \
    --namespace application \

skaffold dev \
    --profile=stock-quote-kstream \
    --skip-tests=true \
    --port-forward=user

helm install kafka-cluster ./kubernetes/apache-kafka/helm \
    --dry-run --debug \
    --namespace kafka

skaffold run --profile=kafka --port-forward=user

helm install portgresql ./kubernetes/postgresql/helm \
    --dry-run --debug \
    --namespace postgresql

skaffold dev --profile=postgresql

skaffold dev \
    -p postgresql,kafka \
    --port-forward=user
```

```bash
# run connect-standalone.sh with default connect-standalone.properties with kubectl
kubectl run --stdin --tty \
    kafka-connect-standalone --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/connect-standalone.sh /opt/kafka/config/connect-standalone.properties

# run connect-distributed.sh with default connect-distributed.properties with kubectl
kubectl run --stdin --tty \
    kafka-connect-distributed --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/connect-distributed.sh /opt/kafka/config/connect-distributed.properties

kubectl run --stdin --tty \
    apache-kafka --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /bin/bash
```

Attach kafka-broker pod to execute the commands
```bash
# list all available topics
kubectl exec --stdin --tty \
    broker-node-10 --namespace=kafka -- \
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

kubectl exec --stdin --tty \
    broker-node-10 --namespace=kafka -- \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic pg_stock_price_feed --from-beginning \
    --property print.headers=true \
    --property print.key=true

kubectl exec --stdin --tty \
    broker-node-10 --namespace=kafka -- \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic pg_stock_volume_feed --from-beginning \
    --property print.headers=true 

kubectl exec --stdin --tty \
    broker-node-10 --namespace=kafka -- \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic quote --from-beginning \
    --property print.headers=true \
    --property print.key=true

kubectl exec --stdin --tty \
    broker-node-10 --namespace=kafka -- \
    /bin/bash
```

```bash
# call kafka connect REST api with netshoot image (since curl does not included in apache kafka image)
kubectl run --stdin --tty \
    netshoot --image=nicolaka/netshoot:latest \
    --restart=Never --rm --namespace=kafka --command -- \
    curl -X GET \
    -H "Accept:application/json" \
    connect-node-1.connect.kafka.svc.cluster.local:8083/connectors\?expand=status\&expand=info | jq .

kubectl run --stdin --tty \
    netshoot --image=nicolaka/netshoot:latest \
    --restart=Never --rm --namespace=application --command -- \
    /bin/bash
```

```bash
kubectl get pods -lapp=postgresql -n=postgresql -o jsonpath='{.items[0].metadata.name}'

kubectl exec --stdin --tty \
    $(kubectl get pods -lapp=postgresql -n=postgresql -o jsonpath='{.items[0].metadata.name}') \
    --namespace=postgresql -- \
    psql --host=localhost --username=admin --dbname=db1 -c "call stock.genRandomPriceFeed(500,1,10);"

kubectl exec --stdin --tty \
    $(kubectl get pods -lapp=postgresql -n=postgresql -o jsonpath='{.items[0].metadata.name}') \
    --namespace=postgresql -- \
    psql --host=localhost --username=admin --dbname=db1 -c "call stock.genRandomVolumeFeed(500,1,10);"

kubectl run --stdin --tty \
    psql --image=docker.io/postgres:16.2 \
    --env "PGPASSWORD=passwd" \
    --restart=Never --rm --namespace=postgresql --command -- \
    psql -hpostgres-db.postgresql.svc.cluster.local -Uadmin -ddb1 -c "call stock.genRandomVolumeFeed(500,1,10);"
```

```sql
SELECT code,(1/52.0)*init_price 
from stock.stock_statistic
where code = 'AMD';

call stock.nextPriceFeed('AAPL',1/52.0);
call stock.genRandomPriceFeed();
```

```bash
docker run -it \
  --rm --name=hivemq-cli \
  --network mqtt-sink-connector_default --link hivemq \
  hivemq/mqtt-cli shell
```

```bash
connect --host=hivemq --port=1883
sub -t quote --stay --jsonOutput
```

```bash
docker run -it --rm \
  --network mqtt-sink-connector_default --link hivemq \
  hivemq/mqtt-cli shell

docker exec -it hivemq /bin/bash
docker exec -it hivemq cat conf/config.xml
docker exec -it hivemq cat extensions/hivemq-enterprise-security-extension/conf/config.xml

connect --host hivemq --port 1883 \
    -u mqtt-user-1 -pw mqtt-password-1 \
    --mqttVersion 5 --identifierPrefix hivemqcli \
    --willTopic lastword --willMessage "I am die!" \
    --topicAliasMax 10

connect --host hivemq --port 1883 \
    -u mqtt-user-1 -pw mqtt-password-1 \
    --mqttVersion 5

connect --host hivemq --port 1883 \
    -u quote-publisher -pw abcd1234 \
    --mqttVersion 5 \
    --willTopic lastword --willMessage "I am die!"

con -h hivemq -p 8000 -ws -ws:path /mqtt -V 5 -u 477c250c-aea6-43bb-aa54-3af39ffb587d -pw


sub --stay --topic quote/111 \
    --showTopics --jsonOutput

sub --stay --topic listword \
    --showTopics --jsonOutput -oc

pub -t test -m hello -q 1
sub -s -t test -q 1

connect -h hivemq -p 1883 -ip hivemq_cli_ \
  -Wt last-word -Wm "I'm will be back" -Wr

pub -t quote/111 -m hello -q 1
sub -s -t quote/111 -q 1
sub -sJT -t quote/# -q 1
sub -sJT -t $share/grp1/quote/# -q 1
sub -sJT -t last-word -q 1

docker run -it --rm \
  --network mqtt-sink-connector_default --link hivemq \
  node:22.2 /bin/bash

npm install mqtt --save --global

mqtt subscribe -v -h hivemq -p 1883 -i mqttjs_cli_1 -t quote/+

java -jar hivemq-ese-helper.jar hash create -a PKCS5S2 -i 10 -s nNctCFrfwfZXW4oNi81AUQ== password

java -jar hivemq-ese-helper.jar hash create -a PKCS5S2 -i 10 -s nNctCFrfwfZXW4oNi81AUQ== -p
```

References:
[Kafka-UI configuration properties](https://docs.kafka-ui.provectus.io/configuration/misc-configuration-properties)
[Kubernetes - DNS for Services and Pods](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/)
[Fix Powerline font for VSCode terminal](https://cloverinks.medium.com/oh-my-zsh-agnoster-theme-not-showing-correct-font-on-vscode-ubuntu-47b5e8dcbada)
[hivemq-cli Reference](https://hivemq.github.io/mqtt-cli/docs/shell/connect/)
[hivemq enterprise security extenstion config](https://docs.hivemq.com/hivemq-enterprise-security-extension/latest/getting-started.html#installation)