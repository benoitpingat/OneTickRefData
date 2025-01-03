package com.cmcmarkets.refdata.service.utils;


import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.io.OutputStream;

public class HttpExchangeServletAdapter extends HttpServlet {

    private final HttpExchange exchange;

    public HttpExchangeServletAdapter(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Adapt HttpExchange to HttpServletRequest and HttpServletResponse
        // This is a simplified example
        String response = "Hello, World!";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
