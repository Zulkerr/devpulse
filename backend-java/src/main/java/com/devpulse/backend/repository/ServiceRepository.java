package com.devpulse.backend.repository;

import com.devpulse.backend.model.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<MonitoredService, Long> {
}
