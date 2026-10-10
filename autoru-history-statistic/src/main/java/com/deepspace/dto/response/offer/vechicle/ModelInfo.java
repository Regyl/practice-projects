package com.deepspace.dto.response.offer.vechicle;

import lombok.Data;

@Data
public class ModelInfo {

    private String code;
    
    private String name;
    
    private String ruName;
    
    public static class Nameplate {
        
        private String name;
    }
}
