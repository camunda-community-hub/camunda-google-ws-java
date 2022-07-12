package io.camunda.google.config;

import org.thymeleaf.templatemode.TemplateMode;

public class ThymeleafConfig {

    private TemplateMode mode = TemplateMode.HTML;
    private String encoding = "UTF-8";
    private String prefix = "/templates/";
    private String suffix = ".html";
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
}
