package monitors;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import io.github.thisisnozaku.charactercreator.plugins.monitors.S3EventSqsQueueListener;
import org.junit.AfterClass;
import org.junit.Test;
import org.testng.annotations.BeforeClass;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * Created by Damien on 12/30/2016.
 */
public class S3EventMonitorTest {
    private static final String testQueueName = "s3-monitor-test-queue";
    @Inject
    private static AmazonSQS sqsClient;

    @BeforeClass
    public static void setup() {

    }

    @AfterClass
    public static void cleanup() {
        sqsClient.deleteQueue(testQueueName);
    }

    @Test
    public void testNewFileCreated() {
        Result testResult = new Result();

        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.withQueueName(testQueueName);
        sqsClient.createQueue(createQueueRequest);

        assertNotNull(sqsClient.getQueueUrl(testQueueName).getQueueUrl());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        S3EventSqsQueueListener monitor = new S3EventSqsQueueListener(sqsClient, executorService, testQueueName);
        monitor.addCallback(message -> {
            try {
                S3EventNotification notification = S3EventNotification.parseJson(message.getBody());
                assertTrue(notification.getRecords().size() == 1);
                S3EventNotification.S3EventNotificationRecord record = notification.getRecords().get(0);
                assertEquals(record.getEventName(), S3Event.ObjectCreated.name());
                assertEquals(record.getS3().getBucket().getName(), "plugin");
                testResult.passed = true;
            } finally {
                testResult.completed = true;
            }
        });

        List<S3EventNotification.S3EventNotificationRecord> records = new LinkedList<>();
        records.add(new S3EventNotification.S3EventNotificationRecord(Region.getRegion(Regions.DEFAULT_REGION).getName(),
                S3Event.ObjectCreated.name(),
                "aws:s3",
                null,
                "2.0",
                new S3EventNotification.RequestParametersEntity("127.0.0.1"),
                new S3EventNotification.ResponseElementsEntity("", ""),
                new S3EventNotification.S3Entity("",
                        new S3EventNotification.S3BucketEntity("plugins",
                                new S3EventNotification.UserIdentityEntity(""),
                                ""),
                        new S3EventNotification.S3ObjectEntity("plugin",
                                0L, "", ""),
                        ""),
                new S3EventNotification.UserIdentityEntity("")
        ));
        S3EventNotification testEvent = new S3EventNotification(records);
        SendMessageRequest request = new SendMessageRequest(sqsClient.getQueueUrl(testQueueName).getQueueUrl(),
                testEvent.toJson());
        sqsClient.sendMessage(request);

        while (!testResult.completed) {

        }

        assertTrue(testResult.passed);
    }

    static class Result {
        public boolean completed;
        public boolean passed;
    }
}