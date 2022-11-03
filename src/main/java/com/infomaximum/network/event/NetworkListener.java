package com.infomaximum.network.event;

import com.infomaximum.network.session.Session;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Ulitin
 * Date: 08.03.12
 * Time: 20:06
 * Слушатель
 */
public interface NetworkListener {

	void onConnect(Session session);

	void onDisconnect(Session session);
}
