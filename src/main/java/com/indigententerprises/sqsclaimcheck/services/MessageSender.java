package com.indigententerprises.sqsclaimcheck.services;

public interface MessageSender {

    public void sendMessage(final String message) throws MessageTransmissionException;
}
