package com.nexon.nutriai.util.cache;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存缓存实现类
 * 
 * 基于ConcurrentHashMap实现的内存缓存，支持过期时间设置和自动清理。
 * 使用ScheduledExecutorService定期清理过期缓存项。
 * 
 * @param <K> 键类型
 * @param <V> 值类型
 */
@Component
@Primary
public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    /**
     * 构造函数
     * 
     * 初始化缓存并启动定期清理过期缓存的任务。
     */
    public InMemoryCache() {
        // 启动定期清理过期缓存的任务
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存值
     * 
     * 根据键获取缓存值，如果键不存在或已过期则返回null。
     * 
     * @param key 缓存键
     * @return 缓存值，不存在或过期返回null
     */
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

        return entry.value();
    }

    /**
     * 存储缓存值（无过期时间）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
    }

    /**
     * 存储缓存值（指定过期时间）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间（毫秒）
     */
    @Override
    public void put(K key, V value, long ttl) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttl));
    }

    /**
     * 移除缓存项
     * 
     * @param key 缓存键
     * @return 是否成功移除
     */
    @Override
    public boolean remove(K key) {
        return cache.remove(key) != null;
    }

    /**
     * 检查缓存键是否存在
     * 
     * @param key 缓存键
     * @return 键是否存在
     */
    @Override
    public boolean containsKey(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return entry != null;
    }

    /**
     * 清空所有缓存
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * 获取缓存大小
     * 
     * @return 缓存项数量
     */
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
    private record CacheEntry<V>(V value, Long expireTime) {
        public CacheEntry(V value) {
            this(value, null);
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