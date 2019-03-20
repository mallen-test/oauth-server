package org.mallen.test.oauth.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableConfigurationProperties(ReqRespLoggingProperties.class)
public class TimaWebCommonConfig extends WebMvcConfigurerAdapter {
    @Bean
    public ReqRespLoggingFilter loggingFilter(@Autowired ReqRespLoggingProperties reqRespLoggingProperties) {
        return new ReqRespLoggingFilter(reqRespLoggingProperties);
    }
}