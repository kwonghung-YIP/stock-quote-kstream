FROM nicolaka/netshoot:latest AS builder

WORKDIR /build

RUN curl -O https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/10.7.4/kafka-connect-jdbc-10.7.4.jar
RUN curl -O https://jdbc.postgresql.org/download/postgresql-42.7.3.jar 
RUN curl -O https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/2.6.1.Final/debezium-connector-postgres-2.6.1.Final.jar

FROM apache/kafka:3.7.0

COPY --from=builder --chown=appuser:appuser /build/kafka-connect-jdbc-10.7.4.jar /opt/kafka/libs
COPY --from=builder --chown=appuser:appuser /build/postgresql-42.7.3.jar /opt/kafka/libs
COPY --from=builder --chown=appuser:appuser /build/debezium-connector-postgres-2.6.1.Final.jar /opt/kafka/libs

WORKDIR /opt/kafka

ENTRYPOINT ["bin/connect-standalone.sh", "config/connect-standalone.properties"]