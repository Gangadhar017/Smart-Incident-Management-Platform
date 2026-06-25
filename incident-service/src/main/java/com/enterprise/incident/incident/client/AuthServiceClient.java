package com.enterprise.incident.incident.client;

import com.enterprise.incident.incident.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${app.services.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @GetMapping("/api/v1/auth/me")
    UserDto getCurrentUser(@RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
}
