package io.camunda.google;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.annotation.Nullable;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

/**
 * Copied and simplified from Spring MimeMessageHelper
 */
public class MimeMessageUtils {

    /**
     * Constant indicating a non-multipart message.
     */
    public static final int MULTIPART_MODE_NO = 0;

    /**
     * Constant indicating a multipart message with a single root multipart
     * element of type "mixed". Texts, inline elements and attachements
     * will all get added to that root element.
     * <p>This was Spring 1.0's default behavior. It is known to work properly
     * on Outlook. However, other mail clients tend to misinterpret inline
     * elements as attachments and/or show attachments inline as well.
     */
    public static final int MULTIPART_MODE_MIXED = 1;

    /**
     * Constant indicating a multipart message with a single root multipart
     * element of type "related". Texts, inline elements and attachements
     * will all get added to that root element.
     * <p>This was the default behavior from Spring 1.1 up to 1.2 final.
     * This is the "Microsoft multipart mode", as natively sent by Outlook.
     * It is known to work properly on Outlook, Outlook Express, Yahoo Mail, and
     * to a large degree also on Mac Mail (with an additional attachment listed
     * for an inline element, despite the inline element also shown inline).
     * Does not work properly on Lotus Notes (attachments won't be shown there).
     */
    public static final int MULTIPART_MODE_RELATED = 2;

    /**
     * Constant indicating a multipart message with a root multipart element
     * "mixed" plus a nested multipart element of type "related". Texts and
     * inline elements will get added to the nested "related" element,
     * while attachments will get added to the "mixed" root element.
     * <p>This is the default since Spring 1.2.1. This is arguably the most correct
     * MIME structure, according to the MIME spec: It is known to work properly
     * on Outlook, Outlook Express, Yahoo Mail, and Lotus Notes. Does not work
     * properly on Mac Mail. If you target Mac Mail or experience issues with
     * specific mails on Outlook, consider using MULTIPART_MODE_RELATED instead.
     */
    public static final int MULTIPART_MODE_MIXED_RELATED = 3;


    private static final String MULTIPART_SUBTYPE_MIXED = "mixed";

    private static final String MULTIPART_SUBTYPE_RELATED = "related";

    private static final String MULTIPART_SUBTYPE_ALTERNATIVE = "alternative";

    private static final String CONTENT_TYPE_ALTERNATIVE = "text/alternative";

    private static final String CONTENT_TYPE_HTML = "text/html";

    private static final String CONTENT_TYPE_CHARSET_SUFFIX = ";charset=";

    private static final String HEADER_PRIORITY = "X-Priority";

    private final MimeMessage mimeMessage;

    private MimeMultipart rootMimeMultipart;

    private MimeMultipart mimeMultipart;

    private final String encoding;

