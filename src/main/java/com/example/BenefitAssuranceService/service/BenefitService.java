package com.example.BenefitAssuranceService.service;

public interface BenefitService {

    public Boolean sendEmailPdf(String email);

    public Boolean sendEmailExcel(String email);
}
