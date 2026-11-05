package com.ips2026.pl11.controller.email;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import com.ips2026.pl11.model.store.CartLine;
import com.ips2026.pl11.model.store.SaleReceipt;
import com.ips2026.pl11.view.common.MoneyFormat;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String host;
    private final int port;
    private final String user;
    private final String password;

    public EmailController() {
        this(env("MAIL_HOST", "smtp.gmail.com"), Integer.parseInt(env("MAIL_PORT", "587")), System.getenv("MAIL_USER"),
            System.getenv("MAIL_PASSWORD"));
    }

    public EmailController(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void send(String to, String subject, String htmlBody) throws MessagingException {
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Email is not configured (MAIL_USER / MAIL_PASSWORD).");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    public void sendReceipt(String to, SaleReceipt receipt) throws MessagingException {
        send(to, "Purchase receipt - Sale nº " + receipt.getSaleNumber(), buildReceiptHtml(receipt));
    }

    private static String buildReceiptHtml(SaleReceipt receipt) {
        StringBuilder html = new StringBuilder("<html><body style='font-family: Arial, sans-serif'>");

        html.append("<h2>Thank you for your purchase</h2>").append("<p><b>Sale nº ").append(receipt.getSaleNumber())
            .append("</b> &mdash; ").append(receipt.getDate().format(DATE_FORMAT)).append(" - ")
            .append(TIME_FORMAT.format(LocalTime.now())).append("</p>")
            .append("<table cellpadding='6' style='border-collapse: collapse'>");

        for (CartLine line : receipt.getLines()) {
            html.append("<tr><td>").append(line.getUnits()).append(" x ")
                .append(escapeHtml(line.getMerchandise().getName())).append("</td><td align='right'>")
                .append(MoneyFormat.format(line.getSubtotal())).append("</td></tr>");
        }

        html.append("</table><hr><p><b>FINAL PRICE: ").append(MoneyFormat.format(receipt.getFinalPrice()))
            .append("</b></p><p>Receipt code: ").append(generateTicketCode("SLE")).append("</p></body></html>");
        return html.toString();
    }

    public void sendTicketReceipt(String to, String matchName, String grandstand, String section, List<String> seats,
        BigDecimal price) throws MessagingException {
        StringBuilder html = new StringBuilder("<html><body style='font-family: Arial, sans-serif'>");

        html.append("<h2>Thank you for your purchase</h2><p><b>").append(escapeHtml(matchName))
            .append("</b></p><p>Purchase time: ").append(escapeHtml(DATE_FORMAT.format(LocalDate.now()))).append(" - ")
            .append(escapeHtml(TIME_FORMAT.format(LocalTime.now()))).append("</p>").append("<p>Grandstand ")
            .append(escapeHtml(grandstand)).append(", Section ").append(escapeHtml(section)).append("</p><ul>");

        for (String seat : seats) {
            html.append("<li>").append(escapeHtml(seat)).append("</li>");
        }

        html.append("</ul><hr><p><b>FINAL PRICE: ").append(MoneyFormat.format(price)).append("</b></p>");
        html.append("<p>Receipt code: ").append(generateTicketCode("TCK")).append("</p>");
        html.append("</body></html>");

        send(to, "Ticket receipt - " + matchName, html.toString());
    }

    public void sendSeasonPassReceipt(String to, String dni, String fullName, String team, String grandstand,
        String section, int i, int j, String ageCategory, double basePrice) throws MessagingException {
        StringBuilder html = new StringBuilder("<html><body style='font-family: Arial, sans-serif'>");
        
        html.append("<h2>Thank you for your purchase</h2><ul><li>Customer name: <b>").append(escapeHtml(fullName))
            .append("</b></li><li>Customer DNI: ").append(escapeHtml(dni))
            .append("</li><li>Purchase time: ").append(escapeHtml(DATE_FORMAT.format(LocalDate.now()))).append(" - ")
            .append(escapeHtml(TIME_FORMAT.format(LocalTime.now())))
            .append("</li></ul><p>Team: ")
            .append(escapeHtml(team)).append("</p><p>Grandstand: ").append(escapeHtml(grandstand))
            .append(", Section: ").append(escapeHtml(section)).append("</p><p>Seat: Row ")
            .append(i).append(", Seat ").append(j).append("</p><p>Age category: ")
            .append(escapeHtml(ageCategory)).append("</p><hr><p><b>FINAL PRICE: ")
            .append(MoneyFormat.format(BigDecimal.valueOf(basePrice)))
            .append("</b></p><p>Receipt code: ").append(generateTicketCode("SSP")).append("</p>");
        
        send(to, "Season Pass receipt - " + fullName, html.toString());
    }

    private static String generateTicketCode(String string) {
        long timestamp = System.currentTimeMillis();
        return string + "-" + timestamp;
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
