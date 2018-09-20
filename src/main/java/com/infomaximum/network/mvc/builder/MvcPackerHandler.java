package com.infomaximum.network.mvc.builder;

import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.handler.PacketHandler;
import com.infomaximum.network.mvc.anotation.Controller;
import com.infomaximum.network.mvc.anotation.ControllerAction;
import com.infomaximum.network.packet.RequestPacket;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.session.Session;
import net.minidev.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class MvcPackerHandler implements PacketHandler {

    private final static Logger log = LoggerFactory.getLogger(MvcPackerHandler.class);

    private static class MvcController {
        private final Object controller;
        private final HashMap<String, Method> actions;

        public MvcController(Object controller, HashMap<String, Method> actions) {
            this.controller = controller;
            this.actions = actions;
        }
    }

    public static class Builder extends PacketHandler.Builder {

        private Package scanPackage;

        private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

        public Builder(Package scanPackage) {
            this.scanPackage = scanPackage;
        }

        @Override
        public PacketHandler build(NetworkImpl network) throws ReflectiveOperationException {
            HashMap<String, MvcController> controllers = new HashMap<>();

            Reflections reflections = new Reflections(scanPackage.getName());
            for (Class classController : reflections.getTypesAnnotatedWith(Controller.class, true)) {
                Controller aController = (Controller) classController.getAnnotation(Controller.class);

                Constructor constructor = classController.getConstructor();
                constructor.setAccessible(true);
                Object controller = constructor.newInstance();

                HashMap<String, Method> actions = new HashMap<String, Method>();
                for (Method method : classController.getDeclaredMethods()) {
                    ControllerAction aControllerAction = method.getDeclaredAnnotation(ControllerAction.class);
                    if (aControllerAction == null) continue;

                    method.setAccessible(true);
                    actions.put(aControllerAction.value(), method);
                }

                controllers.put(aController.value(), new MvcController(controller, actions));
            }

            return new MvcPackerHandler(network, controllers);
        }
    }

    private final NetworkImpl network;
    private final HashMap<String, MvcController> controllers;

    private MvcPackerHandler(NetworkImpl network, HashMap<String, MvcController> controllers) {
        this.network = network;
        this.controllers = controllers;
    }

    @Override
    public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet) {
        MvcController mvcController = controllers.get(packet.controller);

        try {
            final Method method = mvcController.actions.get(packet.action);
            if (method == null) {
                return CompletableFuture.completedFuture(
                        ResponsePacket.responseException((RequestPacket) packet, new JSONObject() {{
                            put("message", "unknown action");
                        }})
                );
            }


            /** Разбираемся с аргументами */
            Type[] methodParameterTypes = method.getGenericParameterTypes();
            final Object[] methodArds = new Object[methodParameterTypes.length];
            for (int i = 0; i < methodParameterTypes.length; i++) {
                Class clazz = (Class) methodParameterTypes[i];
                if (clazz == Session.class) {
                    methodArds[i] = session;
                } else if (clazz == JSONObject.class) {
                    methodArds[i] = packet.getData();
                } else {
                    throw new RuntimeException("Nothing type to method: " + packet.action + ", i: " + i);
                }
            }

            Object result = method.invoke(mvcController.controller, methodArds);

            if (result == null) {
                return CompletableFuture.completedFuture(ResponsePacket.responseAccept((RequestPacket) packet, null));
            } else if (result instanceof CompletableFuture) {
                CompletableFuture<JSONObject> futureResponse = (CompletableFuture<JSONObject>) result;
                return futureResponse.thenApply(
                        jsonResponse -> ResponsePacket.responseAccept((RequestPacket) packet, jsonResponse)
                );
            } else if (result instanceof JSONObject) {
                return CompletableFuture.completedFuture(
                        ResponsePacket.responseAccept((RequestPacket) packet, (JSONObject) result)
                );
            } else {
                throw new RuntimeException("Not support return type: " + result);
            }
        } catch (Throwable t) {
            network.getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
            return null;
        }

    }
}
