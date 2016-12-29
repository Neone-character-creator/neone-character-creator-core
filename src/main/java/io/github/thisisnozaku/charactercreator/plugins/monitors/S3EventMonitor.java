package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Exposes a REST endpoint that listens for events related to plugins generated by the AWS S3 service.
 * Created by Damien on 12/3/2016.
 */
@Service
@Profile("aws")
public class S3EventMonitor extends PluginMonitorAdapter {
    private final AmazonSQSClient sqsClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    public S3EventMonitor(AmazonSQSClient sqsClient, ScheduledExecutorService executorService, String... queueNames) {
        this.sqsClient = sqsClient;
        Arrays.asList(queueNames).stream().forEach(q->{
            executorService.scheduleAtFixedRate(()->{
                ReceiveMessageRequest request = new ReceiveMessageRequest(q);
                Collection<Message> messages = sqsClient.receiveMessage(request).getMessages();
                messages.stream().forEach(message -> {
                    String messageBody = message.getBody();
                    S3EventNotification notification = S3EventNotification.parseJson(messageBody);
                    notification.getRecords().forEach(record->{
                        record.getS3();
                    });
                });
            }, 0, 1000L, TimeUnit.MILLISECONDS);
        });
    }
}