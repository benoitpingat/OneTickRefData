package com.cmcmarkets.refdata.service;

import com.cmcmarkets.prophet.messaging.common.imagecache.ValueEntryList;
import com.cmcmarkets.refdata.service.api.CorporateAction;
import com.cmcmarkets.refdata.service.api.Intention;
import com.cmcmarkets.refdata.service.api.SymbolNameHistory;
import com.cmcmarkets.refdata.service.callback.CallbackAdaptor;
import com.cmcmarkets.refdata.service.callback.SymbolMappingsCallback;
import com.cmcmarkets.refdata.service.callback.SymbolNameHistoryCallback;
import com.omd.jomd.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import com.cmcmarkets.prophet.common.onetick.OneTickRecorder;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OneTickRefDataTest {

    @Mock
    private OneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>> refDataAuditRecorder;

    //Loads of mocks that rely on OneTick object involving static JNI instances we want to bypass ...
    otq_parameters_t otq_parameters_tMock = mock(otq_parameters_t.class);
    MockedConstruction<OtqQuery> otQQueryMock = mockConstruction(OtqQuery.class,(mock,context)-> {
        when(mock.get_otq_parameters()).thenReturn(otq_parameters_tMock);
    });
    MockedConstruction<Connection> connectionMock = mockConstruction(Connection.class,(mock,context)-> {
    });
    MockedConstruction<StringCollection> stringCollection = mockConstruction(StringCollection.class,(mock,context)-> {
    });
    MockedConstruction<QueryConcurrency> stringQueryConcurrency = mockConstruction(QueryConcurrency.class,(mock,context)-> {
    });
    MockedConstruction<timeval_t> timevalMock = mockConstruction(timeval_t.class,(mock,context)-> {
    });
    MockedConstruction<JavaOutputCallbackBase> javaOutputCallbackBase = mockConstruction(JavaOutputCallbackBase.class,(mock,context)-> {
    });
    MockedConstruction<JavaOutputCallback> javaOutputCallback = mockConstruction(JavaOutputCallback.class,(mock,context)-> {
    });
    MockedConstruction<SymbolSpecs> symbolSpecsMock = mockConstruction(SymbolSpecs.class,(mock,context)-> {
    });
    MockedConstruction<SymbolSpec> symbolSpecMock = mockConstruction(SymbolSpec.class,(mock,context)-> {
    });
    MockedConstruction<otq_parameters_t> otq_parameters_tGenericMock = mockConstruction(otq_parameters_t.class,(mock,context)-> {
    });


    private OneTickRefData underTestOneTickRefData;



    @BeforeEach
    void setup() throws IOException {
        Resource res = mock(Resource.class);
        when(res.getInputStream()).thenReturn(InputStream.nullInputStream());
        Resource res2 = mock(Resource.class);
        when(res2.getInputStream()).thenReturn(InputStream.nullInputStream());

        underTestOneTickRefData = new OneTickRefData(res, res2 , refDataAuditRecorder, mock(timeval_t.class));
        reset(refDataAuditRecorder);
    }

    @AfterEach
    void tearDown() {
        otQQueryMock.close();
        connectionMock.close();
        stringCollection.close();
        stringQueryConcurrency.close();
        timevalMock.close();
        javaOutputCallbackBase.close();
        javaOutputCallback.close();
        symbolSpecsMock.close();
        symbolSpecMock.close();
        otq_parameters_tGenericMock.close();
    }

    @Test
    public void testOneTickRefDataSymbolWrite() {
        underTestOneTickRefData.publishRefDataSymbolNameHistory("Test", new Intention("test", "localhost", "endpoint", "testBody"),
                new SymbolNameHistory());
        verify(refDataAuditRecorder, only()).writeToOnetick(any(), any(), anyBoolean());

    }

    @Test
    public void testOneTickCorporateActionWrite() {

        underTestOneTickRefData.publishRefDataCorpAction("Test", new Intention("test", "localhost", "endpoint", "testBody"),
                new CorporateAction());
        verify(refDataAuditRecorder, only()).writeToOnetick(any(), any(), anyBoolean());
    }

    @Test
    public void testLoadSymbologyMapping() {

        //we'll capture the SymbolNameHistoryCallback instance and will simulate its population from OneTick
        final SymbolNameHistoryCallback[] capture = new SymbolNameHistoryCallback[1];

        SymbolNameHistory expected = new SymbolNameHistory("RefUUID", "FOUND!");

        MockedConstruction<CallbackAdaptor> javaCallbackAdaptor = mockConstruction(CallbackAdaptor.class,(mock,context)-> {
            capture[0] = (SymbolNameHistoryCallback)context.arguments().getFirst();
            capture[0].getSymbolMappings().add(new SymbolNameHistory("RefUUID", "FOUND!"));
        });


        MockedStatic<RequestGroup> mockedStatic = null;
        try {
            mockedStatic = mockStatic(RequestGroup.class);
            // Define the behavior of the static method
            mockedStatic.when(() -> RequestGroup.process_otq_file(any(OtqQuery.class), any(CallbackAdaptor.class), any(Connection.class)))
                    .thenAnswer(invocation -> {
                        return null;
                    });


            SymbolNameHistory found = underTestOneTickRefData.loadSymbologyMapping("Test", "RefUUID");
            //Check that callBack has been reset
            Assertions.assertEquals(0, capture[0].getSymbolMappings().size());
            //And that the returned value is the one provided within the mocked callback
            Assertions.assertEquals(expected.getSymbol(), found.getSymbol());
            Assertions.assertEquals(expected.getRefDataUuid(), found.getRefDataUuid());
        }
        finally{
            mockedStatic.close();
            javaCallbackAdaptor.close();
        }

    }

    @Test
    public void testLoadSymbologyMappingNotFound() {

        SymbolNameHistory expected = new SymbolNameHistory("RefUUID", "FOUND!");

        MockedConstruction<CallbackAdaptor> javaCallbackAdaptor = mockConstruction(CallbackAdaptor.class,(mock,context)-> {
        });


        MockedStatic<RequestGroup> mockedStatic = null;
        try {
            mockedStatic = mockStatic(RequestGroup.class);
            // Define the behavior of the static method
            mockedStatic.when(() -> RequestGroup.process_otq_file(any(OtqQuery.class), any(CallbackAdaptor.class), any(Connection.class)))
                    .thenAnswer(invocation -> {
                        return null;
                    });


            SymbolNameHistory found = underTestOneTickRefData.loadSymbologyMapping("Test", "RefUUID");
            //And that the returned value is the one provided within the mocked callback
            Assertions.assertNull(found);
        }
        finally{
            mockedStatic.close();
        }

    }

    @Test
    public void testgetSymbolUID() {

        SymbolNameHistory expected = new SymbolNameHistory("RefUUID", "FOUND!");



        MockedConstruction<CallbackAdaptor> javaCallbackAdaptor = mockConstruction(CallbackAdaptor.class,(mock,context)-> {
        });

        MockedStatic<RequestGroup> mockedStatic = null;
        try {
            mockedStatic = mockStatic(RequestGroup.class);
            // Define the behavior of the static method
            mockedStatic.when(() -> RequestGroup.process_otq_file(any(OtqQuery.class), any(CallbackAdaptor.class), any(Connection.class)))
                    .thenAnswer(invocation -> {
                        return null;
                    });


            List<String> found = underTestOneTickRefData.getSymbolUUID("context", "symbol", "symbology");
            //And that the returned value is the one provided within the mocked callback
            Assertions.assertTrue(found.isEmpty());
        }
        finally{
            mockedStatic.close();
            javaCallbackAdaptor.close();
        }


    }



}