package sk.projects.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class FileWritingRecordsHandlerTest {

    @Test
    public void process() throws IOException {
        final Path tempFilePath = Files.createTempFile("test-handler", ".out");
        try {
            final ConsumerRecordsHandler<String, String> recordsHandler = new FileWritingRecordsHandler(tempFilePath);
            recordsHandler.process(createConsumerRecords());
            final List<String> expectedWords = Arrays.asList("it's but", "a flesh wound", "come back");
            List<String> actualRecords = Files.readAllLines(tempFilePath);
            assertThat(actualRecords, equalTo(expectedWords));
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }

        private ConsumerRecords<String, String> createConsumerRecords() {
            final String topic = "test";
            final int partition = 0;
            final TopicPartition topicPartition = new TopicPartition(topic, partition);
            final List<ConsumerRecord<String, String>> consumerRecordsList = new ArrayList<>();
            consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "it's but"));
            consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "a flesh wound"));
            consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "come back"));
            final Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
            recordsMap.put(topicPartition, consumerRecordsList);

            return new ConsumerRecords<>(recordsMap);
        }
}