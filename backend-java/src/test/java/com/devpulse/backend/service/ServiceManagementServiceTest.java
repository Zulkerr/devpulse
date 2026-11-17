package com.devpulse.backend.service;

import com.devpulse.backend.dto.ServiceDTO;
import com.devpulse.backend.model.MonitoredService;
import com.devpulse.backend.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    @Test
    void createService() {
        ServiceDTO dto = new ServiceDTO();
        dto.setName("Test");
        dto.setUrl("http://test.com");

        MonitoredService ms = new MonitoredService();
        ms.setName("Test");
        ms.setUrl("http://test.com");

        when(repository.save(any(MonitoredService.class))).thenReturn(ms);

        MonitoredService result = serviceManagementService.create(dto);
        assertEquals("Test", result.getName());
    }
}
