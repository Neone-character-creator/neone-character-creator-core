package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Listens for events from one or more Amazon Simple Queue Service queues.
 * Created by Damien on 12/3/2016.
 */
@Service
@Profile("aws")
public class AmazonSqsQueueMonitor extends PluginMonitorAdapter {
    private final AmazonSQS sqsClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ScheduledExecutorService executorService;
    private final List<String> monitoredQueueNames;
    private final Region currentRegion;

    public AmazonSqsQueueMonitor(AmazonSQS sqsClient, ScheduledExecutorService executorService, Map<EventType, Collection<Consumer>> eventHandlers, @Value("${sqs.queues}") String... queueNames) {
        this.sqsClient = sqsClient;
        this.executorService = executorService;
        monitoredQueueNames = Arrays.asList(queueNames);
        currentRegion = Regions.getCurrentRegion();
        eventHandlers.entrySet().stream().flatMap(e -> {
            return e.getValue().stream().map(v -> {
                return new AbstractMap.SimpleEntry<>(e.getKey(), v);
            });
        }).forEach(e -> {
            switch (e.getKey()) {
                case CREATED:
                    this.onCreated(e.getValue());
                    break;
                case DELETED:
                    this.onDeleted(e.getValue());
                    break;
                case MODIFIED:
                    this.onModified(e.getValue());
                    break;
            }
        });
    }

    /**
     *
     */
    public void start() {
        //20 seconds is suggested maximum. http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-long-polling.html
        Duration pollingInterval = Duration.ofSeconds(20);
        monitoredQueueNames.stream().forEach(queue -> {
            System.out.println(String.format("Starting polling queue %s in region %s.", queue, currentRegion != null ? currentRegion.getName() : currentRegion));
            final GetQueueUrlResult getQueueResult = sqsClient.getQueueUrl(queue);
            executorService.scheduleAtFixedRate(() -> {
                try {
                    ReceiveMessageRequest request = new ReceiveMessageRequest(getQueueResult.getQueueUrl());
                    request.setWaitTimeSeconds((int) pollingInterval.getSeconds());
                    Collection<Message> messages = sqsClient.receiveMessage(request).getMessages();
                    Collection<Message> handledMessages = new LinkedList<>();
                    messages.stream().forEach(message -> {
                        S3EventNotification notification = S3EventNotification.parseJson(message.getBody());
                        notification.getRecords().stream().forEach(record -> {
                            String appendedEventName = "";
                            PluginMonitorEvent event = null;
                            //S3Event has names like "s3:ObjectCreated:Put", but the record event name lacks the leading "s3:"
                            appendedEventName = "s3:" + record.getEventName();
                            if (S3Event.ObjectCreatedByPut.toString().equals(appendedEventName) ||
                                    S3Event.ObjectCreatedByPost.toString().equals(appendedEventName)) {
                                event = new PluginMonitorEvent(EventType.CREATED, record.getS3().getObject().getKey());
                            } else if (S3Event.ObjectRemovedDelete.toString().equals(appendedEventName)) {
                                event = new PluginMonitorEvent(EventType.DELETED, record.getS3().getObject().getKey());
                            }
                            if (event != null) {
                                handle(event);
                            }
                        });
                    });
                    List<DeleteMessageBatchRequestEntry> deleteEntries = messages.stream().map(message -> {
                        return new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle());
                    }).collect(Collectors.toList());
                    //Don't send an empty collection of entries to avoid EmptyBatchException.
                    if (deleteEntries.size() > 0) {
                        sqsClient.deleteMessageBatch(queue, deleteEntries);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, pollingInterval.getSeconds(), TimeUnit.SECONDS);
        });
    }

    @Bean
    public static ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(1);
    }
}