ARG HIVEMQ_VERSION=dns-4.30.0
FROM hivemq/hivemq4:${HIVEMQ_VERSION}

ADD conf/config.xml $HOME/conf/config.xml
ADD extensions/ese/config.xml $HOME/extensions/hivemq-enterprise-security-extension/conf/config.xml
ADD extensions/ese/ese-file-realm.xml $HOME/extensions/hivemq-enterprise-security-extension/conf/ese-file-realm.xml

ENV HIVEMQ_ALLOW_ALL_CLIENTS=false
ENV HIVEMQ_LOG_LEVEL=DEBUG

RUN <<EOF
cd /opt/hivemq/extensions/hivemq-enterprise-security-extension
rm ./DISABLED
openssl s_client \
  -connect auth0.com:443 \
  -showcerts -certform PEM \
  </dev/null 2>/dev/null | \
  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > conf/trust.crt
keytool -importcert -noprompt \
  -file conf/trust.crt -alias auth0.com \
  -storepass abcd1234 \
  -keystore conf/oauth2-trust.jks
EOF