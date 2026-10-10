package com.deepspace.dto.response.offer.vechicle;

import lombok.Data;

@Data
public class VehicleInfo {
    
    private MarkInfo markInfo;
    
    private ModelInfo modelInfo;
    
    private SuperGen superGen;
    
    private String steeringWheel;
}
