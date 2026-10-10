package com.deepspace.dto.response;

import com.deepspace.dto.response.offer.Offer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RootResponse {
    
    private String status;
    
    private List<Offer> offers;
}
