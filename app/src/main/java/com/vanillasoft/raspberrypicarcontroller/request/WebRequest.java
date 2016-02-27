package com.vanillasoft.raspberrypicarcontroller.request;

import com.vanillasoft.raspberrypicarcontroller.exceptions.RequestException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Christian on 22/02/2016.
 */
public class WebRequest {
    private String baseUrl;
    public WebRequest(final String baseUrlParameter) {
        this.baseUrl = baseUrlParameter;
    }

    public final void sendPostRequest(final String controllerUrl) throws RequestException {
        final String fullControllerUrl = this.baseUrl + controllerUrl;
        try {
            final URL url = new URL(fullControllerUrl);
            final HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setDoOutput(true);


        } catch (IOException ioException) {
            throw new RequestException("No se pudo establecer la conexión con el servidor",
                    ioException);
        }
    }
}
