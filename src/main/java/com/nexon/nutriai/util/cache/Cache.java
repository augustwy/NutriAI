package com.nexon.nutriai.util.cache;

// Cache.java
public interface Cache<K, V> {
    
    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值，不存在返回null
     */
    V get(K key);
    
    /**
     * 设置缓存值
     * @param key 缓存键
     * @param value 缓存值
     */
    void put(K key, V value);
    
    /**
     * 设置带过期时间的缓存值
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间(毫秒)
     */
    void put(K key, V value, long ttl);
    
    /**
     * 删除缓存项
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean remove(K key);
    
    /**
     * 检查缓存项是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean containsKey(K key);
    
    /**
     * 清空所有缓存
     */
    void clear();
    
    /**
     * 获取缓存大小
     * @return 缓存项数量
     */
    int size();
}
