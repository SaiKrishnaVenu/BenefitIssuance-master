package com.example.BenefitAssuranceService.controller;


import com.example.BenefitAssuranceService.service.BenefitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/benefit")
public class BenefitController {
    public BenefitController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    private BenefitService benefitService;


    @GetMapping("/pdf")
    public ResponseEntity<String> sendPdf(@RequestParam String email){
        Boolean isSent = benefitService.sendEmailPdf(email);
        String resMsg  = null;

        if(isSent){
            resMsg="Pdf Document Sent Successfully";
        }
        return new ResponseEntity<>(resMsg, HttpStatus.OK);

    }

    @GetMapping("/excel")
    public ResponseEntity<String> sendExcel(@RequestParam String email){
        Boolean isSent = benefitService.sendEmailExcel(email);
        String resMsg  = null;

        if(isSent){
            resMsg="Excel Document Sent Successfully";
        }
        return new ResponseEntity<>(resMsg, HttpStatus.OK);

    }
}
