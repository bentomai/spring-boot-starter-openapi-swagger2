package io.github.bentomai.customizer;

import io.github.bentomai.util.SpringUniAnnotationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Implement swagger2 @Api, @ApiIgnore, @ApiModel, @ApiModelProperty recognition
 * @author Bento Mai
 * @see  OpenApiCustomizer customize default OpenAPI description but not groups
 */
public class OpenApiTagCustomizer implements GlobalOpenApiCustomizer{

    private static ApplicationContext applicationContext;

    /**
     * Accept Spring context
     * @param applicationContext spring application context
     */
    public OpenApiTagCustomizer(ApplicationContext applicationContext) {
        OpenApiTagCustomizer.applicationContext = applicationContext;
    }

    @Override
    public void customise(OpenAPI openApi) {

        // Retrieve all beans containing @RestController from the Spring application context
        String[] restControllerBeanNames = OpenApiTagCustomizer.applicationContext.getBeanNamesForAnnotation(RestController.class);
        Set<String> processedTags = new HashSet<>(restControllerBeanNames.length);

        Arrays.stream(restControllerBeanNames).forEach((beanName)->{
            Class<?> restControllerClz = OpenApiTagCustomizer.applicationContext.getType(beanName);
            // the bean have @ApiIgnore
            if(restControllerClz!=null && !SpringUniAnnotationUtil.isAnnotation(restControllerClz, ApiIgnore.class)){

                Api apiAnnotation = AnnotationUtils.findAnnotation(restControllerClz, Api.class);
                if(apiAnnotation!=null && !apiAnnotation.hidden()){

                    String[] apiTagNames = apiAnnotation.tags();

                    if(apiTagNames.length==0 && !apiAnnotation.value().isEmpty()){
                        apiTagNames = new String[]{apiAnnotation.value()};
                    }
                    for (String apiTagName : apiTagNames) {
                        if(!processedTags.contains(apiTagName)){
                            Tag tag = new Tag().name(apiTagName).description(apiAnnotation.value());
                            openApi.addTagsItem(tag);
                            processedTags.add(apiTagName);
                        }
                    }

                }
            }
        });

        // Process all schemas
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {

            SpringDocConfigProperties springDocConfigProperties = applicationContext.getBean(SpringDocConfigProperties.class);

            List<String> allPackageScans = new ArrayList<>();

            springDocConfigProperties.getGroupConfigs().stream().map(SpringDocConfigProperties.GroupConfig::getPackagesToScan).forEach(allPackageScans::addAll);

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .forPackages(allPackageScans.toArray(new String[allPackageScans.size()]))
                    .addScanners(Scanners.TypesAnnotated));

            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(io.swagger.annotations.ApiModel.class);
            for (Class<?> clazz : annotatedClasses) {
                io.swagger.annotations.ApiModel apiModel = clazz.getAnnotation(io.swagger.annotations.ApiModel.class);

                String modelName = clazz.getSimpleName();

                if (apiModel != null) {
                    Schema<?> schema = openApi.getComponents().getSchemas().get(clazz.getSimpleName());
                    if(schema == null){
                        schema = new Schema<>();
                    }

                    schema.setType("object");
                    if(StringUtils.isBlank(schema.getDescription())){
                        schema.setDescription(apiModel.description());
                    }

                    if(StringUtils.isBlank(schema.getName())){
                        schema.setName(modelName);
                    }

                    // Processing attribute @ ApiModelProperty annotation
                    for (Field field : clazz.getDeclaredFields()) {
                        ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
                        if(apiModelProperty == null || apiModelProperty.hidden()){
                            continue;
                        }

                        Schema<?> originalPropertySchema = Optional.ofNullable(schema.getProperties()).orElse(new HashMap<>(0)).get(field.getName());
                        if(originalPropertySchema==null){
                            Schema<?> propertySchema = new Schema<>();
                            propertySchema.setType(field.getType().getSimpleName());
                            propertySchema.setTitle(apiModelProperty.value());
                            if(StringUtils.isBlank(propertySchema.getTitle())){
                                propertySchema.setTitle(apiModelProperty.name());
                            }
                            propertySchema.setDescription(apiModelProperty.notes());
                            propertySchema.setExample(apiModelProperty.example());

                            originalPropertySchema = propertySchema;
                        }

                        if(apiModelProperty.required()){
                            schema.addRequiredItem(field.getName());
                        }

                        if(StringUtils.isBlank(originalPropertySchema.getTitle()) && StringUtils.isNoneBlank(apiModelProperty.value())){
                            originalPropertySchema.setTitle(apiModelProperty.value());
                        }else if(StringUtils.isBlank(originalPropertySchema.getTitle()) && StringUtils.isNoneBlank(apiModelProperty.name())){
                            originalPropertySchema.setTitle(apiModelProperty.name());
                        }

                        if(StringUtils.isBlank(originalPropertySchema.getType())){
                            originalPropertySchema.setType(field.getType().getSimpleName());
                        }

                        if(StringUtils.isBlank(originalPropertySchema.getDescription()) && StringUtils.isNoneBlank(apiModelProperty.notes())){
                            originalPropertySchema.setDescription(apiModelProperty.notes());
                        }

                        if(originalPropertySchema.getExample() == null && StringUtils.isNoneBlank(apiModelProperty.example())){
                            originalPropertySchema.setExample(apiModelProperty.example());
                        }

                        schema.addProperty(field.getName(), originalPropertySchema);

                    }

                    // Add schema to OpenAPI document
                    openApi.getComponents().addSchemas(modelName, schema);
                }
            }

        }

    }
}
