package com.cmcmarkets.refdata.service.utils;

import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class HttpServletRequestAdapter extends HttpServletRequestWrapper {

    private final HttpExchange exchange;

    public HttpServletRequestAdapter(HttpExchange exchange) {
        super(null);
        this.exchange = exchange;
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getRequestURI() {
        return exchange.getRequestURI().toString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new Enumeration<>() {
            private final List<String> names = List.copyOf(exchange.getRequestHeaders().keySet());
            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < names.size();
            }

            @Override
            public String nextElement() {
                return names.get(index++);
            }
        };
    }

    @Override
    public String getHeader(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    // Implement other methods as needed
}
