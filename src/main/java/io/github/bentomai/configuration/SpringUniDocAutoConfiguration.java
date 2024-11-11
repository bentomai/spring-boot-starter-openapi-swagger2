package io.github.bentomai.configuration;

import io.github.bentomai.customizer.OpenApiOperationCustomizer;
import io.github.bentomai.customizer.OpenApiTagCustomizer;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;

/**
 * Spring Uni Doc config
 * @author Bento Mai
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class SpringUniDocAutoConfiguration implements ApplicationContextAware{

    private static ApplicationContext applicationContext;

    /**
     * Register for custom GlobalOpenApiCustomizing
     * @return all custom GlobalOpenApiCustomizing
     */
    @Bean
    public List<GlobalOpenApiCustomizer> globalOpenApiCustomizers(){
        OpenApiTagCustomizer openApiTagCustomizer = new OpenApiTagCustomizer(SpringUniDocAutoConfiguration.applicationContext);
        return Collections.singletonList(openApiTagCustomizer);
    }

    /**
     * Register for custom ActuatorOperationCustomizer
     * @param springDocConfigProperties spring doc config properties
     * @return all custom ActuatorOperationCustomizer
     */
    @Bean
    public List<GlobalOperationCustomizer> globalOperationCustomizers(SpringDocConfigProperties springDocConfigProperties){
        return Collections.singletonList(new OpenApiOperationCustomizer(springDocConfigProperties));
    }

    /**
     * Monitor the container for instantiated beans
     * @param applicationContext spring application context
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUniDocAutoConfiguration.applicationContext = applicationContext;
    }
}
