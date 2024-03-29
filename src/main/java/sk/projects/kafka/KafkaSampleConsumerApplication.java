package sk.projects.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaSampleConsumerApplication {

    private boolean keepConsuming = true;
    private final Consumer<String, String> consumer;
    private final ConsumerRecordsHandler<String, String> recordsHandler;

    public KafkaSampleConsumerApplication(Consumer<String, String> consumer, ConsumerRecordsHandler<String, String> recordsHandler) {
        this.consumer = consumer;
        this.recordsHandler = recordsHandler;
    }

    public void runConsume(final Properties consumerProps) {
        try {
            consumer.subscribe(Collections.singletonList(consumerProps.getProperty("input.topic.name")));
            while (keepConsuming) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                recordsHandler.process(consumerRecords);
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        keepConsuming = false;
    }

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties props = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        props.load(input);
        input.close();
        return props;
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "This program takes one argument: the path to an environment configuration file.");
        }

        final Properties consumerAppProps = KafkaSampleConsumerApplication.loadProperties(args[0]);
        final String filePath = consumerAppProps.getProperty("file.path");
        final Consumer<String, String> consumer = new KafkaConsumer<>(consumerAppProps);
        final ConsumerRecordsHandler<String, String> recordsHandler = new FileWritingRecordsHandler(Paths.get(filePath));
        final KafkaSampleConsumerApplication consumerApplication = new KafkaSampleConsumerApplication(consumer, recordsHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(consumerApplication::shutdown));

        consumerApplication.runConsume(consumerAppProps);
    }

}
