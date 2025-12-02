package com.busapi.core.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class XssConfig implements WebMvcConfigurer {

    /**
     * Spring Framework 7.0 için yeni API kullanarak
     * XSS korumalı Jackson JSON converter yapılandırması.
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        SimpleModule xssModule = new SimpleModule("XssSanitizerModule");
        xssModule.addDeserializer(String.class, new HtmlSanitizer());

        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .addModule(xssModule)
                .build();

        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper));
    }
}