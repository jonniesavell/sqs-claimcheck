package com.indigententerprises.sqsclaimcheck.services;

public interface MessageSender extends AutoCloseable {

    public void sendMessage(final String message) throws MessageTransmissionException;
}