    private boolean encodeFilenames = false;


    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * assuming a simple text message (no multipart content,
     * i.e. no alternative texts and no inline elements or attachments).
     * <p>The character encoding for the message will be taken from
     * the passed-in MimeMessage object, if carried there. Else,
     * JavaMail's default encoding will be used.
     * @param mimeMessage the mime message to work on
     * @see #MimeMessageUtils(jakarta.mail.internet.MimeMessage, boolean)
     * @see #getDefaultEncoding(jakarta.mail.internet.MimeMessage)
     * @see JavaMailSenderImpl#setDefaultEncoding
     */
    public MimeMessageUtils(MimeMessage mimeMessage) {
        this(mimeMessage, null);
    }

    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * assuming a simple text message (no multipart content,
     * i.e. no alternative texts and no inline elements or attachments).
     * @param mimeMessage the mime message to work on
     * @param encoding the character encoding to use for the message
     * @see #MimeMessageUtils(jakarta.mail.internet.MimeMessage, boolean)
     */
    public MimeMessageUtils(MimeMessage mimeMessage, String encoding) {
        this.mimeMessage = mimeMessage;
        this.encoding = encoding;
    }

    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * in multipart mode (supporting alternative texts, inline
     * elements and attachments) if requested.
     * <p>Consider using the MimeMessageUtils constructor that
     * takes a multipartMode argument to choose a specific multipart
     * mode other than MULTIPART_MODE_MIXED_RELATED.
     * <p>The character encoding for the message will be taken from
     * the passed-in MimeMessage object, if carried there. Else,
     * JavaMail's default encoding will be used.
     * @param mimeMessage the mime message to work on
     * @param multipart whether to create a multipart message that
     * supports alternative texts, inline elements and attachments
     * (corresponds to MULTIPART_MODE_MIXED_RELATED)
     * @throws MessagingException if multipart creation failed
     * @see #MimeMessageUtils(jakarta.mail.internet.MimeMessage, int)
     * @see #getDefaultEncoding(jakarta.mail.internet.MimeMessage)
     * @see JavaMailSenderImpl#setDefaultEncoding
     */
    public MimeMessageUtils(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
        this(mimeMessage, multipart, null);
    }

    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * in multipart mode (supporting alternative texts, inline
     * elements and attachments) if requested.
     * <p>Consider using the MimeMessageUtils constructor that
     * takes a multipartMode argument to choose a specific multipart
     * mode other than MULTIPART_MODE_MIXED_RELATED.
     * @param mimeMessage the mime message to work on
     * @param multipart whether to create a multipart message that
     * supports alternative texts, inline elements and attachments
     * (corresponds to MULTIPART_MODE_MIXED_RELATED)
     * @param encoding the character encoding to use for the message
     * @throws MessagingException if multipart creation failed
     * @see #MimeMessageUtils(jakarta.mail.internet.MimeMessage, int, String)
     */
    public MimeMessageUtils(MimeMessage mimeMessage, boolean multipart, String encoding)
            throws MessagingException {

        this(mimeMessage, (multipart ? MULTIPART_MODE_MIXED_RELATED : MULTIPART_MODE_NO), encoding);
    }

    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * in multipart mode (supporting alternative texts, inline
     * elements and attachments) if requested.
     * <p>The character encoding for the message will be taken from
     * the passed-in MimeMessage object, if carried there. Else,
     * JavaMail's default encoding will be used.
     * @param mimeMessage the mime message to work on
     * @param multipartMode which kind of multipart message to create
     * (MIXED, RELATED, MIXED_RELATED, or NO)
     * @throws MessagingException if multipart creation failed
     * @see #MULTIPART_MODE_NO
     * @see #MULTIPART_MODE_MIXED
     * @see #MULTIPART_MODE_RELATED
     * @see #MULTIPART_MODE_MIXED_RELATED
     * @see #getDefaultEncoding(jakarta.mail.internet.MimeMessage)
     * @see JavaMailSenderImpl#setDefaultEncoding
     */
    public MimeMessageUtils(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        this(mimeMessage, multipartMode, null);
    }

    /**
     * Create a new MimeMessageUtils for the given MimeMessage,
     * in multipart mode (supporting alternative texts, inline
     * elements and attachments) if requested.
     * @param mimeMessage the mime message to work on
     * @param multipartMode which kind of multipart message to create
     * (MIXED, RELATED, MIXED_RELATED, or NO)
     * @param encoding the character encoding to use for the message
     * @throws MessagingException if multipart creation failed
     * @see #MULTIPART_MODE_NO
     * @see #MULTIPART_MODE_MIXED
     * @see #MULTIPART_MODE_RELATED
     * @see #MULTIPART_MODE_MIXED_RELATED
     */
    public MimeMessageUtils(MimeMessage mimeMessage, int multipartMode, String encoding)
            throws MessagingException {

        this.mimeMessage = mimeMessage;
        createMimeMultiparts(mimeMessage, multipartMode);
        this.encoding = encoding;
    }


    /**
     * Return the underlying MimeMessage object.
     */
    public final MimeMessage getMimeMessage() {
        return this.mimeMessage;
    }


