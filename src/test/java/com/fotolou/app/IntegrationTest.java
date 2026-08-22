package com.fotolou.app;

import com.fotolou.app.config.AsyncSyncConfiguration;
import com.fotolou.app.config.DatabaseTestcontainer;
import com.fotolou.app.config.JacksonConfiguration;
import com.fotolou.app.config.RedisTestContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        FotolouBackendApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.fotolou.app.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers({ DatabaseTestcontainer.class, RedisTestContainer.class })
public @interface IntegrationTest {}
