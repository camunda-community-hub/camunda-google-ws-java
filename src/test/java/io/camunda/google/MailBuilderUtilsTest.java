package io.camunda.google;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.camunda.google.model.Mail;

public class MailBuilderUtilsTest {

    @Test
    public void buildMailBody() {
        String body = MailBuilderUtils.buildMailBody("testMail", Map.of("username", "blop"), Locale.ENGLISH);
        Assertions.assertTrue(body.contains("<span>blop</span>"), body);
        Pattern datePatten = Pattern.compile("Date <span>[0-9]{2}/[0-9]{2}/[0-9]{4}");
        Matcher matcherDate = datePatten.matcher(body);
        Assertions.assertTrue(matcherDate.find(), body);
        Pattern dateTimePatten = Pattern.compile("Date time <span>[0-9]{2}/[0-9]{2}/[0-9]{4} - [0-9]{2}:[0-9]{2}:[0-9]{2}");
        Matcher matcherDateTime = dateTimePatten.matcher(body);
        Assertions.assertTrue(matcherDateTime.find(), body);
    }
    
    @Test
    public void buildMimeMessageWithoutAttachment() throws MessagingException, IOException {
        Mail mail = new Mail.Builder().from("toto@toto.com").to("tata@tata.com").subject("sub").body("body").build();
        MimeMessage mimeMessage = MailBuilderUtils.buildMimeMessage(mail);
        String contentType = mimeMessage.getContentType();

        Assertions.assertFalse(mimeMessage.getContent() instanceof Multipart, "should not be multipart");

        Assertions.assertTrue(mimeMessage.getFrom()[0].toString().equals("toto@toto.com"), "sender is toto");
        Assertions.assertTrue(mimeMessage.getAllRecipients()[0].toString().equals("tata@tata.com"), "receiver is tata");
        Assertions.assertTrue(mimeMessage.getSubject().equals("sub"), "subject is sub");
        Assertions.assertTrue(mimeMessage.getContent().equals("body"), "Body is body");
    }
    
    @Test
    public void buildMimeMessageWithAttachment() throws MessagingException, IOException {
        Mail mail = new Mail.Builder().from("toto@toto.com").to("tata@tata.com").subject("sub").body("body").attachments(new File("test.txt")).build();
        MimeMessage mimeMessage = MailBuilderUtils.buildMimeMessage(mail);

        Assertions.assertTrue(mimeMessage.getFrom()[0].toString().equals("toto@toto.com"), "sender is toto");
        Assertions.assertTrue(mimeMessage.getAllRecipients()[0].toString().equals("tata@tata.com"), "receiver is tata");
        Assertions.assertTrue(mimeMessage.getSubject().equals("sub"), "subject is sub");

        Assertions.assertTrue(mimeMessage.getContent() instanceof Multipart, "should be multipart");

        Multipart multiPart = (Multipart) mimeMessage.getContent();

        Assertions.assertTrue(multiPart.getCount()==2, "Message should have 2 parts");
        for (int i = 0; i < multiPart.getCount(); i++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                Assertions.assertTrue("test.txt".equals(part.getDataHandler().getDataSource().getName()));
            } else {
                String content = new String(part.getDataHandler().getDataSource().getInputStream().readAllBytes());
                Assertions.assertTrue(content.contains("body"), "mail should contain body");
            }
        }
    }
}
