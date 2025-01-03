package com.cmcmarkets.refdata.service.callback;

import com.cmcmarkets.refdata.service.api.SymbolNameHistory;
import com.omd.jomd.Tick;

import java.util.ArrayList;
import java.util.List;

public class SymbolNameHistoryCallback extends NoopCallback<SymbolNameHistoryCallback> implements Callback<SymbolNameHistoryCallback> {
    private List<SymbolNameHistory> symbolMappings = new ArrayList<>();

    public List<SymbolNameHistory> getSymbolMappings() {
        return symbolMappings;
    }

    public void clear() {
        symbolMappings.clear();
    }

    @Override
    public void process_event(Tick tick, java.util.Date time) {
        SymbolNameHistory entry = new SymbolNameHistory();
        //TODO implement
        entry.setSymbol(tick.get_string("SYMBOL_NAME"));
        entry.setStartDateTime(tick.get_nsectime("START_DATETIME"));
        entry.setEndDateTime(tick.get_nsectime("END_DATETIME"));

        this.symbolMappings.add(entry);
    }
}
