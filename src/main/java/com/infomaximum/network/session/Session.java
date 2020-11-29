package com.infomaximum.network.session;

import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.struct.SessionData;

/**
 * Created by Kris on 29.07.2015.
 */
public interface Session {

    String getUuid();

    HandshakeData getHandshakeData();

    SessionData getData();
}
