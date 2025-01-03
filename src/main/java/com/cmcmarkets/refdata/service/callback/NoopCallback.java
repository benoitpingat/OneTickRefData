package com.cmcmarkets.refdata.service.callback;

import com.omd.jomd.Tick;
import com.omd.jomd.TickDescriptor;

import java.util.Date;

public class NoopCallback<C extends Callback<C>> implements Callback<C> {

    @Override
    public void process_symbol_name(String symbol_name) {
    }

    @Override
    public void process_tick_descriptor(TickDescriptor tick_descriptor) {
    }

    @Override
    public void process_event(Tick tick, Date time) {
    }

    @Override
    public void process_error(int error_code, String error_msg) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public C getCallback() {
        return (C) this;
    }
}
