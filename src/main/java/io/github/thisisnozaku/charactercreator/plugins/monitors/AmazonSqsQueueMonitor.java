package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.thisisnozaku.charactercreator.data.access.AmazonS3Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private static final Logger logger = LoggerFactory.getLogger(AmazonSqsQueueMonitor.class);
    private final AmazonSQS sqsClient;
    private final ScheduledExecutorService executorService;
    private final List<String> monitoredQueueNames;
    private final Region currentRegion;

    public AmazonSqsQueueMonitor(AmazonSQS sqsClient, ScheduledExecutorService executorService, @Value("${sqs.queues}") String... queueNames) {
        this.sqsClient = sqsClient;
        this.executorService = executorService;
        monitoredQueueNames = Arrays.asList(queueNames);
        currentRegion = Regions.getCurrentRegion();
    }

    /**
     *
     */
    @PostConstruct
    public void start() {
        logger.info("Begun polling queue for plugin events.");
        //20 seconds is suggested maximum. http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-long-polling.html
        Duration pollingInterval = Duration.ofSeconds(20);
        monitoredQueueNames.stream().forEach(queue -> {
            logger.info(String.format("Starting polling queue %s in region %s.", queue, currentRegion != null ? currentRegion.getName() : currentRegion));
            final GetQueueUrlResult getQueueResult = sqsClient.getQueueUrl(queue);
            executorService.scheduleAtFixedRate(() -> {
                try {
                    ReceiveMessageRequest request = new ReceiveMessageRequest(getQueueResult.getQueueUrl());
                    request.setWaitTimeSeconds((int) pollingInterval.getSeconds());
                    Collection<Message> messages = sqsClient.receiveMessage(request).getMessages();
                    messages.stream().forEach(message -> {
                        S3EventNotification notification = S3EventNotification.parseJson(message.getBody());
                        Optional.ofNullable(notification.getRecords()).ifPresent(records -> {
                                records.stream().forEach(record -> {
                            PluginMonitorEvent event = null;
                            //S3Event has names like "s3:ObjectCreated:Put", but the record event name lacks the leading "s3:"
                            String appendedEventName = "s3:" + record.getEventName();
                            if (S3Event.ObjectCreatedByPut.toString().equals(appendedEventName) ||
                                    S3Event.ObjectCreatedByPost.toString().equals(appendedEventName)) {
                                event = new PluginMonitorEvent(EventType.CREATED, record.getS3().getObject().getKey());
                            } else if (S3Event.ObjectRemovedDelete.toString().equals(appendedEventName)) {
                                event = new PluginMonitorEvent(EventType.DELETED, record.getS3().getObject().getKey());
                            }
                            if (event != null) {
                                handle(event);
                            } else {
                                logger.warn("An S3 event {} wasn't handled", appendedEventName);
                            }
                        });
                    });
                    });
                    List<DeleteMessageBatchRequestEntry> deleteEntries = messages.stream().map(message -> {
                        return new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle());
                    }).collect(Collectors.toList());
                    //Don't send an empty collection of entries to avoid EmptyBatchException.
                    if (deleteEntries.size() > 0) {
                        logger.info("Notifying SQS that {} messages were consumed", messages.size());
                        sqsClient.deleteMessageBatch(queue, deleteEntries);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, pollingInterval.getSeconds(), TimeUnit.SECONDS);
        });
    }

        @Bean
        public static ScheduledExecutorService executorService () {
            return Executors.newScheduledThreadPool(1);
        }
    }