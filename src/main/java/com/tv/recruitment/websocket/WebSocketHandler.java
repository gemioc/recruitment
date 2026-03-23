package com.tv.recruitment.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tv.recruitment.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final DeviceService deviceService;

    /**
     * 存储所有连接的Session，key为设备编码
     */
    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceCode = getDeviceCode(session);
        if (deviceCode != null) {
            SESSION_MAP.put(deviceCode, session);
            deviceService.updateOnlineStatus(deviceCode, true);
            log.info("设备连接成功: {}", deviceCode);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");

        switch (type) {
            case "HEARTBEAT" -> handleHeartbeat(session, json);
            case "STATUS_REPORT" -> handleStatusReport(session, json);
            case "PUSH_ACK" -> handlePushAck(session, json);
            default -> log.warn("未知消息类型: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceCode = getDeviceCode(session);
        if (deviceCode != null) {
            SESSION_MAP.remove(deviceCode);
            deviceService.updateOnlineStatus(deviceCode, false);
            log.info("设备断开连接: {}", deviceCode);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session, JSONObject json) {
        String deviceCode = getDeviceCode(session);
        if (deviceCode != null) {
            deviceService.updateOnlineStatus(deviceCode, true);
        }
        // 响应心跳
        sendMessage(session, JSONUtil.createObj()
                .set("type", "HEARTBEAT_ACK")
                .set("timestamp", System.currentTimeMillis())
                .toString());
    }

    /**
     * 处理状态上报
     */
    private void handleStatusReport(WebSocketSession session, JSONObject json) {
        String deviceCode = json.getStr("deviceCode");
        JSONObject data = json.getJSONObject("data");

        // 更新设备状态
        // TODO: 更新设备当前播放状态

        log.info("设备状态上报: {} - {}", deviceCode, data);
    }

    /**
     * 处理推送确认
     */
    private void handlePushAck(WebSocketSession session, JSONObject json) {
        String messageId = json.getStr("messageId");
        boolean success = json.getBool("success");
        String errorMessage = json.getStr("errorMessage");

        // TODO: 更新推送记录状态

        log.info("推送确认: messageId={}, success={}", messageId, success);
    }

    /**
     * 推送内容到设备
     */
    public void pushContent(String deviceCode, String contentType, String contentUrl, String playRule) {
        WebSocketSession session = SESSION_MAP.get(deviceCode);
        if (session == null || !session.isOpen()) {
            log.warn("设备不在线: {}", deviceCode);
            return;
        }

        String message = JSONUtil.createObj()
                .set("type", "PUSH_CONTENT")
                .set("messageId", "msg_" + System.currentTimeMillis())
                .set("data", JSONUtil.createObj()
                        .set("contentType", contentType)
                        .set("contentUrl", contentUrl)
                        .set("playRule", JSONUtil.parseObj(playRule)))
                .set("timestamp", System.currentTimeMillis())
                .toString();

        sendMessage(session, message);
    }

    /**
     * 发送控制指令
     */
    public void sendControl(String deviceCode, String action) {
        WebSocketSession session = SESSION_MAP.get(deviceCode);
        if (session == null || !session.isOpen()) {
            log.warn("设备不在线: {}", deviceCode);
            return;
        }

        String message = JSONUtil.createObj()
                .set("type", "CONTROL")
                .set("data", JSONUtil.createObj().set("action", action))
                .set("timestamp", System.currentTimeMillis())
                .toString();

        sendMessage(session, message);
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取设备编码
     */
    private String getDeviceCode(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("deviceCode=")) {
            return query.split("deviceCode=")[1].split("&")[0];
        }
        return null;
    }

    /**
     * 检查设备是否在线
     */
    public boolean isOnline(String deviceCode) {
        WebSocketSession session = SESSION_MAP.get(deviceCode);
        return session != null && session.isOpen();
    }
}