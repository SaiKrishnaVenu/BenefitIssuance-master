package com.example.BenefitAssuranceService.service;

import com.example.BenefitAssuranceService.dto.EligibilityResponse;
import com.example.BenefitAssuranceService.feign.EligibilityClient;
import com.example.BenefitAssuranceService.util.EmailUtils;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.script.ScriptTemplateConfig;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Service
public class BenefitServiceImpl implements BenefitService {

    private EligibilityClient eligibilityClient;
    private EmailUtils emailUtils;

    public BenefitServiceImpl(EligibilityClient eligibilityClient, EmailUtils emailUtils) {
        this.eligibilityClient = eligibilityClient;
        this.emailUtils=emailUtils;
    }

    @Override
    public Boolean sendEmailPdf(String email) {
        List<EligibilityResponse> res = eligibilityClient.getAll().getBody();
        if (res == null || res.isEmpty()) {
            throw new RuntimeException("No eligibility data found.");
        }
        String fileName="benefits.txt";
        String body = readEmailBody(fileName);
        String subject = "Benefits Reports pdf";
        byte[] pdfData = generatePdf(res);
        emailUtils.sendMail(email,subject ,body,pdfData);
        return true;
    }

    @Override
    public Boolean sendEmailExcel(String email) {
        List<EligibilityResponse> res = eligibilityClient.getAll().getBody();
        if (res == null || res.isEmpty()) {
            throw new RuntimeException("No eligibility data found.");
        }
        String fileName="benefits.txt";
        String body = readEmailBody(fileName);
        String subject = "Benefits Reports Excel";
        byte[] ExcelData = generateExcel(res);
        emailUtils.sendMailExcel(email,subject ,body,ExcelData);
        return true;
    }

    private byte[] generateExcel(List<EligibilityResponse> resList) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Eligibility Report");

            // ✅ Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // ✅ Header row
            String[] headers = {
                    "Case Number", "Plan Name", "Plan Status",
                    "Plan Start", "Plan End", "Benefit Amount", "Denial Reason"
            };

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ✅ Data rows
            int rowIdx = 1;
            for (EligibilityResponse res : resList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(res.getCaseNumber());
                row.createCell(1).setCellValue(res.getPlanName());
                row.createCell(2).setCellValue(res.getPlanStatus());
                row.createCell(3).setCellValue(res.getPlanStartDate() != null ? res.getPlanStartDate().toString() : "-");
                row.createCell(4).setCellValue(res.getPlanEndDate() != null ? res.getPlanEndDate().toString() : "-");
                row.createCell(5).setCellValue(res.getBenefitAmount() != null ? res.getBenefitAmount() : 0.0);
                row.createCell(6).setCellValue(res.getDenialReason() != null ? res.getDenialReason() : "-");

                // Apply style to all cells
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // ✅ Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ✅ Write to ByteArrayOutputStream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel", e);
        }
    }
    public byte[] generatePdf(List<EligibilityResponse> responses) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Add Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Eligibility Batch Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table with 7 columns
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add header cells with style
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Color headerBg = new Color(0, 121, 182); // Nice blue

            String[] headers = {
                    "Case Number", "Plan Name", "Plan Status",
                    "Plan Start", "Plan End", "Benefit Amount", "Denial Reason"
            };

            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(headerBg);
                headerCell.setPadding(8f);
                table.addCell(headerCell);
            }

            // Add data rows
            Font cellFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            for (EligibilityResponse res : responses) {
                table.addCell(makeCell(String.valueOf(res.getCaseNumber()), cellFont));
                table.addCell(makeCell(res.getPlanName(), cellFont));
                table.addCell(makeCell(res.getPlanStatus(), cellFont));
                table.addCell(makeCell(res.getPlanStartDate() != null ? res.getPlanStartDate().toString() : "-", cellFont));
                table.addCell(makeCell(res.getPlanEndDate() != null ? res.getPlanEndDate().toString() : "-", cellFont));
                table.addCell(makeCell(res.getBenefitAmount() != null ? String.valueOf(res.getBenefitAmount()) : "-", cellFont));
                table.addCell(makeCell(res.getDenialReason() != null ? res.getDenialReason() : "-", cellFont));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }


    private PdfPCell makeCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }



    public String readEmailBody(  String fileName){

        String mailBody = null;
        try{
            ClassPathResource resource = new ClassPathResource(fileName);
            Path filePath = Path.of(resource.getURI());
            try(FileReader fr = new FileReader(filePath.toFile()); BufferedReader br = new BufferedReader(fr)) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while(line!=null){
                    sb.append(line);
                    sb.append("\n");
                    line=br.readLine();
                }
                mailBody= sb.toString();



            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mailBody;
    }
}
