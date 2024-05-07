## Useful commands


```bash
#test API with curl
curl -v -u john:passwd http://localhost:8080/quote/AAPL
curl -v -u john:passwd http://localhost:8080/quote/

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

docker exec -it -w /opt/kafka/bin kafka  \
    /bin/bash

helm install stock-quote-kstream ./ \
    --dry-run --debug \
    --namespace application

skaffold dev \
    --profile=stock-quote-kstream \
    --namespace='application' \
    --skip-tests=true \
    --port-forward=user

helm install kafka-cluster ./kubernetes/apache-kafka/helm \
    --dry-run --debug \
    --namespace kafka

skaffold dev --profile=kafka --port-forward=user

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
    --property print.headers=true

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
    --property print.headers=true

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
    --restart=Never --rm --namespace=kafka --command -- \
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
```

```sql
SELECT code,(1/52.0)*init_price 
from stock.stock_statistic
where code = 'AMD';

call stock.nextPriceFeed('AAPL',1/52.0);
call stock.genRandomPriceFeed();
```
References:
[Kafka-UI configuration properties](https://docs.kafka-ui.provectus.io/configuration/misc-configuration-properties)