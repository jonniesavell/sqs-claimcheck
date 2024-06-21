package com.indigententerprises.sqsclaimcheck.factories;

import com.indigententerprises.components.ObjectStorageComponent;
import com.indigententerprises.factories.ObjectStoreFactory;
import com.indigententerprises.services.common.SystemException;
import com.indigententerprises.services.objects.IObjectService;
import com.indigententerprises.sqsclaimcheck.components.MessageSender;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.util.UUID;

/**
 * @author jonnie savell
 */
public class MessageSenderFactory {

    private final String bucketName;

    public MessageSenderFactory(final String bucketName) {
        this.bucketName = bucketName;
    }

    public com.indigententerprises.sqsclaimcheck.services.MessageSender
            newMessageSender(final String region, final String queueName) {
        try {
            final ObjectStorageComponent objectStorageComponent =
                    ObjectStoreFactory.createObjectStorageComponent(this.bucketName);
            final IObjectService objectService = objectStorageComponent.getObjectService();
            final SqsClient sqsClient = SqsClient.builder()
                    .region(Region.of(region))
                    .build();
            final GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();
            final String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            final String messageGroupId = UUID.randomUUID().toString();
            return new MessageSender(sqsClient, objectService, queueUrl, messageGroupId);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
