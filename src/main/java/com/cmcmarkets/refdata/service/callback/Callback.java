package com.cmcmarkets.refdata.service.callback;

import com.omd.jomd.Tick;
import com.omd.jomd.TickDescriptor;

public interface Callback<C extends Callback<C>> {

    C getCallback();

    void process_symbol_name(String symbol_name);

    void process_tick_descriptor(TickDescriptor tick_descriptor);

    void process_event(Tick tick, java.util.Date time);

    void process_error(int error_code, String error_msg) throws Exception;
}
