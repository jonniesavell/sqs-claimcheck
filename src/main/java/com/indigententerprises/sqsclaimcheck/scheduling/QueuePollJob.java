package com.indigententerprises.sqsclaimcheck.scheduling;

import com.indigententerprises.messagingartifacts.ClaimCheck;
import com.indigententerprises.services.common.SystemException;
import com.indigententerprises.services.objects.IObjectService;
import com.indigententerprises.domain.objects.Handle;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.List;


@Service
public class QueuePollJob implements Job {

    private String queueUrl;
    private SqsClient sqsClient;
    private IObjectService objectService;

    @Autowired
    public void setQueueUrl(final String queueUrl) {
        this.queueUrl = queueUrl;
    }

    @Autowired
    public void setSqsClient(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Autowired
    public void setObjectService(final IObjectService objectService) {
        this.objectService = objectService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final JAXBContext context = JAXBContext.newInstance(ClaimCheck.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .build();
            final List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            for (final Message message : messages) {
                final String messageBody = message.body();
                final StringReader reader = new StringReader(messageBody);
                final ClaimCheck claimCheck = (ClaimCheck) unmarshaller.unmarshal(reader);
                final Handle handle = new Handle(claimCheck.getHandle());
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                try {
                    objectService.retrieveObject(handle, byteArrayOutputStream);
                    final String document = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                    System.out.println(document);
                } finally {
                    byteArrayOutputStream.close();
                }
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
