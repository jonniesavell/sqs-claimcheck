package com.indigententerprises.sqsclaimcheck.configuration;

import com.indigententerprises.components.ObjectStorageComponent;
import com.indigententerprises.factories.ObjectStoreFactory;
import com.indigententerprises.services.common.SystemException;
import com.indigententerprises.services.objects.IObjectService;
import com.indigententerprises.sqsclaimcheck.scheduling.QueuePollJob;
import com.indigententerprises.sqsclaimcheck.scheduling.QueueProducerJob;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

@Configuration
public class SchedulingConfiguration {

    @Value("${queue.name}")
    private String queueName;

    @Value("${bucket.name}")
    private String bucketName;

    @Bean
    public String region() {
        String result = System.getenv("AWS_REGION");

        if (result == null) {
            result = System.getenv("AWS_DEFAULT_REGION");
        }

        return result;
    }

    @Bean
    public SqsClient sqsClient() {
        final SqsClient result = SqsClient.builder()
                .region(Region.of(region()))
                .build();
        return result;
    }

    @Bean
    public String queueUrl(final SqsClient sqsClient) {
        final GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        final String result = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
        return result;
    }

    @Bean
    public IObjectService objectService() throws SystemException {
        final ObjectStorageComponent objectStorageComponent =
                ObjectStoreFactory.createObjectStorageComponent(this.bucketName);
        final IObjectService result = objectStorageComponent.getObjectService();
        return result;
    }

    @Bean
    public JobDetail queuePollJobDetail() {
        return JobBuilder.newJob(QueuePollJob.class)
                .withIdentity("queue-poll-job")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger queuePollJobTrigger() {
        final SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(3)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(queuePollJobDetail())
                .withIdentity("queue-poll-trigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail queueProducerJobDetail() {
        return JobBuilder.newJob(QueueProducerJob.class)
                .withIdentity("queue-producer-job")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger queueProducerJobTrigger() {
        final SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(3)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(queueProducerJobDetail())
                .withIdentity("queue-producer-trigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
