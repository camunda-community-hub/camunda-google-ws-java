package io.camunda.google.thymeleaf;

import java.util.Map;
import java.util.Set;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

public class ThymeleafCustomResourceResolver extends StringTemplateResolver {
    private final static String PREFIX = "";

    //@Autowired ThymeleafTemplateDao thymeleaftemplateDao;
    private ITemplateResolver templateResolver;
    
    public ThymeleafCustomResourceResolver(ITemplateResolver templateResolver) {
        setResolvablePatterns(Set.of(PREFIX + "*"));
        this.templateResolver  = templateResolver;
    }

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {

        // ThymeleafTemplate is our internal object that contains the content.  
        // You should change this to match you're set up.

        //ThymeleafTemplateDao thymeleaftemplateDao = ApplicationContextProvider.getApplicationContext().getBean(ThymeleafTemplateDao.class);
        String templateContent = templateResolver.getTemplateContent(template);//thymeleaftemplateDao.findByTemplateName(template);  
        if (templateContent != null) {
            return super.computeTemplateResource(configuration, ownerTemplate, templateContent, templateResolutionAttributes);
        }
        return null;
    }

}
