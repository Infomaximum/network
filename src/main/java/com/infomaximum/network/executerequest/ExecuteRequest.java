package com.infomaximum.network.executerequest;

import com.infomaximum.network.exception.ParsePacketNetworkException;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.session.TransportSession;
import com.infomaximum.network.transport.TransportPacketHandler;
import com.infomaximum.network.utils.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Общая логика следующая. Если сейчас стои режим рукопожатия - то все пакеты выстраиваются в очередь,
 * как пакеты на рукопожатие, так и уже последующие запросы.
 * Как тольок фаза рукопожатия закончится - все пакеты начнут выполняться асинхронно
 */
public class ExecuteRequest {

    private final static Logger log = LoggerFactory.getLogger(ExecuteRequest.class);

    private final TransportPacketHandler transportPacketHandler;

    private final ConcurrentLinkedQueue<String> queue;
    private final AtomicBoolean syncExecuted;

    public ExecuteRequest(TransportPacketHandler transportPacketHandler) {
        this.transportPacketHandler = transportPacketHandler;

        this.queue = new ConcurrentLinkedQueue();
        this.syncExecuted = new AtomicBoolean(false);
    }

    public void incomingPacket(String message) {
        if (transportPacketHandler.isPhaseHandshake()) {
            queue.add(message);
            trySyncExecute();
        } else {
            asyncExecute(message);
        }
    }

    private void trySyncExecute() {
        ExecutorUtil.executors.execute(() -> {
            synchronized (queue) {

                //Есть какой то уже работающий поток - выходим
                if (!syncExecuted.compareAndSet(false, true)) {
                    return;
                }

                try {
                    String message;
                    while ((message = queue.poll()) != null) {
                        if (transportPacketHandler.isPhaseHandshake()) {
                            syncExecute(message).get();
                        } else {
                            asyncExecute(message);
                        }
                    }
                } catch (Exception e) {
                    ((TransportSession)transportPacketHandler).getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                } finally {
                    syncExecuted.set(false);
                }
            }
        });
    }

    private void asyncExecute(String message) {
        ExecutorUtil.executors.execute(() -> {
            syncExecute(message);
        });
    }

    private CompletableFuture<Void> syncExecute(String message) {
        try {
            IPacket request = transportPacketHandler.parse(message);
            PacketHandler packetHandler = transportPacketHandler.getPacketHandler();
            CompletableFuture<IPacket[]> response = packetHandler.exec(transportPacketHandler.getSession(), request);
            return sendResponse(response);
        } catch (ParsePacketNetworkException e) {
            log.error("Ошибка парсинга входящего пакета, игнорим. packet: " + message, e);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Ошибка обработки входящего пакета", e);
            transportPacketHandler.getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            return CompletableFuture.completedFuture(null);
        }
    }


    private CompletableFuture<Void> sendResponse(CompletableFuture<IPacket[]> response) {
        return response.thenAccept(responsePackets -> {
            if (responsePackets != null) {
                for(IPacket packet: responsePackets) {
                    transportPacketHandler.send(packet);
                }
            }
        });
    }
}
