package com.deepspace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcludeCatalogFilter {
    
    private String mark;
    
    private String model;
}
