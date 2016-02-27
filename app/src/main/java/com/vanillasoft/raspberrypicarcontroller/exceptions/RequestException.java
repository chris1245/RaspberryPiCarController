package com.vanillasoft.raspberrypicarcontroller.exceptions;

/**
 * Created by Christian on 22/02/2016.
 */
public class RequestException extends Exception {

    public RequestException(final String message) {
        super(message);
    }

    public RequestException(final Throwable throwable) {
        super(throwable);
    }

    public RequestException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
