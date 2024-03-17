package sk.projects.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class KafkaSampleConsumerApplicationTest {

    private static final String TEST_CONFIG_FILE = "configuration/test.properties";
    @Test
    public void runConsume() throws IOException {
        final Path tempFilePath = Files.createTempFile("test-consumer-output", ".out");
        final ConsumerRecordsHandler<String, String> recordsHandler = new FileWritingRecordsHandler(tempFilePath);
        final Properties testConsumerProps = KafkaSampleConsumerApplication.loadProperties(TEST_CONFIG_FILE);
        final String topic = testConsumerProps.getProperty("input.topic.name");
        final TopicPartition topicPartition = new TopicPartition(topic, 0);
        final MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

        final KafkaSampleConsumerApplication consumerApplication = new KafkaSampleConsumerApplication(mockConsumer, recordsHandler);

        mockConsumer.schedulePollTask(() -> addTopicPartitionsAssignmentAndAddConsumerRecords(topic, mockConsumer, topicPartition));
        mockConsumer.schedulePollTask(consumerApplication::shutdown);
        consumerApplication.runConsume(testConsumerProps);

        final List<String> expectedWords = Arrays.asList("foo", "bar", "baz");
        List<String> actualRecords = Files.readAllLines(tempFilePath);
        assertThat(actualRecords, equalTo(expectedWords));
    }

    private void addTopicPartitionsAssignmentAndAddConsumerRecords(final String topic,
                                                                   final MockConsumer<String, String> mockConsumer,
                                                                   final TopicPartition topicPartition) {

        final Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(topicPartition, 0L);
        mockConsumer.rebalance(Collections.singletonList(topicPartition));
        mockConsumer.updateBeginningOffsets(beginningOffsets);
        addConsumerRecords(mockConsumer,topic);
    }

    private void addConsumerRecords(final MockConsumer<String, String> mockConsumer, final String topic) {
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 0, null, "foo"));
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 1, null, "bar"));
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 2, null, "baz"));
    }

}