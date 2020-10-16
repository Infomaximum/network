package com.infomaximum.network.protocol;

import com.infomaximum.network.session.TransportSession;
import com.infomaximum.network.transport.Transport;

import java.lang.reflect.InvocationTargetException;

public abstract class Protocol {

    public abstract String getName();

    public abstract TransportSession onConnect(Transport transport, Object channel) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, Exception;
}
