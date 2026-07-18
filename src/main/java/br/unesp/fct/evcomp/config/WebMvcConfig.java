package br.unesp.fct.evcomp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração MVC residual.
 * O AuthInterceptor foi substituído pelo JwtAuthenticationFilter (Spring Security).
 * A configuração de CORS foi migrada para o SecurityConfig.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Configurações de CORS e interceptadores foram migradas para SecurityConfig.java
}

