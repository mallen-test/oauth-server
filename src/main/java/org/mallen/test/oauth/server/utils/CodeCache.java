package org.mallen.test.oauth.server.utils;

import org.ehcache.CacheManager;
import org.ehcache.config.ResourceUnit;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.time.Duration;

/**
 * 使用ehcache作为临时保存code的媒介，过期时间为120s
 * @author mallen
 * @date 3/23/19
 */
public class CodeCache {
    private static final String CACHE_NAME = "AUTH_CODE";
    private static final long EXPIRE_SECONDS = 120;
    private static CodeCache instance = null;
    private CacheManager cacheManager;

    static {
        instance = new CodeCache();
        instance.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(
                        CACHE_NAME,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100, MemoryUnit.MB)
                        ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(EXPIRE_SECONDS)))
                ).build(true);
    }

    public static void set(String clientId, String code) {
        instance.getCacheManager().getCache(CACHE_NAME, String.class, String.class).put(code, clientId);
    }

    public static String get(String code) {
        return instance.getCacheManager().getCache(CACHE_NAME, String.class, String.class).get(code);
    }

    public static void remove(String code) {
        instance.getCacheManager().getCache(CACHE_NAME, String.class, String.class).remove(code);
    }


    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
