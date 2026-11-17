package com.devpulse.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ServiceDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String url;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
