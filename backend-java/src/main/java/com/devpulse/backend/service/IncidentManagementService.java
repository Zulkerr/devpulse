package com.devpulse.backend.service;

import com.devpulse.backend.dto.IncidentDTO;
import com.devpulse.backend.model.Incident;
import com.devpulse.backend.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IncidentManagementService {
    private final IncidentRepository repository;

    public IncidentManagementService(IncidentRepository repository) {
        this.repository = repository;
    }

    public List<Incident> findAll() {
        return repository.findAll();
    }

    public Incident create(IncidentDTO dto) {
        Incident incident = new Incident();
        incident.setServiceId(dto.getServiceId());
        incident.setTitle(dto.getTitle());
        incident.setSeverity(dto.getSeverity());
        return repository.save(incident);
    }

    public Incident markResolved(Long id) {
        Incident incident = repository.findById(id).orElseThrow();
        incident.setResolved(true);
        return repository.save(incident);
    }
}
