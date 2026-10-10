package com.deepspace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RootRequest {
    
    private String inStock = "IN_STOCK";
    
    private Integer yearFrom;
    
    private Integer priceFrom;
    
    private Integer priceTo;
    
    private String steeringWheel = "LEFT";
    
    /**
     * Максимальное время разгона до сотни.
     */
    private Integer accelerationTo;
    
    private List<String> bodyTypeGroup;
    
    private List<String> engineGroup = Arrays.asList("DIESEL", "GASOLINE", "TURBO", "ATMO");
    
    private String section = "all";
    
    private String category = "cars";
    
    private String outputType = "list";
    
    private List<String> geoId = new ArrayList<>();
    
    private List<ExcludeCatalogFilter> excludeCatalogFilter;
    
    private List<String> gearType; //ALL_WHEEL_DRIVE
    
    private Integer kmAgeTo;
}
