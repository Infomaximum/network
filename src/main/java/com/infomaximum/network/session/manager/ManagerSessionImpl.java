package com.infomaximum.network.session.manager;

import com.infomaximum.network.ManagerSession;
import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by kris on 30.08.16.
 */
public interface ManagerSessionImpl extends ManagerSession, NetworkListener, AutoCloseable {

}
