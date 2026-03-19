package com.example.paymentledger.kafka;

import com.example.paymentledger.domain.PaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit Kafka configuration for typed PaymentEvent producer and consumer.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Producer configuration

    @Bean
    public ProducerFactory<String, PaymentEvent> paymentEventProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Do not add type headers to keep payloads simple and language-agnostic
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate() {
        return new KafkaTemplate<>(paymentEventProducerFactory());
    }

    // Consumer configuration

    @Bean
    public ConsumerFactory<String, PaymentEvent> paymentEventConsumerFactory() {
        JsonDeserializer<PaymentEvent> deserializer = new JsonDeserializer<>(PaymentEvent.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Let the container control commits after successful processing
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> paymentEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentEventConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());
        // Records are acknowledged only after the listener method returns successfully
        factory.getContainerProperties().setAckOnError(false);
        return factory;
    }

    /**
     * Error handler that logs failures but keeps the listener container running.
     * Uses a small fixed backoff and skips records that continue to fail.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        FixedBackOff backOff = new FixedBackOff(1_000L, 3L); // 3 retries, 1s apart
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, ex) -> {
            if (record == null) {
                log.error("Unrecoverable error in Kafka listener with no record context", ex);
                return;
            }
            log.error("Unrecoverable error processing record from topic={} partition={} offset={} key={} - skipping record",
                    record.topic(), record.partition(), record.offset(), record.key(), ex);
        }, backOff);
        // Example: configuration / validation issues are not worth retrying
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
