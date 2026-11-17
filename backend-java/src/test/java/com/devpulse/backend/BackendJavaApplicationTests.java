package com.devpulse.backend;

import com.devpulse.backend.dto.ServiceDTO;
import com.devpulse.backend.model.MonitoredService;
import com.devpulse.backend.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendJavaApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServiceRepository repository;

    @Test
    void testCreateAndGetService() {
        ServiceDTO dto = new ServiceDTO();
        dto.setName("TestService");
        dto.setUrl("http://localhost");

        ResponseEntity<MonitoredService> response = restTemplate.postForEntity("/api/services", dto, MonitoredService.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("TestService");
    }
}
