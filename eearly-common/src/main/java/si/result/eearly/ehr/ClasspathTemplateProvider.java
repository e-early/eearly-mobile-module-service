package si.result.eearly.ehr;

import org.apache.xmlbeans.XmlException;
import org.ehrbase.openehr.sdk.util.exception.SdkException;
import org.ehrbase.openehr.sdk.webtemplate.templateprovider.TemplateProvider;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.openehr.schemas.v1.TemplateDocument;
import org.openehr.schemas.v1.TemplateDocument.Factory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClasspathTemplateProvider implements TemplateProvider {
  private static final String TEMPLATE_PATH = "templates/*.opt";
  private final Map<String, Resource> templateMap = new HashMap<>();

  public ClasspathTemplateProvider() {
    sync();
  }

  private void sync() {
    try {
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:" + TEMPLATE_PATH);

      for (Resource resource : resources) {
        OPERATIONALTEMPLATE template = readTemplate(resource);
        templateMap.put(template.getTemplateId().getValue(), resource);
      }
    } catch (IOException e) {
      throw new SdkException("Failed to load templates from resources", e);
    }
  }

  private OPERATIONALTEMPLATE readTemplate(Resource resource) {
    try (InputStream in = resource.getInputStream()) {
      TemplateDocument document = Factory.parse(in);
      return document.getTemplate();
    } catch (XmlException | IOException e) {
      throw new SdkException("Error parsing template: " + resource.getFilename(), e);
    }
  }

  @Override
  public Optional<OPERATIONALTEMPLATE> find(String templateId) {
    return Optional.ofNullable(templateMap.get(templateId)).map(this::readTemplate);
  }
}
