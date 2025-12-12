
package service;

import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

 
public class NotificationService {

    private final String SENDER_EMAIL = "dockdisk.clinic@gmail.com";
    private final String APP_PASSWORD = "olkn qoxm cqmo tkzw"  ;

    public void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipientEmail)
        );
        message.setSubject(subject);
        // HTML
        message.setContent(body, "text/html; charset=utf-8"); 

        Transport.send(message);
        System.out.println("✅ Email sent successfully to: " + recipientEmail);
    }
}