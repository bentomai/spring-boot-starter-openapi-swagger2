package io.github.bentomai.customizer;

import io.github.bentomai.util.SpringUniAnnotationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.ActuatorOperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Implement swagger2 @ApiOperation, @ApiResponses, @ApiParam recognition
 * @author Bento Mai
 **/
public class OpenApiOperationCustomizer extends ActuatorOperationCustomizer {

    /**
     * Instantiates a new Actuator operation customizer.
     * @param springDocConfigProperties the spring doc config properties
     */
    public OpenApiOperationCustomizer(SpringDocConfigProperties springDocConfigProperties) {
        super(springDocConfigProperties);
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {

        Class<?> controllerClass = handlerMethod.getBeanType();

        Method methodMethod = handlerMethod.getMethod();

        if(SpringUniAnnotationUtil.isAnnotation(controllerClass, ApiIgnore.class)|| SpringUniAnnotationUtil.isAnnotation(methodMethod, ApiIgnore.class)){
            return null;
        }

        // Processing @ Api annotations at the class level
        Api apiAnnotation = AnnotationUtils.findAnnotation(controllerClass, Api.class);
        if(apiAnnotation!=null){
            String[] tags = apiAnnotation.tags();
            if(tags.length == 0 && ! apiAnnotation.value().isEmpty()){
                tags = new String[]{apiAnnotation.value()};
            }
            operation.setTags(Arrays.asList(tags));
        }


        // Processing @ApiOperation annotations at the method level
        ApiOperation apiOperation = handlerMethod.getMethodAnnotation(ApiOperation.class);
        if(apiOperation!=null){
            operation.summary(apiOperation.value());
            operation.description(apiOperation.notes());
            List<String> apiOperationTags = Arrays.stream(apiOperation.tags()).filter(StringUtils::isNotBlank).toList();
            if(!apiOperationTags.isEmpty()){
                operation.setTags(Arrays.asList(apiOperation.tags()));
            }else if(apiAnnotation!=null){
                operation.setTags(Arrays.stream(apiAnnotation.tags()).toList());
            }
            //Processing @ApiOperation response
            if(apiOperation.response()!=Void.class){
                operation.getResponses().addApiResponse("200", new ApiResponse().description("Successful operation"));
            }
        }

        // Processing @ApiResponses annotations at the method level Processing
        ApiResponses apiResponses = handlerMethod.getMethodAnnotation(ApiResponses.class);
        if(apiResponses!=null){
            for (io.swagger.annotations.ApiResponse apiResponse : apiResponses.value()) {
                operation.getResponses().addApiResponse(String.valueOf(apiResponse.code()),
                        new ApiResponse().description(apiResponse.message()));
            }
        }

        // Processing @ApiParam annotations at the method level Processing
        Parameter[] parameters = handlerMethod.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            ApiParam apiParam = AnnotationUtils.getAnnotation(parameters[i], ApiParam.class);
            if(apiParam!=null && operation.getParameters()!=null && operation.getParameters().size()>i){
                io.swagger.v3.oas.models.parameters.Parameter param = operation.getParameters().get(i);
                param.description(apiParam.value());
                param.required(apiParam.required());
                if(!apiParam.example().isEmpty()){
                    param.example(apiParam.example());
                }
            }
        }

        return operation;
    }
}
