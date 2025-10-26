package com.nexon.nutriai.ports.output;

/**
 * Core 层自己的流抽象，代表一个可以产生一系列结果的源。
 * 它不依赖任何外部框架。
 */
public interface ResultStream<T> {

    /**
     * 订阅这个流，并设置一个监听器来接收数据。
     * @param subscriber 监听器
     */
    void subscribe(Subscriber<T> subscriber);

    /**
     * 流的订阅者/监听器接口。
     * @param <T> 数据项类型
     */
    interface Subscriber<T> {
        void onNext(T item);
        void onError(Throwable error);
        void onComplete();
    }
}