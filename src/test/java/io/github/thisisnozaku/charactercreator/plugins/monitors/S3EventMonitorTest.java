package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.resources.SQSQueueResource;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.simpleemail.model.S3Action;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.AddPermissionRequest;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * Created by Damien on 12/30/2016.
 */
public class S3EventMonitorTest {
    private final String testQueueName = "s3-monitor-test-queue";
    @Test
    public void testNewFileCreated(){
        AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        ClientConfiguration configuration = new ClientConfiguration();
        AmazonSQSClient sqsClient = new AmazonSQSClient(configuration);
        sqsClient.configureRegion(Regions.US_WEST_2);

        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.withQueueName(testQueueName);
        sqsClient.createQueue(createQueueRequest);
        
        assertNotNull(sqsClient.getQueueUrl(testQueueName).getQueueUrl());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        S3EventMonitor monitor = new S3EventMonitor(sqsClient, executorService, testQueueName);

        List<S3EventNotification.S3EventNotificationRecord> records = new LinkedList<>();
        records.add(new S3EventNotification.S3EventNotificationRecord(Region.getRegion(Regions.DEFAULT_REGION).getName(),
                S3Event.ObjectCreated.name(),
                "aws:s3",
                null,
                "2.0",
                new S3EventNotification.RequestParametersEntity("127.0.0.1"),
                new S3EventNotification.ResponseElementsEntity("",""),
                new S3EventNotification.S3Entity("",
                        new S3EventNotification.S3BucketEntity("plugins",
                                new S3EventNotification.UserIdentityEntity(""),
                                ""),
                        new S3EventNotification.S3ObjectEntity("",
                                0L, "", ""),
                        ""),
                new S3EventNotification.UserIdentityEntity("")
                ));
        S3EventNotification testEvent = new S3EventNotification(records);
        SendMessageRequest request = new SendMessageRequest(sqsClient.getQueueUrl(testQueueName).getQueueUrl(),
                testEvent.toJson());
        sqsClient.sendMessage(request);
    }
}