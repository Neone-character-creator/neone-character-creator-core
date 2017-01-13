package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.ListenableFutureTask;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Listens for events from one or more Amazon Simple Queue Service queues.
 * Created by Damien on 12/3/2016.
 */
@Service
@Profile("aws")
public class S3EventSqsQueueListener extends PluginMonitorAdapter {
    private final AmazonSQSClient sqsClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    private ExecutorService executorService;
    private Collection<Consumer<Message>> callbacks = new LinkedList<>();

    public S3EventSqsQueueListener(AmazonSQSClient sqsClient, ScheduledExecutorService executorService, String... queueNames) {
        this.sqsClient = sqsClient;
        this.executorService = executorService;
        Arrays.asList(queueNames).stream().forEach(q->{
            executorService.scheduleAtFixedRate(()->{
                ReceiveMessageRequest request = new ReceiveMessageRequest(q);
                Collection<Message> messages = sqsClient.receiveMessage(request).getMessages();
                messages.stream().forEach(message -> {
                    callbacks.stream().forEach(callable -> {
                        callable.accept(message);
                    });
                });
            }, 0, 1000L, TimeUnit.MILLISECONDS);
        });
    }

    public void addCallback(final Consumer<Message> callback){
        callbacks.add(callback);
    }
}