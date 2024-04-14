package com.example.backendmainserver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check API", description = "Health Check API Controller입니다.")
@RestController
@RequestMapping("api/health")
@Slf4j
public class HealthTestController {
    @Operation(summary = "Health Check API", description = "Health Check API입니다.")
    @GetMapping("")
    public String apiHealthTest() {
        log.info("health req!!");
        return "energiology!";
    }
}
