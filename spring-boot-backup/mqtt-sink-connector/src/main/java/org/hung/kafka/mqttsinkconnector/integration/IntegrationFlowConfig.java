package org.hung.kafka.mqttsinkconnector.integration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableIntegration
public class IntegrationFlowConfig {

    @Bean
    public IntegrationFlow flow(ConsumerFactory<String,String> factory) {
        ConsumerProperties props = new ConsumerProperties("quote");

        //MqttProperties mqttProps = new MqttProperties();
        //mqttProps.

        MqttMessage willMessage = new MqttMessage();
        willMessage.setPayload("I am going to offline...".getBytes());

        MqttConnectionOptions mqttConnOptions = new MqttConnectionOptions();
        mqttConnOptions.setServerURIs(new String[]{"tcp://localhost:1883"});
        mqttConnOptions.setUserName("quote-publisher");
        mqttConnOptions.setPassword("abcd1234".getBytes());
        mqttConnOptions.setTopicAliasMaximum(100);
        mqttConnOptions.setWill("lastword", willMessage);

        Mqttv5PahoMessageHandler mqttMsgHandler = new Mqttv5PahoMessageHandler(mqttConnOptions, "testing");
        mqttMsgHandler.setAsync(true);


        return IntegrationFlow.from(Kafka.inboundChannelAdapter(factory, props),
                        e -> e.poller(poller -> poller.fixedRate(Duration.ofSeconds(5))))
                    .enrichHeaders(Map.of("mqtt_topic", "quote/111","mqtt_retained",true))
                    //.handle(msg -> log.info("{}",msg))
                    .log()
                    .handle(mqttMsgHandler)
                    .get();
    }
}
