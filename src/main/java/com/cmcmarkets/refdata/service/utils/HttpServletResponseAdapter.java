package com.cmcmarkets.refdata.service.utils;

import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class HttpServletResponseAdapter extends HttpServletResponseWrapper {

    private final HttpExchange exchange;
    private final OutputStream outputStream;

    public HttpServletResponseAdapter(HttpExchange exchange) {
        super(null);
        this.exchange = exchange;
        this.outputStream = exchange.getResponseBody();
    }

    @Override
    public void setStatus(int sc) {
        try {
            exchange.sendResponseHeaders(sc, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(outputStream);
    }

    // Implement other methods as needed
}
