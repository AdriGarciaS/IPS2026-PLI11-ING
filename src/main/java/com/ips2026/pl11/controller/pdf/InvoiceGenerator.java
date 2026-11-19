package com.ips2026.pl11.controller.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class InvoiceGenerator {


    public record InvoiceLine(int amount, String product, BigDecimal price) {
    }

    public record InvoiceData(String customerName, String customerId, String customerEmail, String sellerName,
        String sellerId, String saleType, String sellerEmail, LocalDateTime dateTime, String code,
        List<InvoiceLine> lines) {

        public BigDecimal total() {
            return lines.stream().map(InvoiceLine::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public static void generateInvoicePdf(String filePath, String htmlContent) throws IOException {
        try (OutputStream os = new FileOutputStream(filePath)) {
            renderPdf(htmlContent, os);
        }
    }

    public static void generateTicketInvoicePdf(String filePath, String htmlContent) throws IOException {
        generateInvoicePdf(filePath, htmlContent);
    }

    public static byte[] generateInvoicePdfBytes(String htmlContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderPdf(htmlContent, baos);
        return baos.toByteArray();
    }

    private static void renderPdf(String htmlContent, OutputStream os) throws IOException {
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();
            
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not render invoice PDF", e);
        }
    }

    public static String buildInvoiceHtml(InvoiceData d) {
        StringBuilder rows = new StringBuilder();
        int i = 0;
        for (InvoiceLine l : d.lines()) {
            String rowClass = "";
            if (i++ % 2 == 1) {
                rowClass = " class=\"alt\"" ;
            }
            
            rows.append("<tr").append(rowClass).append("><td>").append(l.amount()).append("</td><td>")
                .append(esc(l.product()).replace("\n", "<br/>")).append("</td><td class=\"right\">")
                .append(money(l.price())).append("</td></tr>\n");
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
              "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <meta charset="UTF-8"/>
              <style>
                @page { size: A4; margin: 15mm 18mm; }
                body { font-family: Helvetica, Arial, sans-serif; font-size: 12pt; color: #000; }
                table { width: 100%%; border-collapse: collapse; }
                td, th { vertical-align: top; }
                .header td { vertical-align: middle; }
                .header .logo-cell { width: 40%%; text-align: right; padding-right: 15px; }
                .header .title-cell { text-align: left; }
                .logo { width: 150px; }
                .club { font-size: 26pt; font-weight: bold; letter-spacing: 1px; margin: 0; }
                .subtitle { font-size: 16pt; margin: 0; }
                h1 { font-size: 19pt; margin: 80px 0 25px 0; }
                .info { margin-left: 25px; }
                .info td { width: 33%%; line-height: 1.5; font-size: 10pt; }
                .info h3 { font-size: 12pt; margin: 0 0 4px 0; }
                .items { margin-top: 45px; }
                .items tr.alt td { background: #c9d3f0; }
                .items th { background: #000; color: #fff; text-align: left; padding: 8px 10px; }
                .items td { padding: 8px 10px; font-size: 11pt; }
                .items .col-amount { width: 30%%; }
                .items .col-price { width: 20%%; }
                .right { text-align: right; }
                .total td { border-top: 1px solid #000; font-weight: bold; padding-top: 10px; }
                .total .value { font-weight: normal; }
              </style>
            </head>
            <body>
              <table class="header">
                <tr>
                  <td class="logo-cell">%s</td>
                  <td class="title-cell">
                    <p class="club">IPSWICH TOWN</p>
                    <p class="subtitle">club management</p>
                  </td>
                </tr>
              </table>

              <h1>INVOICE:</h1>

              <table class="info">
                <tr>
                  <td>
                    <h3>Billed to:</h3>
                    %s<br/>%s<br/>%s
                  </td>
                  <td>
                    <h3>From:</h3>
                    %s<br/>%s<br/>%s<br/>%s
                  </td>
                  <td>
                    <h3>Sale information:</h3>
                    Date: %s<br/>Time: %s<br/>Code: %s
                  </td>
                </tr>
              </table>

              <table class="items">
                <thead>
                  <tr>
                    <th class="col-amount">Amount</th>
                    <th>Product</th>
                    <th class="col-price right">Price</th>
                  </tr>
                </thead>
                <tbody>
                  %s
                  <tr class="total">
                    <td colspan="2" class="right" style="padding-right:40px;">TOTAL:</td>
                    <td class="right value">%s</td>
                  </tr>
                </tbody>
              </table>
            </body>
            </html>
            """.formatted(logoImgTag(), esc(d.customerName()), esc(d.customerId()), esc(d.customerEmail()),
            esc(d.sellerName()), esc(d.sellerId()), esc(d.saleType()), esc(d.sellerEmail()),
            d.dateTime().format(dateFmt), d.dateTime().format(timeFmt), esc(d.code()), rows, money(d.total()));
    }

    private static String money(BigDecimal value) {
        return String.format(Locale.forLanguageTag("es-ES"), "%,.2f \u20AC", value);
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String logoImgTag() {
        try (InputStream in = InvoiceGenerator.class.getResourceAsStream("/images/ipswich.png")) {
            if (in == null) {
                return "";
            }
            String b64 = Base64.getEncoder().encodeToString(in.readAllBytes());
            return "<img class=\"logo\" src=\"data:image/png;base64," + b64 + "\" alt=\"Ipswich Town\"/>";
        } catch (IOException e) {
            return "";
        }
    }
}