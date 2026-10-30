package com.ems.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.dto.FileDownload;
import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final String COMPANY_NAME = "G-Innovation";
    private static final String CURRENCY = "MAD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private static final List<String[]> TEMPLATE = List.of(
            new String[]{"Article 1 - Position",
                    "{{company}} hereby employs {{fullName}} as {{jobTitle}} within the {{department}} department. "
                            + "The Employee agrees to perform the duties associated with this position "
                            + "diligently and to the best of their abilities."},
            new String[]{"Article 2 - Start Date",
                    "This contract takes effect on {{hireDate}}. It is concluded for an indefinite period."},
            new String[]{"Article 3 - Remuneration",
                    "In consideration of the services rendered, the Employee will receive a gross annual salary of "
                            + "{{salary}}, payable in monthly installments."},
            new String[]{"Article 4 - Working Hours",
                    "The Employee will work according to the company's standard working hours and internal policies."},
            new String[]{"Article 5 - Confidentiality",
                    "The Employee agrees not to disclose any confidential information obtained during "
                            + "the course of employment, both during and after the term of this contract."},
            new String[]{"Article 6 - Termination",
                    "Either party may terminate this contract in accordance with the applicable labour laws, "
                            + "subject to the required notice period."}
    );

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public FileDownload generate(Long employeeId) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Map<String, String> values = Map.of(
                "{{company}}", COMPANY_NAME,
                "{{fullName}}", e.getFullName(),
                "{{jobTitle}}", e.getJobTitle(),
                "{{department}}", orDefault(e.getDepartment()),
                "{{hireDate}}", e.getHireDate().format(DATE_FORMAT),
                "{{salary}}", formatSalary(e.getSalary())
        );

        byte[] pdf = buildPdf(e, values);
        String fileName = "contract-" + slug(e.getFirstName()) + "-" + slug(e.getLastName()) + ".pdf";
        return new FileDownload(pdf, "application/pdf", fileName);
    }

    private byte[] buildPdf(Employee e, Map<String, String> values) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 60, 60, 50, 50);

        try {
            PdfWriter.getInstance(doc, out);
            doc.addTitle("Employment Contract - " + e.getFullName());
            doc.addAuthor(COMPANY_NAME);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 41, 59));
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);

            Paragraph company = new Paragraph(COMPANY_NAME, smallFont);
            company.setAlignment(Element.ALIGN_RIGHT);
            doc.add(company);

            Paragraph title = new Paragraph("EMPLOYMENT CONTRACT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            doc.add(title);

            doc.add(paragraph("Between the undersigned:", bodyFont, 6));
            doc.add(paragraph(COMPANY_NAME + ", hereinafter referred to as \"the Employer\",", bodyFont, 4));
            doc.add(paragraph("and", bodyFont, 4));
            doc.add(paragraph(e.getFullName() + " (" + e.getEmail() + "), hereinafter referred to as \"the Employee\".",
                    bodyFont, 14));
            doc.add(summaryTable(values, headingFont, bodyFont));

            for (String[] article : TEMPLATE) {
                Paragraph heading = new Paragraph(article[0], headingFont);
                heading.setSpacingBefore(12);
                heading.setSpacingAfter(4);
                doc.add(heading);

                Paragraph text = new Paragraph(fill(article[1], values), bodyFont);
                text.setAlignment(Element.ALIGN_JUSTIFIED);
                text.setLeading(15);
                doc.add(text);
            }

            Paragraph date = paragraph("Done on " + LocalDate.now().format(DATE_FORMAT) + ", in two original copies.",
                    bodyFont, 0);
            date.setSpacingBefore(24);
            doc.add(date);
            doc.add(signatureTable(headingFont, smallFont));

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            if (doc.isOpen()) {
                doc.close();
            }
            throw new IllegalStateException("Contract generation failed", ex);
        }
    }

    private PdfPTable summaryTable(Map<String, String> v, Font labelFont, Font valueFont) {
        PdfPTable table = new PdfPTable(new float[]{1, 2});
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        String[][] rows = {
                {"Employee", v.get("{{fullName}}")},
                {"Job title", v.get("{{jobTitle}}")},
                {"Department", v.get("{{department}}")},
                {"Hire date", v.get("{{hireDate}}")},
                {"Annual salary", v.get("{{salary}}")}
        };
        Color background = new Color(241, 245, 249);
        Color border = new Color(203, 213, 225);

        for (String[] row : rows) {
            PdfPCell label = new PdfPCell(new Phrase(row[0], labelFont));
            label.setBackgroundColor(background);
            label.setBorderColor(border);
            label.setPadding(6);
            table.addCell(label);

            PdfPCell value = new PdfPCell(new Phrase(row[1], valueFont));
            value.setBorderColor(border);
            value.setPadding(6);
            table.addCell(value);
        }
        return table;
    }

    private PdfPTable signatureTable(Font labelFont, Font smallFont) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(30);

        for (String party : new String[]{"The Employer", "The Employee"}) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(new Paragraph(party, labelFont));
            cell.addElement(new Paragraph("Signature preceded by \"Read and approved\"", smallFont));
            cell.addElement(new Paragraph("\n\n\n______________________________", smallFont));
            table.addCell(cell);
        }
        return table;
    }

    private static Paragraph paragraph(String text, Font font, float spacingAfter) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(spacingAfter);
        return p;
    }

    private static String fill(String template, Map<String, String> values) {
        String result = template;
        for (var entry : values.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static String formatSalary(BigDecimal salary) {
        if (salary == null) {
            return "to be defined";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(salary) + " " + CURRENCY;
    }

    private static String orDefault(String value) {
        return (value == null || value.isBlank()) ? "General" : value;
    }

    private static String slug(String value) {
        String s = value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return s.isEmpty() ? "employee" : s;
    }
}
