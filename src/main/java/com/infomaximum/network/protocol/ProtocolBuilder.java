package com.infomaximum.network.protocol;

import com.infomaximum.network.exception.NetworkException;

public abstract class ProtocolBuilder {

    public abstract Protocol build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws NetworkException;
}
