package com.indigententerprises.sqsclaimcheck.scheduling;

import com.indigententerprises.sqsclaimcheck.factories.MessageSenderFactory;
import com.indigententerprises.sqsclaimcheck.services.MessageSender;
import com.indigententerprises.messagingartifacts.AppraisalResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class QueueProducerJob implements Job {

    @Value("${queue.name}")
    private String queueName;

    private String region;
    private MessageSenderFactory messageSenderFactory;

    @Autowired
    public void setRegion(final String region) {
        this.region = region;
    }

    @Autowired
    public void setMessageSenderFactory(final MessageSenderFactory messageSenderFactory) {
        this.messageSenderFactory = messageSenderFactory;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final AppraisalResponse appraisalResponse = new AppraisalResponse();
            appraisalResponse.setItemName("BOSS frame/fork");
            appraisalResponse.setValue(300000L);
            appraisalResponse.setDateString("2024-06-12T01:22:31.437869Z[UTC]");

            final JAXBContext context = JAXBContext.newInstance(AppraisalResponse.class);
            final StringWriter writer = new StringWriter();
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(appraisalResponse, writer);

            final String document = writer.toString();
            final MessageSender messageSender = messageSenderFactory.newMessageSender(region, queueName);
            messageSender.sendMessage(document);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