    /**
     * Determine the MimeMultipart objects to use, which will be used
     * to store attachments on the one hand and text(s) and inline elements
     * on the other hand.
     * <p>Texts and inline elements can either be stored in the root element
     * itself (MULTIPART_MODE_MIXED, MULTIPART_MODE_RELATED) or in a nested element
     * rather than the root element directly (MULTIPART_MODE_MIXED_RELATED).
     * <p>By default, the root MimeMultipart element will be of type "mixed"
     * (MULTIPART_MODE_MIXED) or "related" (MULTIPART_MODE_RELATED).
     * The main multipart element will either be added as nested element of
     * type "related" (MULTIPART_MODE_MIXED_RELATED) or be identical to the root
     * element itself (MULTIPART_MODE_MIXED, MULTIPART_MODE_RELATED).
     * @param mimeMessage the MimeMessage object to add the root MimeMultipart
     * object to
     * @param multipartMode the multipart mode, as passed into the constructor
     * (MIXED, RELATED, MIXED_RELATED, or NO)
     * @throws MessagingException if multipart creation failed
     * @see #setMimeMultiparts
     * @see #MULTIPART_MODE_NO
     * @see #MULTIPART_MODE_MIXED
     * @see #MULTIPART_MODE_RELATED
     * @see #MULTIPART_MODE_MIXED_RELATED
     */
    protected void createMimeMultiparts(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        switch (multipartMode) {
            case MULTIPART_MODE_NO:
                setMimeMultiparts(null, null);
                break;
            case MULTIPART_MODE_MIXED:
                MimeMultipart mixedMultipart = new MimeMultipart(MULTIPART_SUBTYPE_MIXED);
                mimeMessage.setContent(mixedMultipart);
                setMimeMultiparts(mixedMultipart, mixedMultipart);
                break;
            case MULTIPART_MODE_RELATED:
                MimeMultipart relatedMultipart = new MimeMultipart(MULTIPART_SUBTYPE_RELATED);
                mimeMessage.setContent(relatedMultipart);
                setMimeMultiparts(relatedMultipart, relatedMultipart);
                break;
            case MULTIPART_MODE_MIXED_RELATED:
                MimeMultipart rootMixedMultipart = new MimeMultipart(MULTIPART_SUBTYPE_MIXED);
                mimeMessage.setContent(rootMixedMultipart);
                MimeMultipart nestedRelatedMultipart = new MimeMultipart(MULTIPART_SUBTYPE_RELATED);
                MimeBodyPart relatedBodyPart = new MimeBodyPart();
                relatedBodyPart.setContent(nestedRelatedMultipart);
                rootMixedMultipart.addBodyPart(relatedBodyPart);
                setMimeMultiparts(rootMixedMultipart, nestedRelatedMultipart);
                break;
            default:
                throw new IllegalArgumentException("Only multipart modes MIXED_RELATED, RELATED and NO supported");
        }
    }

    /**
     * Set the given MimeMultipart objects for use by this MimeMessageUtils.
     * @param root the root MimeMultipart object, which attachments will be added to;
     * or {@code null} to indicate no multipart at all
     * @param main the main MimeMultipart object, which text(s) and inline elements
     * will be added to (can be the same as the root multipart object, or an element
     * nested underneath the root multipart element)
     */
    protected final void setMimeMultiparts(@Nullable MimeMultipart root, @Nullable MimeMultipart main) {
        this.rootMimeMultipart = root;
        this.mimeMultipart = main;
    }

    /**
     * Return whether this helper is in multipart mode,
     * i.e. whether it holds a multipart message.
     * @see #MimeMessageUtils(MimeMessage, boolean)
     */
    public final boolean isMultipart() {
        return (this.rootMimeMultipart != null);
    }

    /**
     * Return the root MIME "multipart/mixed" object, if any.
     * Can be used to manually add attachments.
     * <p>This will be the direct content of the MimeMessage,
     * in case of a multipart mail.
     * @throws IllegalStateException if this helper is not in multipart mode
     * @see #isMultipart
     * @see #getMimeMessage
     * @see jakarta.mail.internet.MimeMultipart#addBodyPart
     */
    public final MimeMultipart getRootMimeMultipart() throws IllegalStateException {
        if (this.rootMimeMultipart == null) {
            throw new IllegalStateException("Not in multipart mode - " +
                    "create an appropriate MimeMessageUtils via a constructor that takes a 'multipart' flag " +
                    "if you need to set alternative texts or add inline elements or attachments.");
        }
        return this.rootMimeMultipart;
    }

