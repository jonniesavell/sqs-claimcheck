package com.indigententerprises.sqsclaimcheck.components;

import com.indigententerprises.sqsclaimcheck.services.MessageTransmissionException;
import com.indigententerprises.services.common.SystemException;
import com.indigententerprises.services.objects.IObjectService;
import com.indigententerprises.messagingartifacts.ClaimCheck;
import com.indigententerprises.domain.objects.Handle;
import com.indigententerprises.domain.objects.HandleAndArnPair;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

/**
 * @author jonniesavell
 */
public class MessageSender implements com.indigententerprises.sqsclaimcheck.services.MessageSender {

    private final SqsClient sqsClient;
    private final IObjectService objectService;
    private final String queueUrl;
    private final String messageGroupId;

    public MessageSender(
            final SqsClient sqsClient,
            final IObjectService objectService,
            final String queueUrl,
            final String messageGroupId
    ) {
        this.sqsClient = sqsClient;
        this.objectService = objectService;
        this.queueUrl = queueUrl;
        this.messageGroupId = messageGroupId;
    }

    @Override
    public void sendMessage(final String message) throws MessageTransmissionException {
        try {
            final byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            final int size = bytes.length;

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                final HandleAndArnPair handleAndArnPair =
                        objectService.storeObjectAndMetaData(
                                inputStream,
                                size,
                                Collections.emptyMap()
                        );

                final ClaimCheck claimCheck = new ClaimCheck();
                claimCheck.setHandle(handleAndArnPair.handle.identifier);
                claimCheck.setUrl(handleAndArnPair.arn);

                final JAXBContext context = JAXBContext.newInstance(ClaimCheck.class);
                final StringWriter writer = new StringWriter();
                final Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(claimCheck, writer);

                final String xmlOutput = writer.toString();
                final SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(xmlOutput)
                        .delaySeconds(0)
                        .messageGroupId(messageGroupId)
                        .messageDeduplicationId(UUID.randomUUID().toString())
                        .build();
                sqsClient.sendMessage(sendMsgRequest);
            }
        } catch (SqsException e) {
            throw new MessageTransmissionException(e);
        } catch (IOException e) {
            throw new MessageTransmissionException(e);
        } catch (SystemException e) {
            throw new MessageTransmissionException(e);
        } catch (JAXBException e) {
            throw new MessageTransmissionException(e);
        }
    }

    @Override
    public void close() throws SdkClientException {
        sqsClient.close();
    }
}
