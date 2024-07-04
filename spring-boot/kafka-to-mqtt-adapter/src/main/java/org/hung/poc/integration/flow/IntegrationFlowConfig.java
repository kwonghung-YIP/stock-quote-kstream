package org.hung.poc.integration.flow;

import java.time.Duration;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.hung.stock.domain.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerProperties;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class IntegrationFlowConfig {
    
    @Value("${kafka-to-mqtt.kafka.topics.quote}")
    private String quoteTopicName;

    @Value("${kafka-to-mqtt.mqtt.client-id}")
    private String mqttClientId;

    @Bean
    @ConfigurationProperties("kafka-to-mqtt.mqtt.connect-option")
    public MqttConnectionOptions mqttConnectionOptions() {
        return new MqttConnectionOptions();
    }
    
    @Bean
    public IntegrationFlow flow(ConsumerFactory<String,Quote> factory, MqttConnectionOptions mqttConnOptions) {
        
        ConsumerProperties kafkaConsumerProps = new ConsumerProperties(quoteTopicName);

        Mqttv5PahoMessageHandler mqttMsgHandler = new Mqttv5PahoMessageHandler(mqttConnOptions, mqttClientId);
        mqttMsgHandler.setAsync(true);

        return IntegrationFlow.from(Kafka.inboundChannelAdapter(factory, kafkaConsumerProps),
            e -> e.poller(poller -> poller.fixedDelay(Duration.ofSeconds(5))))
            .enrichHeaders(h -> 
                h.headerExpression("mqtt_topic", "'quote/'+payload.code")
                 .header("mqtt_retained", true))
            .transform(Transformers.toJson(ResultType.STRING))
            //.log()
            .handle(mqttMsgHandler)
            .get();
    }

    @Component
    @ConfigurationPropertiesBinding
    static public class PasswordStringToByteArrayConverter implements Converter<String,byte[]> {

        @Override
        public byte[] convert(String source) {
            return source.getBytes();
        }
    } 
}
