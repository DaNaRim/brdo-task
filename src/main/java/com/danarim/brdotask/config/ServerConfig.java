package com.danarim.brdotask.config;

import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat configuration.
 */
@Configuration
public class ServerConfig {

    /**
     * Bean for customizing the Tomcat server. This is needed to allow cookies with json.
     *
     * @return WebServerFactoryCustomizer
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return container -> container.addContextCustomizers(context -> {
            context.setCookieProcessor(new LegacyCookieProcessor());
        });
    }

}
