package org.example.ads.dto;

import lombok.Data;

@Data
public class AdCreateDto {
    private String title;
    private String description;
    private double price;
}
