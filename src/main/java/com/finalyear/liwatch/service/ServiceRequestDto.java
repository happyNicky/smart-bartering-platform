package com.finalyear.liwatch.service;

import com.finalyear.liwatch.service.enumservice.SkillLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ServiceRequestDto {
    private String serviceDuration;
    private SkillLevel skillLevel;
    private String availability;
}
