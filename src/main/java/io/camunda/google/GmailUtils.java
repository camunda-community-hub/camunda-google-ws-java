package io.camunda.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import io.camunda.google.model.Mail;
import io.camunda.google.thymeleaf.MailBuilderUtils;

public class GmailUtils {
    
    public static Gmail gmail() {
        // Build a new authorized API client service.
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, GoogleAuthUtils.JSON_FACTORY, GoogleAuthUtils.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(GoogleAuthUtils.getGoogleWsConfig().getApplicationName())
                .build();
            return service;
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Message convertToGmailMessage(MimeMessage mimeMessage) throws IOException, MessagingException {
        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
    
    public static void sendEmail(Mail mail) throws MessagingException, IOException {

        Gmail service = gmail();
        
        MimeMessage mimeMessage = MailBuilderUtils.buildMimeMessage(mail);
        
        Message gmailMessage = GmailUtils.convertToGmailMessage(mimeMessage);

        gmailMessage = service.users().messages().send("me", gmailMessage).execute();

    }
}
