package com.busapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // createdAt ve updatedAt'in otomatik dolmasını sağlar
public class JpaConfig {
}
