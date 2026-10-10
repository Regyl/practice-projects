package com.deepspace.enums;

import com.deepspace.dto.request.ExcludeCatalogFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExcludeCatalogFilterEnum {
    
    CAMARO(new ExcludeCatalogFilter("CHEVROLET", "CAMARO")),
    MUSTANG(new ExcludeCatalogFilter("FORD", "MUSTANG")),
    CHALLENGER(new ExcludeCatalogFilter("DODGE", "CHALLENGER"));
    
    private final ExcludeCatalogFilter filter;
}
