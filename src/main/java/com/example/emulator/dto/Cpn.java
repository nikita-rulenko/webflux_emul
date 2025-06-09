package com.example.emulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO для представления CPN (Coupon) из JSON конфигурации
 */
public record Cpn(
    @JsonProperty("id") 
    long id,
    
    @JsonProperty("omni_id") 
    String omniId,
    
    @JsonProperty("use") 
    String use,
    
    @JsonProperty("conditions") 
    String conditions,
    
    @JsonProperty("partner_omni_id") 
    long partnerOmniId,
    
    @JsonProperty("partner_crm_id") 
    String partnerCrmId,
    
    @JsonProperty("offers") 
    List<CpnOffer> offers
) {
    /**
     * Вложенный record для представления предложения в купоне
     */
    public record CpnOffer(
        @JsonProperty("id") 
        long id,
        
        @JsonProperty("omni_id") 
        String omniId,
        
        @JsonProperty("price") 
        int price
    ) {}
}
