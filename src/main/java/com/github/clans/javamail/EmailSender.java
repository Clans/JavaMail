package com.github.clans.javamail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Properties;

public class EmailSender {

    public void sendEmail(String username, String password, String recipientEmail, String filename) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress("from@no-spam.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject("Testing Subject");

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("This is message body text.", "text/plain");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Attachment
            if (filename != null) {
                MimeBodyPart attachment = new MimeBodyPart();
//                DataSource source = new FileDataSource(FILENAME);
//                attachment.setDataHandler(new DataHandler(source));
//                attachment.setFileName(source.getName());
                attachment.attachFile(filename);
                multipart.addBodyPart(attachment);
            }

            // Send the complete message parts
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Message to " + recipientEmail + " sent successfully.");
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
