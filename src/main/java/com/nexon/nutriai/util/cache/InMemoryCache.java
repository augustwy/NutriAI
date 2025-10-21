package com.nexon.nutriai.util.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    public InMemoryCache() {
        // 启动定期清理过期缓存的任务
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        return entry.getValue();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
    }

    @Override
    public void put(K key, V value, long ttl) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttl));
    }

    @Override
    public boolean remove(K key) {
        return cache.remove(key) != null;
    }

    @Override
    public boolean containsKey(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return entry != null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        cleanupExpiredEntries(); // 先清理过期项再返回大小
        return cache.size();
    }

    /**
     * 清理过期的缓存项
     */
    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry ->
                entry.getValue().isExpired()
        );
    }

    /**
     * 缓存条目内部类
     */
    private static class CacheEntry<V> {
        private final V value;
        private final Long expireTime;

        public CacheEntry(V value) {
            this.value = value;
            this.expireTime = null;
        }

        public CacheEntry(V value, Long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return expireTime != null && System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 关闭清理任务
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
