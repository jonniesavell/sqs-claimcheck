# sqs-claimcheck

this is an implementation of the claim-check pattern using the AWS Java SDK for SQS.

this project provides a simulation of a queue based consumer and queue based producer. the important part is that the actual messages are stored in AWS S3, allowing messages to be larger than SQS' 256K limit.

this project could easily be broken apart into two smaller projects, one which writes to the queue and another which reads from the queue.
