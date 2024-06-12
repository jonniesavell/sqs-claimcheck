package com.indigententerprises.sqsclaimcheck.services;

public class MessageTransmissionException extends RuntimeException {

    public MessageTransmissionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MessageTransmissionException(final Throwable cause) {
        this("", cause);
    }
}