    /**
     * Return the underlying MIME "multipart/related" object, if any.
     * Can be used to manually add body parts, inline elements, etc.
     * <p>This will be nested within the root MimeMultipart,
     * in case of a multipart mail.
     * @throws IllegalStateException if this helper is not in multipart mode
     * @see #isMultipart
     * @see #getRootMimeMultipart
     * @see jakarta.mail.internet.MimeMultipart#addBodyPart
     */
    public final MimeMultipart getMimeMultipart() throws IllegalStateException {
        if (this.mimeMultipart == null) {
            throw new IllegalStateException("Not in multipart mode - " +
                    "create an appropriate MimeMessageUtils via a constructor that takes a 'multipart' flag " +
                    "if you need to set alternative texts or add inline elements or attachments.");
        }
        return this.mimeMultipart;
    }

    /**
     * Return the specific character encoding used for this message, if any.
     */
    @Nullable
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Set whether to encode attachment filenames passed to this helper's
     * {@code #addAttachment} methods.
     * <p>The default is {@code false} for standard MIME behavior; turn this to
     * {@code true} for compatibility with older email clients. On a related note,
     * check out JavaMail's {@code mail.mime.encodefilename} system property.
     * <p><b>NOTE:</b> The default changed to {@code false} in 5.3, in favor of
     * JavaMail's standard {@code mail.mime.encodefilename} system property.
     * @since 5.2.9
     * @see #addAttachment(String, DataSource)
     * @see MimeBodyPart#setFileName(String)
     */
    public void setEncodeFilenames(boolean encodeFilenames) {
        this.encodeFilenames = encodeFilenames;
    }

    /**
     * Return whether to encode attachment filenames passed to this helper's
     * {@code #addAttachment} methods.
     * @since 5.2.9
     * @see #setEncodeFilenames
     */
    public boolean isEncodeFilenames() {
        return this.encodeFilenames;
    }

    public void setFrom(InternetAddress from) throws MessagingException {
        this.mimeMessage.setFrom(from);
    }

    public void setFrom(String from) throws MessagingException {
        setFrom(parseAddress(from));
    }

    public void setFrom(String from, String personal) throws MessagingException, UnsupportedEncodingException {
        setFrom(getEncoding() != null ?
            new InternetAddress(from, personal, getEncoding()) : new InternetAddress(from, personal));
    }

    public void setReplyTo(InternetAddress replyTo) throws MessagingException {
        this.mimeMessage.setReplyTo(new InternetAddress[] {replyTo});
    }

    public void setReplyTo(String replyTo) throws MessagingException {
        setReplyTo(parseAddress(replyTo));
    }

    public void setReplyTo(String replyTo, String personal) throws MessagingException, UnsupportedEncodingException {
        InternetAddress replyToAddress = (getEncoding() != null) ?
                new InternetAddress(replyTo, personal, getEncoding()) : new InternetAddress(replyTo, personal);
        setReplyTo(replyToAddress);
    }


    public void setTo(InternetAddress to) throws MessagingException {
        this.mimeMessage.setRecipient(Message.RecipientType.TO, to);
    }

    public void setTo(InternetAddress[] to) throws MessagingException {
        this.mimeMessage.setRecipients(Message.RecipientType.TO, to);
    }

    public void setTo(String to) throws MessagingException {
        setTo(parseAddress(to));
    }

    public void setTo(String[] to) throws MessagingException {
        InternetAddress[] addresses = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            addresses[i] = parseAddress(to[i]);
        }
        setTo(addresses);
    }

    public void setCc(InternetAddress cc) throws MessagingException {
        this.mimeMessage.setRecipient(Message.RecipientType.CC, cc);
    }

    public void setCc(InternetAddress[] cc) throws MessagingException {
        this.mimeMessage.setRecipients(Message.RecipientType.CC, cc);
    }

