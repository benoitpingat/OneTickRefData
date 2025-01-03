package com.cmcmarkets.refdata.service.callback;

import com.omd.jomd.Tick;

import java.util.ArrayList;
import java.util.List;

public class SymbolMappingsCallback extends NoopCallback<SymbolMappingsCallback> implements Callback<SymbolMappingsCallback> {
    private List<String> symbolMappings = new ArrayList<>();

    public List<String> getSymbolMappings() {
        return symbolMappings;
    }

    public void clear() {
        symbolMappings.clear();
    }

    @Override
    public void process_event(Tick tick, java.util.Date time) {
        this.symbolMappings.add(tick.get_string("MAPPED_SYMBOL_NAME"));
    }
}
