package com.deepspace.dto.response.offer;

import com.deepspace.dto.response.offer.seller.Seller;
import com.deepspace.dto.response.offer.vechicle.VehicleInfo;
import lombok.Data;

@Data
public class Offer {
    
    private PriceInfo priceInfo;
    
    private Seller seller;
    
    private VehicleInfo vehicleInfo;
}