    public void setCc(String cc) throws MessagingException {
        setCc(parseAddress(cc));
    }

    public void setCc(String[] cc) throws MessagingException {
        InternetAddress[] addresses = new InternetAddress[cc.length];
        for (int i = 0; i < cc.length; i++) {
            addresses[i] = parseAddress(cc[i]);
        }
        setCc(addresses);
    }

    public void setBcc(InternetAddress bcc) throws MessagingException {
        this.mimeMessage.setRecipient(Message.RecipientType.BCC, bcc);
    }

    public void setBcc(InternetAddress[] bcc) throws MessagingException {
        this.mimeMessage.setRecipients(Message.RecipientType.BCC, bcc);
    }

    public void setBcc(String bcc) throws MessagingException {
        setBcc(parseAddress(bcc));
    }

    public void setBcc(String[] bcc) throws MessagingException {
        InternetAddress[] addresses = new InternetAddress[bcc.length];
        for (int i = 0; i < bcc.length; i++) {
            addresses[i] = parseAddress(bcc[i]);
        }
        setBcc(addresses);
    }

    private InternetAddress parseAddress(String address) throws MessagingException {
        InternetAddress[] parsed = InternetAddress.parse(address);
        if (parsed.length != 1) {
            throw new AddressException("Illegal address", address);
        }
        InternetAddress raw = parsed[0];
        try {
            return (getEncoding() != null ?
                    new InternetAddress(raw.getAddress(), raw.getPersonal(), getEncoding()) : raw);
        }
        catch (UnsupportedEncodingException ex) {
            throw new MessagingException("Failed to parse embedded personal name to correct encoding", ex);
        }
    }


    /**
     * Set the priority ("X-Priority" header) of the message.
     * @param priority the priority value;
     * typically between 1 (highest) and 5 (lowest)
     * @throws MessagingException in case of errors
     */
    public void setPriority(int priority) throws MessagingException {
        this.mimeMessage.setHeader(HEADER_PRIORITY, Integer.toString(priority));
    }

    /**
     * Set the sent-date of the message.
     * @param sentDate the date to set (never {@code null})
     * @throws MessagingException in case of errors
     */
    public void setSentDate(Date sentDate) throws MessagingException {
        this.mimeMessage.setSentDate(sentDate);
    }

    /**
     * Set the subject of the message, using the correct encoding.
     * @param subject the subject text
     * @throws MessagingException in case of errors
     */
    public void setSubject(String subject) throws MessagingException {
        if (getEncoding() != null) {
            this.mimeMessage.setSubject(subject, getEncoding());
        }
        else {
            this.mimeMessage.setSubject(subject);
        }
    }


    /**
     * Set the given text directly as content in non-multipart mode
     * or as default body part in multipart mode.
     * Always applies the default content type "text/plain".
     * <p><b>NOTE:</b> Invoke {@link #addInline} <i>after</i> {@code setText};
     * else, mail readers might not be able to resolve inline references correctly.
     * @param text the text for the message
     * @throws MessagingException in case of errors
     */
    public void setText(String text) throws MessagingException {
        setText(text, false);
    }

    /**
     * Set the given text directly as content in non-multipart mode
     * or as default body part in multipart mode.
     * The "html" flag determines the content type to apply.
     * <p><b>NOTE:</b> Invoke {@link #addInline} <i>after</i> {@code setText};
     * else, mail readers might not be able to resolve inline references correctly.
     * @param text the text for the message
     * @param html whether to apply content type "text/html" for an
     * HTML mail, using default content type ("text/plain") else
     * @throws MessagingException in case of errors
     */
    public void setText(String text, boolean html) throws MessagingException {
        MimePart partToUse;
        if (isMultipart()) {
            partToUse = getMainPart();
        }
        else {
            partToUse = this.mimeMessage;
        }
        if (html) {
            setHtmlTextToMimePart(partToUse, text);
        }
        else {
            setPlainTextToMimePart(partToUse, text);
        }
    }

