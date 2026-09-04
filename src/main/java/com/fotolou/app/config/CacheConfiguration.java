package com.fotolou.app.config;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(JHipsterProperties jHipsterProperties) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();

        URI redisUri = URI.create(jHipsterProperties.getCache().getRedis().getServer()[0]);

        Config config = new Config();
        // Fix Hibernate lazy initialization https://github.com/jhipster/generator-jhipster/issues/22889
        config.setCodec(new org.redisson.codec.SerializationCodec());
        if (jHipsterProperties.getCache().getRedis().isCluster()) {
            ClusterServersConfig clusterServersConfig = config
                .useClusterServers()
                .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());

            if (redisUri.getUserInfo() != null) {
                clusterServersConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        } else {
            SingleServerConfig singleServerConfig = config
                .useSingleServer()
                .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);

            if (redisUri.getUserInfo() != null) {
                singleServerConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        }
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, jHipsterProperties.getCache().getRedis().getExpiration()))
        );
        return RedissonConfiguration.fromInstance(Redisson.create(config), jcacheConfig);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            createCache(cm, com.fotolou.app.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.fotolou.app.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Salon.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Salon.class.getName() + ".actionses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Salon.class.getName() + ".coiffeurses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Salon.class.getName() + ".ticketses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.SalonAction.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.CoiffeurProfile.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Ticket.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Relative.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.FavoriteSalon.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.ProductCategory.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.ProductCategory.class.getName() + ".productses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Product.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.Product.class.getName() + ".imageses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.ProductImage.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.BoutiqueOrder.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.BoutiqueOrder.class.getName() + ".itemses", jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.OrderItem.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.AppNotification.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.PlatformSettings.class.getName(), jcacheConfiguration);
            createCache(cm, com.fotolou.app.domain.OtpVerification.class.getName(), jcacheConfiguration);
            // jhipster-needle-redis-add-entry
        };
    }

    private void createCache(
        javax.cache.CacheManager cm,
        String cacheName,
        javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration
    ) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
