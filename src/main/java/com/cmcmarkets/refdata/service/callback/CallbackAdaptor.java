package com.cmcmarkets.refdata.service.callback;

import com.omd.jomd.JavaOutputCallback;
import com.omd.jomd.Tick;
import com.omd.jomd.TickDescriptor;

public class CallbackAdaptor<C extends Callback<C>> extends JavaOutputCallback implements Callback<C> {
    private final C callback;

    public CallbackAdaptor(final C callback) {
        this.callback = callback;
    }

    public C getCallback() {
        return this.callback;
    }

    @Override
    public void process_symbol_name(String symbol_name) {
        this.callback.process_symbol_name(symbol_name);
    }

    @Override
    public void process_tick_descriptor(TickDescriptor td) {
        this.callback.process_tick_descriptor(td);
    }

    @Override
    public void process_event(Tick tick, java.util.Date time) {
        this.callback.process_event(tick, time);
    }

    @Override
    public void process_error(int error_code, String error_msg) {
        try {
            this.callback.process_error(error_code, error_msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaOutputCallback replicate() {
        return new CallbackAdaptor<>(this.callback);
    }
}