    /**
     * Set the given plain text and HTML text as alternatives, offering
     * both options to the email client. Requires multipart mode.
     * <p><b>NOTE:</b> Invoke {@link #addInline} <i>after</i> {@code setText};
     * else, mail readers might not be able to resolve inline references correctly.
     * @param plainText the plain text for the message
     * @param htmlText the HTML text for the message
     * @throws MessagingException in case of errors
     */
    public void setText(String plainText, String htmlText) throws MessagingException {
        MimeMultipart messageBody = new MimeMultipart(MULTIPART_SUBTYPE_ALTERNATIVE);
        getMainPart().setContent(messageBody, CONTENT_TYPE_ALTERNATIVE);

        // Create the plain text part of the message.
        MimeBodyPart plainTextPart = new MimeBodyPart();
        setPlainTextToMimePart(plainTextPart, plainText);
        messageBody.addBodyPart(plainTextPart);

        // Create the HTML text part of the message.
        MimeBodyPart htmlTextPart = new MimeBodyPart();
        setHtmlTextToMimePart(htmlTextPart, htmlText);
        messageBody.addBodyPart(htmlTextPart);
    }

    private MimeBodyPart getMainPart() throws MessagingException {
        MimeMultipart mimeMultipart = getMimeMultipart();
        MimeBodyPart bodyPart = null;
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bp = mimeMultipart.getBodyPart(i);
            if (bp.getFileName() == null) {
                bodyPart = (MimeBodyPart) bp;
            }
        }
        if (bodyPart == null) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeMultipart.addBodyPart(mimeBodyPart);
            bodyPart = mimeBodyPart;
        }
        return bodyPart;
    }

    private void setPlainTextToMimePart(MimePart mimePart, String text) throws MessagingException {
        if (getEncoding() != null) {
            mimePart.setText(text, getEncoding());
        }
        else {
            mimePart.setText(text);
        }
    }

    private void setHtmlTextToMimePart(MimePart mimePart, String text) throws MessagingException {
        if (getEncoding() != null) {
            mimePart.setContent(text, CONTENT_TYPE_HTML + CONTENT_TYPE_CHARSET_SUFFIX + getEncoding());
        }
        else {
            mimePart.setContent(text, CONTENT_TYPE_HTML);
        }
    }
    

    /**
     * Add an attachment to the MimeMessage, taking the content from a
     * {@code jakarta.activation.DataSource}.
     * <p>Note that the InputStream returned by the DataSource implementation
     * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
     * {@code getInputStream()} multiple times.
     * @param attachmentFilename the name of the attachment as it will
     * appear in the mail (the content type will be determined by this)
     * @param dataSource the {@code jakarta.activation.DataSource} to take
     * the content from, determining the InputStream and the content type
     * @throws MessagingException in case of errors
     * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
     * @see #addAttachment(String, java.io.File)
     */
    public void addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
        try {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDisposition(Part.ATTACHMENT);
            mimeBodyPart.setFileName(isEncodeFilenames() ?
                    MimeUtility.encodeText(attachmentFilename) : attachmentFilename);
            mimeBodyPart.setDataHandler(new DataHandler(dataSource));
            getRootMimeMultipart().addBodyPart(mimeBodyPart);
        }
        catch (UnsupportedEncodingException ex) {
            throw new MessagingException("Failed to encode attachment filename", ex);
        }
    }

    /**
     * Add an attachment to the MimeMessage, taking the content from a
     * {@code java.io.File}.
     * <p>The content type will be determined by the name of the given
     * content file. Do not use this for temporary files with arbitrary
     * filenames (possibly ending in ".tmp" or the like)!
     * @param attachmentFilename the name of the attachment as it will
     * appear in the mail
     * @param file the File resource to take the content from
     * @throws MessagingException in case of errors
     * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
     * @see #addAttachment(String, jakarta.activation.DataSource)
     */
    public void addAttachment(String attachmentFilename, File file) throws MessagingException {
        FileDataSource dataSource = new FileDataSource(file);
        dataSource.setFileTypeMap(FileTypeMap.getDefaultFileTypeMap());
        addAttachment(attachmentFilename, dataSource);
    }




}