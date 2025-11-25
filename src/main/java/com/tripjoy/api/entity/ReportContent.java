package com.tripjoy.api.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class ReportContent extends BaseEntity{

    private String contentType;
    private String text;
    private String mediaUrl;
    private String status;
}
