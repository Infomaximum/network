package com.infomaximum.network.mvc.builder;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.mvc.ResponseEntity;
import com.infomaximum.network.mvc.anotation.Controller;
import com.infomaximum.network.mvc.anotation.ControllerAction;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.protocol.standard.packet.TargetPacket;
import com.infomaximum.network.protocol.standard.session.StandardTransportSession;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.session.SessionImpl;
import net.minidev.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class MvcPackerHandler implements PacketHandler {

    private final static Logger log = LoggerFactory.getLogger(MvcPackerHandler.class);

    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private final HashMap<String, MvcController> controllers;

    private MvcPackerHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler, HashMap<String, MvcController> controllers) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.controllers = controllers;
    }

    private static class MvcController {
        private final Object controller;
        private final HashMap<String, Method> actions;

        public MvcController(Object controller, HashMap<String, Method> actions) {
            this.controller = controller;
            this.actions = actions;
        }
    }

    @Override
    public CompletableFuture<IPacket[]> exec(Session session, IPacket packet) {
        TargetPacket requestPacket = (TargetPacket) packet;

        MvcController mvcController = controllers.get(requestPacket.controller);

        try {
            final Method method = mvcController.actions.get(requestPacket.action);
            if (method == null) {
                return CompletableFuture.completedFuture(
                        ResponsePacket.response(
                                (RequestPacket) packet,
                                ResponseEntity.RESPONSE_CODE_ERROR,
                                new JSONObject() {{
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
                    methodArds[i] = requestPacket.getData();
                } else if (clazz == StandardTransportSession.class) {
                    methodArds[i] = ((SessionImpl) session).getTransportSession();
                } else if (TargetPacket.class.isAssignableFrom(clazz)) {
                    methodArds[i] = packet;
                } else {
                    throw new RuntimeException("Nothing type to method: " + requestPacket.action + ", i: " + i);
                }
            }

            Object result = method.invoke(mvcController.controller, methodArds);

            if (result == null) {
                return CompletableFuture.completedFuture(ResponsePacket.response(
                        (RequestPacket) packet,
                        ResponseEntity.RESPONSE_CODE_OK,
                        null)
                );
            } else if (result instanceof CompletableFuture) {
                CompletableFuture<ResponseEntity> futureResponse = (CompletableFuture<ResponseEntity>) result;
                return futureResponse.thenApply(
                        responseEntity -> ResponsePacket.response(
                                (RequestPacket) packet,
                                responseEntity.code,
                                responseEntity.data
                        )
                ).exceptionally(throwable -> {
                    uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), throwable);
                    return null;
                });
            } else if (result instanceof ResponseEntity) {
                ResponseEntity responseEntity = (ResponseEntity) result;
                return CompletableFuture.completedFuture(
                        ResponsePacket.response((RequestPacket) packet, responseEntity.code, responseEntity.data)
                );
            } else {
                throw new RuntimeException("Not support return type: " + result);
            }
        } catch (Throwable t) {
            uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t);
            return null;
        }

    }

    public static class Builder extends PacketHandler.Builder {

        private String scanPackage;

        public Builder(Package scanPackage) {
            this.scanPackage = scanPackage.getName();
        }

        public Builder(String scanPackage) {
            this.scanPackage = scanPackage;
        }

        @Override
        public PacketHandler build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws NetworkException {
            try {
                HashMap<String, MvcController> controllers = new HashMap<>();

                Reflections reflections = new Reflections(scanPackage);
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

                return new MvcPackerHandler(uncaughtExceptionHandler, controllers);
            } catch (ReflectiveOperationException e) {
                throw new NetworkException(e);
            }
        }
    }
}
