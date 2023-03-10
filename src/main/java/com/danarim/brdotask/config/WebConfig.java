package com.danarim.brdotask.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;

/**
 * Application configuration.
 */
@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            DEFAULT_LOCALE
    );

    public static final String API_V1_PREFIX = "/api/v1";

    private static final List<String> FRONTEND_URLS = List.of(
            "/",
            "/{x:[\\w\\-]+}",
            "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}"
    );

    private static final List<String> MESSAGES = List.of(
            "validation",
            "errors"
    );

    private static final List<String> SQL_INIT_SCRIPTS = List.of(
            "data-roles.sql",
            "data-users.sql"
    );

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String url : FRONTEND_URLS) {
            registry.addViewController(url).setViewName("forward:/index.html");
        }
    }

    /**
     * Bean for locale resolver.
     *
     * @return LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(DEFAULT_LOCALE);
        return localeResolver;
    }

    /**
     * Bean for message source.
     *
     * @return MessageSource
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        String[] baseNames =
                MESSAGES.stream().map(name -> "classpath:/i18n/" + name).toArray(String[]::new);

        messageSource.setBasenames(baseNames);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Bean for data source. It is used for database initialization.
     *
     * @param dataSource DataSource
     *
     * @return DataSourceInitializer
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(
            @Qualifier("dataSource") DataSource dataSource
    ) {
        ResourceDatabasePopulator resourceDbPopulator = new ResourceDatabasePopulator();

        for (String script : SQL_INIT_SCRIPTS) {
            resourceDbPopulator.addScript(new ClassPathResource("/sql/" + script));
        }

        DataSourceInitializer sourceInitializer = new DataSourceInitializer();
        sourceInitializer.setDataSource(dataSource);
        sourceInitializer.setDatabasePopulator(resourceDbPopulator);
        return sourceInitializer;
    }

}
