package io.camunda.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

import io.camunda.google.model.Attachment;
import io.camunda.google.model.Mail;
import io.camunda.google.model.ReceivedMail;

public class GmailUtils {

	private static Gmail service;

	public static Gmail gmail() {
		// Build a new authorized API client service.
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			service = new Gmail.Builder(HTTP_TRANSPORT, GoogleAuthUtils.JSON_FACTORY,
			        GoogleAuthUtils.getCredentials(HTTP_TRANSPORT))
			        .setApplicationName(GoogleAuthUtils.getGoogleWsConfig().getApplicationName()).build();
			return service;
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Gmail getService() {
		if (service == null) {
			service = gmail();
		}
		return service;
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

		Gmail service = getService();

		MimeMessage mimeMessage = MailBuilderUtils.buildMimeMessage(mail);

		Message gmailMessage = GmailUtils.convertToGmailMessage(mimeMessage);

		gmailMessage = service.users().messages().send("me", gmailMessage).execute();

	}

	public static List<Message> readMessages() throws MessagingException, IOException {
		return readMessages(null, null);
	}

	public static List<Message> readMessages(String folderName) throws MessagingException, IOException {
		return readMessages(folderName, null);
	}

	public static List<Message> readMessages(boolean unread) throws MessagingException, IOException {
		return readMessages(null, unread);
	}

	public static List<Message> readMessages(String folderName, Boolean unread) throws MessagingException, IOException {
		Gmail service = getService();
		String q = "";
		if (folderName != null) {
			q = "label:" + folderName;
		}
		if (unread != null) {
			q += " is:" + (unread ? "unread" : "read");
		}
		ListMessagesResponse response = service.users().messages().list("me").setQ(q).execute();

		return response.getMessages();
	}

	public static List<Label> getLabels() throws IOException {
		Gmail service = getService();
		ListLabelsResponse listResponse = service.users().labels().list("me").execute();
		return listResponse.getLabels();
	}

	public static ReceivedMail read(String messageId) throws IOException {
		Gmail service = getService();
		Message m = service.users().messages().get("me", messageId).execute();
		ReceivedMail mail = getContent(m);
		return mail;
	}

	public static void delete(Message m) throws IOException {
		getService().users().messages().delete("me", m.getId()).execute();
	}

	public static void trash(Message m) throws IOException {
		getService().users().messages().trash("me", m.getId()).execute();
	}

	public static ReceivedMail getContent(Message message) throws IOException {
		ReceivedMail mail = new ReceivedMail();

		List<MessagePartHeader> headers = message.getPayload().getHeaders();
		for (MessagePartHeader h : headers) {
			if (h.getName().equals("Subject")) {
				mail.setSubject(h.getValue());
			} else if (h.getName().equals("From")) {
				mail.setFrom(h.getValue());
			} else if (h.getName().equals("To")) {
				mail.setTo(new String[] { h.getValue() });
			}
		}
		return getBodyParts(message, mail);
	}

	private static ReceivedMail getBodyParts(Message message, ReceivedMail mail) throws IOException {
		if (message.getPayload().getParts() == null) {
			getMailContent(message.getPayload(), mail);
		} else {
			List<MessagePart> messageParts = message.getPayload().getParts();
			for (MessagePart messagePart : messageParts) {
				if (messagePart.getFilename().equals("")) {
					getMailContent(messagePart, mail);
				} else {
					getAttachment(message.getId(), messagePart, mail);
				}
			}
		}
		return mail;
	}

	private static ReceivedMail getMailContent(MessagePart messagePart, ReceivedMail mail) {
		if (messagePart.getMimeType().equals("text/plain")) {
			if (mail.getBody() == null) {
				mail.setBody(new String(messagePart.getBody().decodeData(), StandardCharsets.UTF_8));
			}
			return mail;
		}
		if (messagePart.getMimeType().equals("text/html") || messagePart.getMimeType().equals("text/x-amp-html")) {
			mail.setBody(new String(messagePart.getBody().decodeData(), StandardCharsets.UTF_8));
			return mail;
		}
		List<MessagePart> bodyParts = messagePart.getParts();
		for (MessagePart part : bodyParts) {
			if (part.getMimeType().equals("text/html")) {
				mail.setBody(new String(part.getBody().decodeData(), StandardCharsets.UTF_8));
				break;
			}
			if (part.getMimeType().equals("text/plain")) {
				mail.setBody(new String(part.getBody().decodeData(), StandardCharsets.UTF_8));
			}
		}
		return mail;
	}

	private static ReceivedMail getAttachment(String messageId, MessagePart messagePart, ReceivedMail mail)
	        throws IOException {
		Attachment attachment = new Attachment();
		attachment.setContentType(messagePart.getMimeType());
		attachment.setName(messagePart.getFilename());

		MessagePartBody attachmentBody = getService().users().messages().attachments()
		        .get("me", messageId, messagePart.getBody().getAttachmentId()).execute();
		attachment.setData(attachmentBody.decodeData());
		attachment.setSize(attachmentBody.getSize());
		if (mail.getAttachments() == null) {
			mail.setAttachments(new ArrayList<>());
		}
		mail.getAttachments().add(attachment);
		return mail;
	}
}
