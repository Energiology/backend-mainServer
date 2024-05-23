package com.example.backendmainserver.port.presentation;

import com.example.backendmainserver.auth.presentation.AdminAuthenticationPrincipal;
import com.example.backendmainserver.client.raspberry.dto.request.BatterySwitchRequest;
import com.example.backendmainserver.client.raspberry.dto.request.PortAndSupplier;
import com.example.backendmainserver.global.response.SuccessResponse;
import com.example.backendmainserver.port.application.PortBatterySwitchService;
import com.example.backendmainserver.port.application.PortService;
import com.example.backendmainserver.port.domain.Port;
import com.example.backendmainserver.port.presentation.dto.request.PortControlRequest;
import com.example.backendmainserver.port.presentation.dto.response.PortInfoResponse;
import com.example.backendmainserver.port.presentation.dto.response.PortInfoResponses;
import com.example.backendmainserver.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Port API", description = "포트 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/port")
public class PortController {
    private final PortService portService;
    private final PortBatterySwitchService portBatterySwitchService;

    @Operation(summary = "포트 수동 제어 api", description =
            "라즈베리파이에 연결된 포트의 전원을 제어합니다.\n" +
            "state: BATTERY or EXTERNAL or OFF")
    @PostMapping("/control")
    public ResponseEntity<SuccessResponse<HttpStatus>> portControl(
            @AdminAuthenticationPrincipal User user,
            @RequestBody PortControlRequest portControlRequest){

        BatterySwitchRequest batterySwitchRequest = convertToBatterySwitchRequest(portControlRequest);
        portBatterySwitchService.requestBatterySwitchToRaspberry(batterySwitchRequest);

        return SuccessResponse.of(HttpStatus.OK);
    }

    @Operation(summary = "모든 포튼 정보 조회 List")
    @GetMapping("")
    public ResponseEntity<SuccessResponse<PortInfoResponses>> getAllPort(
            @AdminAuthenticationPrincipal User user
            ){

        List<Port> ports = portService.getAllPorts();

        List<PortInfoResponse> portInfoResponseList = convertToPortInfoResponseList(ports);


        return SuccessResponse.of(new PortInfoResponses(portInfoResponseList));
    }

    private List<PortInfoResponse> convertToPortInfoResponseList(List<Port> ports) {
        List<PortInfoResponse> list = ports.stream().map((p) -> {
            return new PortInfoResponse(
                    p.getId(),
                    p.getMinimumOutput(),
                    p.getMaximumOutput(),
                    p.getRoom().getId(),
                    p.getBatterySwitchOption(),
                    p.getPowerSupplier().toString());
        }).toList();

        return list;
    }

    public BatterySwitchRequest convertToBatterySwitchRequest(PortControlRequest portControlRequest) {
        List<PortAndSupplier> portAndSuppliers = portControlRequest.portIdAndStates().stream()
                .map(portIdAndState -> new PortAndSupplier(portIdAndState.portId(), portIdAndState.state()))
                .collect(Collectors.toList());

        return new BatterySwitchRequest(portAndSuppliers);
    }
}