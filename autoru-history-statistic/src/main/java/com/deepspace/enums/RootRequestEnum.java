package com.deepspace.enums;

import com.deepspace.dto.request.RootRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum RootRequestEnum {
    
    MOST_REAL(
            RootRequest.builder()
                    .inStock("IN_STOCK")
                    .steeringWheel("LEFT")
                    .engineGroup(Arrays.asList("DIESEL", "GASOLINE", "TURBO", "ATMO"))
                    .section("all")
                    .category("cars")
                    .outputType("list")
                    .yearFrom(2018)
                    .priceFrom(2_000_000)
                    .priceTo(3_000_000)
                    .bodyTypeGroup(List.of("COUPE"))
                    .build()
    ),
    
    WITH_CREDIT(
            RootRequest.builder()
                    .yearFrom(2018)
                    .priceFrom(2_000_000)
                    .priceTo(4_000_000)
                    .bodyTypeGroup(List.of("COUPE"))
                    /*.excludeCatalogFilter(
                            Arrays.asList(
                                    ExcludeCatalogFilterEnum.CAMARO.getFilter(),
                                    ExcludeCatalogFilterEnum.MUSTANG.getFilter(),
                                    ExcludeCatalogFilterEnum.CHALLENGER.getFilter()
                            )
                    )*/
                    .gearType(List.of("ALL_WHEEL_DRIVE"))
                    .kmAgeTo(80_000)
                    .build()
    );
    
    private final RootRequest request;
}
