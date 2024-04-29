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
    --topic quote --from-beginning

helm install stock-quote-kstream ./ \
    --dry-run --debug \
    --namespace application

skaffold dev \
    --profile=stock-quote-kstream \
    --namespace='application' \
    --skip-tests=true \
    --port-forward=user

helm install stock-quote-kstream ./kubernetes/apache-kafka/helm \
    --dry-run --debug \
    --namespace kadka

skaffold dev \
    --profile=apache-kafka-cluster \
    --namespace='kafka' \
    --skip-tests=true \
    --port-forward=user
```