package com.devpulse.backend.controller;

import com.devpulse.backend.dto.IncidentDTO;
import com.devpulse.backend.model.Incident;
import com.devpulse.backend.service.IncidentManagementService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentManagementService service;

    public IncidentController(IncidentManagementService service) {
        this.service = service;
    }

    @GetMapping
    public List<Incident> getAll() {
        return service.findAll();
    }

    @PostMapping
    public Incident create(@RequestBody IncidentDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}/resolve")
    public Incident resolve(@PathVariable Long id) {
        return service.markResolved(id);
    }
}
