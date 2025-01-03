package com.cmcmarkets.refdata.service.callback;

import com.omd.jomd.Tick;

import java.util.ArrayList;
import java.util.List;

public class SymbolListCallback extends NoopCallback<SymbolListCallback> implements Callback<SymbolListCallback> {
    private List<String> symbolList = new ArrayList<>();

    public List<String> getSymbolList() {
        return symbolList;
    }

    public void clear() {
        symbolList.clear();
    }

    @Override
    public void process_event(Tick tick, java.util.Date time) {
        this.symbolList.add(tick.get_string("PMS_FEED_SYMBOL"));
    }
}
