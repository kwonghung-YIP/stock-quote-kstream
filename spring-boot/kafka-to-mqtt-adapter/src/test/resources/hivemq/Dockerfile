ARG HIVEMQ_VERSION=dns-4.30.0
FROM hivemq/hivemq4:${HIVEMQ_VERSION}

ADD conf/config.xml $HOME/conf/config.xml
ADD extensions/ese/config.xml $HOME/extensions/hivemq-enterprise-security-extension/conf/config.xml
ADD extensions/ese/file-realm/ese-file-realm.xml $HOME/extensions/hivemq-enterprise-security-extension/conf/ese-file-realm.xml

ENV HIVEMQ_ALLOW_ALL_CLIENTS=false
ENV HIVEMQ_LOG_LEVEL=DEBUG

RUN <<EOF
cd /opt/hivemq/extensions/hivemq-enterprise-security-extension
rm ./DISABLED
EOF