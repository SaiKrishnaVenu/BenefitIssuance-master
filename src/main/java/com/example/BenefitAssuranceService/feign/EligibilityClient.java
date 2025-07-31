package com.example.BenefitAssuranceService.feign;


import com.example.BenefitAssuranceService.dto.EligibilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="ELIGIBILITYDETERMINATIONSERVICE", url= "http://localhost:6009/api/eligibility")
public interface EligibilityClient {
    @GetMapping("/getAllbenefits")
    public ResponseEntity<List<EligibilityResponse>> getAll();
}
