package io.camunda.google.model;

import java.io.File;

public class Mail {
    
    private String[] to;
    
    private String[] cc;
    
    private String[] bcc;
    
    private String subject;
    
    private String body;
    
    private File[] attachments;

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public String[] getCc() {
        return cc;
    }

    public void setCc(String... cc) {
        this.cc = cc;
    }

    public String[] getBcc() {
        return bcc;
    }

    public void setBcc(String... bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public File[] getAttachments() {
        return attachments;
    }

    public void setAttachments(File[] attachments) {
        this.attachments = attachments;
    }
    
    public static class Builder {
        
        private String[] to;
        private String[] cc;
        private String[] bcc;
        private String subject;
        private String body;
        private File[] attachments;

        public Builder to(String... to) {
            this.to = simplify(to);
            return this;
        }

        public Builder cc(String... cc) {
            this.cc = simplify(cc);
            return this;
        }

        public Builder bcc(String... bcc) {
            this.bcc = simplify(bcc);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder attachments(File... attachments) {
            this.attachments = attachments;
            return this;
        }
        
        public Mail build() {
            Mail mail = new Mail();
            mail.to = to;
            mail.cc = cc;
            mail.bcc = bcc;
            mail.subject = subject;
            mail.body = body;
            mail.attachments = attachments;
            
            return mail;
        }
        
        private String[] simplify(String... array) {
            if (array.length==1 && array[0]==null) {
                return null;
            }
            return array;
        }
    }
}
