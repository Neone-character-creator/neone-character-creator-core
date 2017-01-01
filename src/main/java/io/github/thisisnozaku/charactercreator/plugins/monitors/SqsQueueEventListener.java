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
 * Listens for events from one or more Amazon Simple Queue Service queues.
 * Created by Damien on 12/3/2016.
 */
@Service
@Profile("aws")
public class SqsQueueEventListener extends PluginMonitorAdapter {
    private final AmazonSQSClient sqsClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    public SqsQueueEventListener(AmazonSQSClient sqsClient, ScheduledExecutorService executorService, String... queueNames) {
        this.sqsClient = sqsClient;
        Arrays.asList(queueNames).stream().forEach(q->{
            executorService.scheduleAtFixedRate(()->{
                ReceiveMessageRequest request = new ReceiveMessageRequest(q);
                Collection<Message> messages = sqsClient.receiveMessage(request).getMessages();
                messages.stream().forEach(message -> {
                    String messageBody = message.getBody();
                    S3EventNotification notification = S3EventNotification.parseJson(messageBody);
                    notification.getRecords().forEach(record->{
                        // Try to determine if there's a constant that could be insterted here
                        if(record.getEventSource().equals("aws:s3")){
                            if(record.getS3().getBucket().getName().equals("plugins")){

                            };
                        }
                    });
                });
            }, 0, 1000L, TimeUnit.MILLISECONDS);
        });
    }
}