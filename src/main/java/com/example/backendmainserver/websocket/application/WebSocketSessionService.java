package com.example.backendmainserver.websocket.application;

import com.example.backendmainserver.PowerData.domain.PowerData;
import com.example.backendmainserver.PowerData.domain.PowerDataList;
import com.example.backendmainserver.event.domain.PowerAlertEvent;
import com.example.backendmainserver.port.application.PortService;
import com.example.backendmainserver.port.domain.Port;
import com.example.backendmainserver.user.application.UserService;
import com.example.backendmainserver.user.domain.Role;
import com.example.backendmainserver.user.domain.User;
import com.example.backendmainserver.user.domain.UserVO;
import com.example.backendmainserver.websocket.domain.InMemoryWebSocketSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketSessionService {
    private final InMemoryWebSocketSessionRepository webSocketSessionRepository;
    private final UserService userService;
    private final PortService portService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void save(WebSocketSession session, Long userId){
        User user = userService.getUser(userId);

        UserVO userVO = UserVO.buildUserVO(user);
        webSocketSessionRepository.save(session, userVO);

        log.info("user: {} connect to websocket session:{}", userId, session.getId());
    }

    public void delete(String sessionId){
        webSocketSessionRepository.delete(sessionId);
    }

    public void sendPowerData(PowerDataList data) throws IOException {
        Set<Map.Entry<WebSocketSession, UserVO>> entrySet = webSocketSessionRepository.getEntrySet();
        List<PowerData> powerDataList = data.getPowerDataList();
        setPowerSupplierToPowerDataList(powerDataList);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        for (Map.Entry<WebSocketSession, UserVO> webSocketSessionUserVOEntry : entrySet) {
            WebSocketSession webSocketSession = webSocketSessionUserVOEntry.getKey();
            UserVO userVO = webSocketSessionUserVOEntry.getValue();
            List<PowerData> sendPowerDataList = new ArrayList<>();

            List<Long> userPortsId = userService.getPortsId(userVO.getId());


            for (PowerData powerData : powerDataList) {
                Long portId = powerData.getPortId();

                if (userVO.getRole().equals(Role.ADMIN) || userPortsId.contains(portId)) {
                    Port port = portService.getPortById(portId);

                    sendPowerDataList.add(powerData);
                    publishPowerAlertEvent(port.getRoom().getId(), portId, userVO.getFcmToken(), powerData.getPower());
                }
            }

            if(!sendPowerDataList.isEmpty()) {
                String jsonMessage = objectMapper.writeValueAsString(sendPowerDataList);

                try{
                    synchronized (webSocketSession){
                        webSocketSession.sendMessage(new TextMessage(jsonMessage));
                    }
                }catch (IOException e){
                    log.error(e.getMessage());
                }

                log.info("Main Server Send Data To User #{} ", userVO.getId());
                log.info("Data = {}", sendPowerDataList);
            }
        }
    }

    private void setPowerSupplierToPowerDataList(List<PowerData> powerDataList){
        HashMap<Long, String> powerSupplierMap = new HashMap<>();
        List<Port> allPorts = portService.getAllPorts();

        for (Port port : allPorts) {
            powerSupplierMap.put(port.getId(), port.getPowerSupplier().getNameKr());
        }

        for (PowerData powerData : powerDataList) {
            powerData.setPowerSupplier(powerSupplierMap.get(powerData.getPortId()));
        }
    }

    private void publishPowerAlertEvent(Long roomId, Long portId, String fcmToken, Double powerUsage){
        PowerAlertEvent event = new PowerAlertEvent(this, roomId, portId, fcmToken, powerUsage);
        applicationEventPublisher.publishEvent(event);
    }
}
