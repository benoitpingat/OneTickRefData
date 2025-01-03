package com.cmcmarkets.refdata.service;

import com.cmcmarkets.prophet.common.onetick.OneTickFramework;
import com.cmcmarkets.refdata.service.api.SymbolNameHistory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RefDataRestServerTest {

    final private OneTickRefData refDataMock = Mockito.mock(OneTickRefData.class);
    final private RefDataRestServer server = new RefDataRestServer(8080, refDataMock);
    static MockedStatic<OneTickFramework> mockedStatic = mockStatic(OneTickFramework.class);

    @Captor
    ArgumentCaptor<SymbolNameHistory> symbolCaptor = ArgumentCaptor.forClass(SymbolNameHistory.class);


    @BeforeEach
    void setUp() {
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testHandleCorporateActionBadJson() throws IOException, URISyntaxException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\"type\":\"DIVIDEND\",\"amount\":100}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        initialiseIncomingHttpMockCall(exchangeMock, inputStream);

        server.handleCorporateAction(exchangeMock);

        verify(exchangeMock).sendResponseHeaders(eq(500), anyLong());
    }

    private static void initialiseIncomingHttpMockCall(HttpExchange exchangeMock, InputStream inputStream) throws URISyntaxException {
        when(exchangeMock.getPrincipal()).thenReturn(new HttpPrincipal("test", "realm"));
        when(exchangeMock.getRemoteAddress()).thenReturn(new InetSocketAddress(8080));
        when(exchangeMock.getRequestURI()).thenReturn(new URI("callingURI"));
        when(exchangeMock.getRequestBody()).thenReturn(inputStream);
        when(exchangeMock.getResponseBody()).thenReturn(OutputStream.nullOutputStream());
    }

    @Test
    void testHandleNewSymbology() throws IOException, URISyntaxException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"AGG\": \"JAPAN225.SPOT.JPY.AGG\",\n" +
                "  \"CMC\": \"JAPAN225.SPOT.JPY.CMC\",\n" +
                "  \"CLEAN\": [\"CLEAN1\", \"CLEAN2\"]\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        initialiseIncomingHttpMockCall(exchangeMock, inputStream);

        server.handleNewSymbology(exchangeMock);

        verify(refDataMock, times(4)).publishRefDataSymbolNameHistory(anyString(), any(), any());

        verify(exchangeMock).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void testHandleNewSymbologyPartial() throws IOException, URISyntaxException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"AGG\": \"JAPAN225.SPOT.JPY.AGG\",\n" +
                "  \"CMC\": \"JAPAN225.SPOT.JPY.CMC\",\n" +
                "  \"CLEAN\": [\"CLEAN1\", \"CLEAN2\"]\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        initialiseIncomingHttpMockCall(exchangeMock, inputStream);

        when(refDataMock.getSymbolUUID(OneTickRefData.QUERY_CONTEXT, "JAPAN225.SPOT.JPY.CMC", "CMC")).thenReturn(List.of("JAPAN225.SPOT.JPY.CMC"));

        server.handleNewSymbology(exchangeMock);

        verify(refDataMock, times(3)).publishRefDataSymbolNameHistory(anyString(), any(), any());

        verify(exchangeMock).sendResponseHeaders(eq(206), anyLong());
    }

    @Test
    void testHandleNewSymbologyErrors() throws IOException, URISyntaxException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"AGG\": \"JAPAN225.SPOT.JPY.AGG\",\n" +
                "  \"CMC\": \"JAPAN225.SPOT.JPY.CMC\",\n" +
                "  \"CLEAN\": [\"CLEAN1\", \"CLEAN2\"]\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        initialiseIncomingHttpMockCall(exchangeMock, inputStream);

        when(refDataMock.getSymbolUUID(OneTickRefData.QUERY_CONTEXT, "JAPAN225.SPOT.JPY.CMC", "CMC")).thenThrow(new RuntimeException("Catastrophic failure !"));
        when(refDataMock.getSymbolUUID(OneTickRefData.QUERY_CONTEXT, "JAPAN225.SPOT.JPY.AGG", "AGG")).thenThrow(new RuntimeException("Catastrophic failure !"));


        server.handleNewSymbology(exchangeMock);

        verify(refDataMock, times(2)).publishRefDataSymbolNameHistory(anyString(), any(), any());

        verify(exchangeMock).sendResponseHeaders(eq(206), anyLong());
    }


    @Test
    void testHandleCorporateAction() throws IOException, URISyntaxException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"PMS_CODE\": \"X-ABCD\",\n" +
                "  \"EFFECTIVE_DATE\": 1715333656,\n" +
                "  \"MULTIPLICATIVE_ADJUSTMENT\": 0.1,\n" +
                "  \"ADDITIVE_ADJUSTMENT\": 25,\n" +
                "  \"ADJUSTMENT_TYPE_NAME\": \"SPLIT\"\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        initialiseIncomingHttpMockCall(exchangeMock, inputStream);

        server.handleCorporateAction(exchangeMock);

        verify(refDataMock, times(1)).publishRefDataCorpAction(anyString(), any(), any());

        verify(exchangeMock).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void testHandleDeList() throws IOException {

        final String SYMBOL_TO_DELIST =  "JAPAN225.SPOT.JPY.CMC";
        final String SYMBOLOGY = "CMC";
        final long TERMINATION_TIME = 1715333656L;

        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"CLEAN\": {\n" +
                "    \"SYMBOL\": \"CLEAN1.RDF\",\n" +
                "    \"EFFECTIVE_DATE\": 1715333656\n" +
                "  },\n" +
                "  \"" + SYMBOLOGY + "\": {\n" +
                "    \"SYMBOL\": \"" + SYMBOL_TO_DELIST + "\",\n" +
                "    \"EFFECTIVE_DATE\": " + TERMINATION_TIME + " \n" +
                "  }\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        when(exchangeMock.getRequestBody()).thenReturn(inputStream);
        when(exchangeMock.getResponseBody()).thenReturn(OutputStream.nullOutputStream());

        when(refDataMock.getSymbolUUID(OneTickRefData.QUERY_CONTEXT, SYMBOL_TO_DELIST, SYMBOLOGY)).thenReturn(List.of(SYMBOL_TO_DELIST));
        when(refDataMock.loadSymbologyMapping(OneTickRefData.QUERY_CONTEXT, SYMBOL_TO_DELIST)).thenReturn(new SymbolNameHistory(SYMBOL_TO_DELIST, SYMBOL_TO_DELIST));

        server.delistSymbol(exchangeMock);

        verify(refDataMock, times(1)).publishRefDataSymbolNameHistory(anyString(), any(), symbolCaptor.capture());

        assertEquals(TERMINATION_TIME, symbolCaptor.getValue().getEndDateTime());

        verify(exchangeMock).sendResponseHeaders(eq(206), anyLong());
    }

    @Test
    void testHandleChangeSymbol() throws IOException {
        final String SYMBOL_TO_UPDATE =  "JAPAN225.SPOT.JPY.CMC";
        final String SYMBOLOGY = "AGG";
        final long UPDATE_TIME = 1715333656;
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"" + SYMBOLOGY + "\": {\n" +
                "    \"OLD_SYMBOL\": \"" + SYMBOL_TO_UPDATE + "\",\n" +
                "    \"NEW_SYMBOL\": \"APAN225.SPOT.JPY.AGGGGG\",\n" +
                "    \"EFFECTIVE_DATE\": " + UPDATE_TIME + "\n" +
                "  }\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        when(exchangeMock.getRequestBody()).thenReturn(inputStream);
        when(exchangeMock.getResponseBody()).thenReturn(OutputStream.nullOutputStream());

        when(refDataMock.getSymbolUUID(OneTickRefData.QUERY_CONTEXT, SYMBOL_TO_UPDATE, SYMBOLOGY)).thenReturn(List.of(SYMBOL_TO_UPDATE));
        when(refDataMock.loadSymbologyMapping(OneTickRefData.QUERY_CONTEXT, SYMBOL_TO_UPDATE)).thenReturn(new SymbolNameHistory(SYMBOL_TO_UPDATE, SYMBOL_TO_UPDATE));

        server.handleChangeSymbol(exchangeMock);

        verify(refDataMock, times(2)).publishRefDataSymbolNameHistory(anyString(), any(), symbolCaptor.capture());

        List<SymbolNameHistory> capturedValues = symbolCaptor.getAllValues();
        assertEquals(UPDATE_TIME, capturedValues.get(0).getEndDateTime());
        assertEquals(UPDATE_TIME, capturedValues.get(1).getStartDateTime());

        verify(exchangeMock).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void testHandleReplayTransaction() throws IOException {
        HttpExchange exchangeMock = mock(HttpExchange.class);
        String json = "{\n" +
                "  \"transaction_id\": 123\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        when(exchangeMock.getRequestBody()).thenReturn(inputStream);
        when(exchangeMock.getResponseBody()).thenReturn(OutputStream.nullOutputStream());

        server.handleReplayTransaction(exchangeMock);

        verify(exchangeMock).sendResponseHeaders(eq(200), anyLong());
    }
}