package io.camunda.google;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import io.camunda.google.model.Mail;
import io.camunda.thymeleaf.feel.TemplateEngineFactory;
import io.camunda.thymeleaf.feel.config.ThymeleafConfig;

public class MailBuilderUtils {

	private static TemplateEngine templateEngine;

	public static void configure() {
		templateEngine = TemplateEngineFactory.getTemplateEngine("MailBuilder");
	}

	public static void configure(ThymeleafConfig config) {
		templateEngine = TemplateEngineFactory.getTemplateEngine("MailBuilder", config);
	}

	public static TemplateEngine getTemplateEngine() {
		if (templateEngine == null) {
			configure();
		}
		return templateEngine;
	}

	public static MimeMessage buildMimeMessage(Mail mail) throws MessagingException, IOException {

		// create MimeMessage
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);
		MimeMessageUtils messageHelper = null;
		if (mail.getAttachments() != null && mail.getAttachments().length > 0) {
			messageHelper = new MimeMessageUtils(email, true);
			for (File attachment : mail.getAttachments()) {
				messageHelper.addAttachment(attachment.getName(), attachment);
			}

		} else {
			messageHelper = new MimeMessageUtils(email, false);
		}
		messageHelper.setFrom("me");
		messageHelper.setTo(mail.getTo());
		if (mail.getBcc() != null && mail.getBcc().length > 0) {
			messageHelper.setBcc(mail.getBcc());
		}
		if (mail.getCc() != null && mail.getCc().length > 0) {
			messageHelper.setCc(mail.getCc());
		}
		messageHelper.setSubject(mail.getSubject());
		messageHelper.setText(mail.getBody(), true);

		return messageHelper.getMimeMessage();
	}

	public static String buildMailBody(String template, Map<String, Object> variables, Locale locale) {
		Context context = new Context();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			context.setVariable(entry.getKey(), entry.getValue());
		}

		return getTemplateEngine().process(template + "-" + locale.getLanguage(), context);
	}
}
