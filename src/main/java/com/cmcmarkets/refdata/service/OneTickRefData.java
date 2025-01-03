package com.cmcmarkets.refdata.service;

import com.cmcmarkets.prophet.common.StartStoppable;
import com.cmcmarkets.prophet.common.onetick.OneTickFramework;
import com.cmcmarkets.prophet.common.onetick.OneTickRecorder;
import com.cmcmarkets.prophet.messaging.common.imagecache.ValueEntryList;
import com.cmcmarkets.refdata.service.api.CorporateAction;
import com.cmcmarkets.refdata.service.api.Intention;
import com.cmcmarkets.refdata.service.api.SymbolNameHistory;
import com.cmcmarkets.refdata.service.callback.CallbackAdaptor;
import com.cmcmarkets.refdata.service.callback.SymbolListCallback;
import com.cmcmarkets.refdata.service.callback.SymbolMappingsCallback;
import com.cmcmarkets.refdata.service.callback.SymbolNameHistoryCallback;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;
import com.cmcmarkets.refdata.service.utils.CorporateAction2ValueMapper;
import com.cmcmarkets.refdata.service.utils.Intention2ValueMapper;
import com.cmcmarkets.refdata.service.utils.SymbolHistory2ValueMapper;
import com.omd.jomd.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OneTickRefData implements StartStoppable {
    private static final Logger LOG = LoggerFactory.getLogger(OneTickRefData.class);

    public static final String QUERY_CONTEXT = "NFR_BLUE";

    private static final int MAX_NUM_CORES = 10;

    private final String uuidLookUpQuery;
    private final String quoteHistoryQueries;
    private final Intention2ValueMapper intention2ValueMapper = new Intention2ValueMapper(null);
    private final CorporateAction2ValueMapper corporateAction2ValueMapper = new CorporateAction2ValueMapper(null);
    private final SymbolHistory2ValueMapper symbolHistory2ValueMapper = new SymbolHistory2ValueMapper(null);

    private final ThreadLocal<ValueEntryList<ReferenceDataAudit>> refDataValueEntryList;

    private final OneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>> refDataAuditRecorder;

    DateTimeFormatter yyyyMMddHHmmssDateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final timeval_t currentTimestamp;

    public OneTickRefData(Resource uuidLookUpQuery,
                          Resource quoteHistoryQueries,
                          OneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>> refDataAuditRecorder,
                          timeval_t currentTimestamp) throws IOException {
        try (InputStream in = uuidLookUpQuery.getInputStream()) {
            this.uuidLookUpQuery = IOUtils.toString(in);
        }
        try (InputStream in = quoteHistoryQueries.getInputStream()) {
            this.quoteHistoryQueries = IOUtils.toString(in);
        }

        this.refDataAuditRecorder = refDataAuditRecorder;
        this.refDataValueEntryList = ThreadLocal.withInitial(ValueEntryList::getInstance);
        this.currentTimestamp = currentTimestamp;
    }


    @Override
    public void start() throws Exception {

        startOneTick();
        refDataAuditRecorder.start();
    }

    @Override
    public void stop() {
        refDataAuditRecorder.stop();
    }

    public static synchronized void startOneTick() {
        /*if (!OneTickFramework.isStarted()) {
            OneTickFramework.start();
        }*/
    }

    public timeval_t getNow(){
        currentTimestamp.set_msec(System.currentTimeMillis());
        return currentTimestamp;
    }


    public SymbolNameHistory getLastSymbolEntry(String context, OtqQuery query) {

        try {
            QueryConcurrency queryConcurrency = new QueryConcurrency();
            queryConcurrency.set_max_concurrency(MAX_NUM_CORES);
            query.set_query_concurrency(queryConcurrency);

            LOG.info("Calling OTQ Query on Context " + context + " params size = " + query.get_otq_parameters().size() + " params =" + query.get_otq_parameters());

            SymbolNameHistoryCallback symbolMappingsCallback = new SymbolNameHistoryCallback();

            RequestGroup.process_otq_file(query, new CallbackAdaptor(symbolMappingsCallback), connection(context));
            List<SymbolNameHistory> symbols = new ArrayList<>(symbolMappingsCallback.getSymbolMappings());
            symbolMappingsCallback.clear();
            LOG.info("[OTQ_END] get_mappingl_symbols: Queried " + symbols.size() + " symbols ");
            return !symbols.isEmpty() ? symbols.getFirst() : null;
        } catch (Exception e) {
            LOG.error("getLastSymbolEntry failed ", e);
            throw new RuntimeException(e);
        }
    }


    public List<String> getSymbolUUID(String context, OtqQuery query) {

        try {
            QueryConcurrency queryConcurrency = new QueryConcurrency();
            queryConcurrency.set_max_concurrency(MAX_NUM_CORES);
            query.set_query_concurrency(queryConcurrency);

            LOG.info("Calling OTQ Query on Context " + context + " params size = " + query.get_otq_parameters().size() + " params =" + query.get_otq_parameters());

            SymbolMappingsCallback symbolMappingsCallback = new SymbolMappingsCallback();

            RequestGroup.process_otq_file(query, new CallbackAdaptor(symbolMappingsCallback), connection(context));
            List<String> symbols = new ArrayList<>(symbolMappingsCallback.getSymbolMappings());
            symbolMappingsCallback.clear();
            LOG.info("[OTQ_END] get_mappingl_symbols: Queried " + symbols.size() + " symbols ");
            return symbols;
        } catch (Exception e) {
            LOG.error("getSymbolUUID failed ", e);
            throw new RuntimeException(e);
        }
    }

    public SymbolNameHistory loadSymbologyMapping(String context, String refUUID) {

        return getLatestSymbologyMappingRecord(context, refUUID);
    }

    public List<String> getSymbolUUID(String context, String symbol, String symbology) {

        LocalDateTime startUtcTime = LocalDateTime.now();
        Date start = new Date(startUtcTime.toInstant(ZoneOffset.UTC).toEpochMilli());

        OtqQuery query = new OtqQuery(this.uuidLookUpQuery, this.uuidLookUpQuery.length(), "LookupUUID");
        query.set_timezone("UTC");
        query.set_start_time(start);
        query.set_end_time(start);
        SymbolSpecs symbols = new SymbolSpecs();
        SymbolSpec symb = new SymbolSpec(symbol);
        symb.add_parameter("_PARAM_SYMBOL_TIME", LocalDateTime.now().format(yyyyMMddHHmmssDateFormatter));
        symbols.add(symb);
        query.set_symbol_specs(symbols);
        otq_parameters_t otqParams = new otq_parameters_t();
        otqParams.set("DEST_SYMBOLOGY", symbology);
        query.set_otq_parameters(otqParams);
        return getSymbolUUID(context, query);
    }

    public SymbolNameHistory getLatestSymbologyMappingRecord(String context, String refDataUUID) {

        OtqQuery query = new OtqQuery(this.uuidLookUpQuery, this.uuidLookUpQuery.length(), "GetLatestSymbologyMappingRecord");
        StringCollection symbolsCollection = new StringCollection();
        symbolsCollection.add(refDataUUID);
        query.set_symbols(symbolsCollection);
        return getLastSymbolEntry(context, query);
    }

    public void publishRefDataSymbolNameHistory(String uuid, Intention intention, SymbolNameHistory toBeSaved){
        ValueEntryList<ReferenceDataAudit> valueEntryList = refDataValueEntryList.get();
        valueEntryList.reset();
        symbolHistory2ValueMapper.apply(toBeSaved, valueEntryList);
        intention2ValueMapper.apply(intention, valueEntryList);


        LOG.info("[publishRefDataSymbolNameHistory] valueEntryList enriched with uuid " + valueEntryList);
        refDataAuditRecorder.writeToOnetick(uuid, valueEntryList, false);
    }

    public void publishRefDataCorpAction(String uuid, Intention intention, CorporateAction toBeSaved){
        ValueEntryList<ReferenceDataAudit> valueEntryList = refDataValueEntryList.get();
        valueEntryList.reset();

        corporateAction2ValueMapper.apply(toBeSaved, valueEntryList);
        intention2ValueMapper.apply(intention, valueEntryList);

        valueEntryList.put(ReferenceDataAudit.RefDataUUID, uuid);
        valueEntryList.put(ReferenceDataAudit.RequestId, UUID.randomUUID().toString());
        LOG.info("[publishRefDataCorpAction] valueEntryList enriched with uuid " + valueEntryList);
        refDataAuditRecorder.writeToOnetick(uuid, valueEntryList, false);
    }

    // Utility methods
    private static Connection connection(String context) {
        Connection conn = new Connection();
        conn.connect(context);
        return conn;
    }


    public List<String> getAllSymbols(String context) {
        SymbolListCallback symbolListCallback = new SymbolListCallback();
        try {
            OtqQuery query = new OtqQuery(this.quoteHistoryQueries, this.quoteHistoryQueries.length(), "get_all_symbols");
            query.set_timezone("UTC");
            currentTimestamp.set_msec(System.currentTimeMillis()-1000000);
            query.set_start_time(currentTimestamp);
            query.set_end_time(getNow());

            QueryConcurrency queryConcurrency = new QueryConcurrency();
            queryConcurrency.set_max_concurrency(MAX_NUM_CORES);
            query.set_query_concurrency(queryConcurrency);

            long qs = System.currentTimeMillis();
            LOG.info("[OTQ_START] get_all_symbols(" + context + "): " + query.get_start_time() + " to " + query.get_end_time());
            RequestGroup.process_otq_file(query, new CallbackAdaptor<>(symbolListCallback), connection(context));
            List<String> symbols = new ArrayList<>(symbolListCallback.getSymbolList());
            symbolListCallback.clear();
            LOG.info("[OTQ_END] get_all_symbols: Queried " + symbols.size() + " symbols in " + (System.currentTimeMillis() - qs) + " ms");
            return symbols;
        } catch (Exception e) {
            String msg = "Failed executing query get_all_symbols() on " + context;
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }
}
