package com.devpulse.backend.controller;

import com.devpulse.backend.dto.ServiceDTO;
import com.devpulse.backend.model.MonitoredService;
import com.devpulse.backend.service.ServiceManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceManagementService service;

    public ServiceController(ServiceManagementService service) {
        this.service = service;
    }

    @GetMapping
    public List<MonitoredService> getAll() {
        return service.findAll();
    }

    @PostMapping
    public MonitoredService create(@Valid @RequestBody ServiceDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public MonitoredService updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.updateStatus(id, body.get("status"));
    }
}
