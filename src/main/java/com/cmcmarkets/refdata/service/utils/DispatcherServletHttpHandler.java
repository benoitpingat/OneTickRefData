package com.cmcmarkets.refdata.service.utils;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.ServletException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.IOException;

public class DispatcherServletHttpHandler implements HttpHandler {

    private final DispatcherServlet dispatcherServlet;

    public DispatcherServletHttpHandler(AnnotationConfigWebApplicationContext context) {
        this.dispatcherServlet = new DispatcherServlet(context);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpServletResponseAdapter responseAdapter  = new HttpServletResponseAdapter(exchange);
        HttpServletRequestAdapter requestAdapter = new HttpServletRequestAdapter(exchange);
        try {
            dispatcherServlet.service(requestAdapter, responseAdapter);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
