package com.devpulse.backend.service;

import com.devpulse.backend.dto.ServiceDTO;
import com.devpulse.backend.model.MonitoredService;
import com.devpulse.backend.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceManagementService {
    private final ServiceRepository repository;

    public ServiceManagementService(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<MonitoredService> findAll() {
        return repository.findAll();
    }

    public MonitoredService create(ServiceDTO dto) {
        MonitoredService service = new MonitoredService();
        service.setName(dto.getName());
        service.setUrl(dto.getUrl());
        return repository.save(service);
    }

    public MonitoredService updateStatus(Long id, String status) {
        MonitoredService service = repository.findById(id).orElseThrow();
        service.setStatus(status);
        service.setLastChecked(LocalDateTime.now());
        return repository.save(service);
    }
}
