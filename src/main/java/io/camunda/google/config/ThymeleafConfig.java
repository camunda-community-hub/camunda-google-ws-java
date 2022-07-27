package io.camunda.google.config;

import org.thymeleaf.templatemode.TemplateMode;

public class ThymeleafConfig {

    private TemplateMode mode = TemplateMode.HTML;
    private String encoding = "UTF-8";
    private String prefix = "/templates/";
    private String suffix = ".html";
    private String datePattern = "dd/MM/yyyy";
    private String dateTimePattern = "dd/MM/yyyy - HH:mm:ss";
    private boolean userFeelExpressions = true;
    
    public TemplateMode getMode() {
        return mode;
    }
    public void setMode(TemplateMode mode) {
        this.mode = mode;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public String getDatePattern() {
        return datePattern;
    }
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
    public String getDateTimePattern() {
        return dateTimePattern;
    }
    public void setDateTimePattern(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }
    public boolean isUserFeelExpressions() {
        return userFeelExpressions;
    }
    public void setUserFeelExpressions(boolean userFeelExpressions) {
        this.userFeelExpressions = userFeelExpressions;
    }
}
