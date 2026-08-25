package org.example.ads.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class AdDto {
    private UUID id;
    private UUID authorId;
    private String title;
    private String description;
    private double price;
    private Instant createdAt;
    private Instant updatedAt;
}
