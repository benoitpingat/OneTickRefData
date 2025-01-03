package com.cmcmarkets.refdata.service;

import com.cmcmarkets.refdata.service.api.*;
import com.cmcmarkets.refdata.service.utils.OneTickTimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.cmcmarkets.refdata.service.OneTickRefData.QUERY_CONTEXT;

public class RefDataRestServer {
    private static final Logger LOG = LoggerFactory.getLogger(RefDataRestServer.class);


    private final HttpServer server;
    private final OneTickRefData refDataOneTick;


    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Swagger UI</title>
                    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.18.2/swagger-ui.css">
                    <style>
                        html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                        *, *:before, *:after { box-sizing: inherit; }
                        body { margin:0; background: #fafafa; }
                    </style>
                </head>
                <body>
                <div id="swagger-ui"></div>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.18.2/swagger-ui-bundle.js"> </script>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.18.2/swagger-ui-standalone-preset.js"> </script>
                <script>
                    window.onload = function() {
                        const ui = SwaggerUIBundle({
                            url: "/swagger/api.yaml",
                            dom_id: '#swagger-ui',
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            layout: "StandaloneLayout"
                        });
                        window.ui = ui;
                    }
                </script>
                </body>
                </html>
            """;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class OpenApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String openApiYaml;
            InputStream resourceAsStream = RefDataRestServer.class.getClassLoader().getResourceAsStream("RefDataServer.yaml");
            openApiYaml = new BufferedReader(
                    new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            exchange.sendResponseHeaders(200, openApiYaml.length());
            OutputStream os = exchange.getResponseBody();
            os.write(openApiYaml.getBytes());
            os.close();
        }
    }

    public RefDataRestServer(int httpPort, OneTickRefData refDataOneTick) {
        try {
            this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(httpPort), 0);
            this.server.createContext("/new").setHandler(this::handleNewSymbology);
            this.server.createContext("/change").setHandler(this::handleChangeSymbol);
            this.server.createContext("/delist").setHandler(this::delistSymbol);
            this.server.createContext("/corporateAction").setHandler(this::handleCorporateAction);
            this.server.createContext("/replayTransaction").setHandler(this::handleReplayTransaction);
            this.server.createContext("/LookupUUID").setHandler(this::LookupUUID);
            this.server.createContext("/getAllSymbols").setHandler(this::getAllSymbols);
            this.server.createContext("/findLatestSymbol").setHandler(this::findLatestSymbol);

            //this.server.createContext("/swagger", new DispatcherServletHttpHandler(context));
            server.createContext("/swagger", new StaticFileHandler());
            server.createContext("/swagger/api.yaml", new OpenApiHandler());
            this.refDataOneTick = refDataOneTick;
        } catch (Exception e) {
            LOG.error("Failed to set up HttpServer", e);
            throw new RuntimeException("Failed to start HttpServer", e);
        }
    }


    public void start() {
        try{
            this.server.start();
            this.refDataOneTick.start();
        } catch (Exception e) {
            LOG.error("Failed to start HttpServer", e);
            throw new RuntimeException("Failed to start HttpServer", e);
        }
    }

    public void stop()  {
        this.server.stop(0);
    }


    void getAllSymbols(HttpExchange exchange) throws IOException {
        logRequest("getAllSymbols", null);
        List<String> found =  refDataOneTick.getAllSymbols("NFR_BLUE");
        sendResponse("LookupUUID", exchange, 200, "Symbols found : " + found.size() + " First 10 are : " + found.stream()
                .limit(10)
                .collect(Collectors.joining(", ")));
    }

    void findLatestSymbol(HttpExchange exchange) throws IOException {
        logRequest("findLatestSymbol", null);
        String symbol = getExchangeBody(exchange);
        SymbolNameHistory found =  refDataOneTick.getLatestSymbologyMappingRecord(QUERY_CONTEXT, symbol);
        sendResponse("findLatestSymbol", exchange, 200, "Symbol found : " + found);
    }

    void LookupUUID(HttpExchange exchange) throws IOException {
        logRequest("LookupUUID", null);
        Map<String, String> params = RefDataRestServer.queryToMap(exchange.getRequestURI().getQuery());
        String symbol = params.get("symbol");
        List<String> found = refDataOneTick.getSymbolUUID(QUERY_CONTEXT, symbol, "CMC_LONG_SYMBOLOGY");
        if(!found.isEmpty()) {
            sendResponse("LookupUUID", exchange, 200, found.getFirst());
        } else {
            sendResponse("LookupUUID", exchange, 204, "no mapping found");
        }
    }



    void handleReplayTransaction(HttpExchange exchange) throws IOException {
        String body = getExchangeBody(exchange);
        ReplayTransaction action = readReplayTransaction(body);

        if(Objects.nonNull(action)) {
            sendResponse("handleReplayTransaction", exchange, 200, "OK");
        } else {
            sendResponse("handleReplayTransaction", exchange, 500, "Could not parse input replayTransaction :" + body);
        }
    }

    public <T> T readFromJson(String body, TypeReference<T> clazz) {
        T result = null;
        try {
            // Parse the JSON
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readValue(body, clazz);
        } catch (Exception e) {
            LOG.error("Failed to parse JSON : #" + body + "#", e);
        }

        return result;
    }

    Intention readIntentionFromRequest(HttpExchange exchange, String body) throws IOException {

        String username = Objects.nonNull(exchange.getPrincipal()) ? exchange.getPrincipal().getUsername() : "";
        String hostName = Objects.nonNull(exchange.getRemoteAddress()) ? exchange.getRemoteAddress().getHostName() : "";
        String endPoint = Objects.nonNull(exchange.getRequestURI()) ? exchange.getRequestURI().toString() : "";

        return new Intention(username,
                hostName,
                endPoint,
                body);
    }

    CorporateAction readCorporateActionFromJson(String body) {
        return readFromJson(body, new TypeReference<CorporateAction>(){});
    }

    Map<String, DelistSymbol> readDelistSymbology(String body) {
        return readFromJson(body, new TypeReference<Map<String, DelistSymbol>>(){});
    }

    Map<String, UpdateSymbol> readUpdateSymbol(String body) {
        return readFromJson(body, new TypeReference<Map<String, UpdateSymbol>>(){});
    }

    ReplayTransaction readReplayTransaction(String body) {
        return readFromJson(body, new TypeReference<ReplayTransaction>(){});
    }

    void handleCorporateAction(HttpExchange exchange) throws IOException {

        LOG.info("[handleCorporateAction] exchange is" + exchange);

        String body = getExchangeBody(exchange);

        CorporateAction action = readCorporateActionFromJson(body);
        Intention intention = readIntentionFromRequest(exchange, body);


        if(Objects.nonNull(action)) {
            LOG.info("[handleCorporateAction] will persist action" + action);
            refDataOneTick.publishRefDataCorpAction(UUID.randomUUID().toString(), intention, action);
            sendResponse("handleCorporateAction", exchange, 200, "OK");
        } else {
            sendResponse("handleCorporateAction", exchange, 500, "Could not parse input CorporateAction :" + body);
        }
    }

    private static @NotNull String getExchangeBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Utility method to parse query parameters into a map
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private void persistNewSymbol(String symbology, String symbol, Intention intention, List<String> failedMappings){

        try {
            //First check that the Symbol does not already exist
            List<String> found = refDataOneTick.getSymbolUUID(QUERY_CONTEXT, symbol, symbology);

            if (!found.isEmpty()) {
                LOG.warn("symbol already exist, skipping : " + symbol);
                failedMappings.add(symbol);
                return;
            }

            SymbolNameHistory toSave = new SymbolNameHistory();
            toSave.setRefDataUuid(OneTickTimeUtils.getRefUUID(symbology, symbol));
            toSave.setStartDateTime(OneTickTimeUtils.getNowYYYYMMDDhhmmss());
            toSave.setEndDateTime(OneTickTimeUtils.OPEN_DATE);
            toSave.setSymbol(symbol);
            refDataOneTick.publishRefDataSymbolNameHistory(toSave.getRefDataUuid(), intention, toSave);
        } catch (Exception e) {
            LOG.error("failed to process new symbol, skipping : " + symbol);
            failedMappings.add(symbol);
        }

    }

    private void persistSymbols(String symbology, List<String> symbols, Intention intention, List<String> failedMappings){
        for(String symbol : symbols) {
            persistNewSymbol(symbology, symbol, intention, failedMappings);
        }
    }

    void handleNewSymbology(HttpExchange exchange) throws IOException {
        String body = getExchangeBody(exchange);
        Map<String, Object> symbolMappings = readFromJson(body, new TypeReference<Map>(){});
        Intention intention = readIntentionFromRequest(exchange, body);

        List<String> failures = new ArrayList<>();

        for(String symbology : symbolMappings.keySet()){
            Object value = symbolMappings.get(symbology);

            if (value instanceof String) {
                persistNewSymbol(symbology, (String)value, intention, failures);
            } else if (value instanceof List) {
                persistSymbols(symbology, (List<String>)value, intention, failures);
            }
        }

        if(failures.isEmpty()) {
            sendResponse("handleNewSymbology", exchange, 200, "OK " + symbolMappings);
        } else {
            sendResponse("handleNewSymbology partial success", exchange, 206, " Failed to process " + String.join(", ", failures));
        }
    }

    void handleChangeSymbol(HttpExchange exchange) throws IOException {

        logRequest("handleChangeSymbol", null);
        String body = getExchangeBody(exchange);
        Map<String, UpdateSymbol> symbolsToUpdate = readUpdateSymbol(body);

        if(symbolsToUpdate.isEmpty()){
            sendResponse("delistSymbol", exchange, 500, "Could not parse input UpdateSymbol :" + body);
        }

        List<String> failures = new ArrayList<>();
        Intention intention = readIntentionFromRequest(exchange, body);

        for(String symbology : symbolsToUpdate.keySet()) {
            UpdateSymbol updatedSymbol = symbolsToUpdate.get(symbology);
            List<String> found = refDataOneTick.getSymbolUUID(QUERY_CONTEXT, updatedSymbol.getOldSymbol(), symbology);
            if(found.isEmpty()){
                failures.add(updatedSymbol.getOldSymbol());
                continue;
            }

            SymbolNameHistory oldSymbol = refDataOneTick.loadSymbologyMapping(QUERY_CONTEXT, found.getFirst());

            long cutOffTime;
            if(updatedSymbol.getEffectiveDate()> 0){
                cutOffTime = updatedSymbol.getEffectiveDate();
            } else {
                cutOffTime = OneTickTimeUtils.getNowYYYYMMDDhhmmss();
            }
            oldSymbol.setEndDateTime(cutOffTime);

            SymbolNameHistory newSymbol = new SymbolNameHistory(oldSymbol.getRefDataUuid(), oldSymbol.getSymbol());
            newSymbol.setStartDateTime(cutOffTime);
            newSymbol.setEndDateTime(OneTickTimeUtils.OPEN_DATE);

            refDataOneTick.publishRefDataSymbolNameHistory(oldSymbol.getRefDataUuid(), intention, oldSymbol);
            refDataOneTick.publishRefDataSymbolNameHistory(newSymbol.getRefDataUuid(), intention, newSymbol);
        }

        if(failures.isEmpty()) {
            sendResponse("handleChangeSymbol", exchange, 200, "OK");
        } else {
            sendResponse("handleChangeSymbol", exchange, 206, ",  Failed to process " + String.join(", ", failures));
        }
    }

    void delistSymbol(HttpExchange exchange) throws IOException {

        logRequest("delistSymbology", null);
        String body = getExchangeBody(exchange);

        List<String> failures = new ArrayList<>();
        Intention intention = readIntentionFromRequest(exchange, body);

        Map<String, DelistSymbol> symbolsToDelist = readDelistSymbology(body);

        if(symbolsToDelist.isEmpty()){
            sendResponse("delistSymbol", exchange, 500, "Could not parse input DelistSymbol :" + body);
        }

        for(String symbology : symbolsToDelist.keySet()) {
            DelistSymbol delistSymbol = symbolsToDelist.get(symbology);
            List<String> found = refDataOneTick.getSymbolUUID(QUERY_CONTEXT, delistSymbol.getSymbol(), symbology);
            if (found.isEmpty()) {
                failures.add(delistSymbol.getSymbol());
                continue;
            }

            SymbolNameHistory oldSymbol = refDataOneTick.loadSymbologyMapping(QUERY_CONTEXT, found.getFirst());

            long cutOffTime;
            if (delistSymbol.getEffectiveDate() > 0) {
                cutOffTime = delistSymbol.getEffectiveDate();
            } else {
                cutOffTime = OneTickTimeUtils.getNowYYYYMMDDhhmmss();
            }
            oldSymbol.setEndDateTime(cutOffTime);
            refDataOneTick.publishRefDataSymbolNameHistory(oldSymbol.getRefDataUuid(), intention, oldSymbol);
        }

        if(failures.isEmpty()) {
            sendResponse("delistSymbol", exchange, 200, "OK");
        } else {
            sendResponse("delistSymbol", exchange, 206, " Failed to process " + String.join(", ", failures));
        }
    }

    // Utility methods
    private void logRequest(String operation, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[REQUEST ").append(operation).append("]\n");
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                sb.append(e).append("\n");
            }
        }
        LOG.info(sb.toString());
    }

    private void sendResponse(String operation, HttpExchange exchange, int code, String msg) throws IOException {
        sendResponse(operation, exchange, code, msg, null);
    }

    private void sendResponse(String operation, HttpExchange exchange, int code, String msg, Map<String, String> headers) throws IOException {
        if (headers != null && !headers.isEmpty()) {
            Headers h = exchange.getResponseHeaders();
            for (Map.Entry<String, String> e : headers.entrySet()) {
                String q = exchange.getRequestURI().getPath() + "?" + exchange.getRequestURI().getQuery();
                String val = e.getValue().replaceAll("\\$QUERY\\$", q);
                h.add(e.getKey(), val);
            }
        }
        if (msg != null) {
            exchange.sendResponseHeaders(code, msg.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(msg.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(code, -1);
        }
        exchange.close();
        LOG.info("\n[RESPONSE " + operation + "] Code:" + code + "\n" + (msg == null || msg.length() < 1024 ? msg : msg.substring(0, 1024) + "\n...") + "\n");
    }

}
