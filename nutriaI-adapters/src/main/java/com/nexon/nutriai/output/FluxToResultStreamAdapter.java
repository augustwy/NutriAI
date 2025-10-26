package com.nexon.nutriai.output;

import com.nexon.nutriai.ports.output.ResultStream;
import reactor.core.publisher.Flux;

public class FluxToResultStreamAdapter<T> implements ResultStream<T> {

    private final Flux<T> flux;

    public FluxToResultStreamAdapter(Flux<T> flux) {
        this.flux = flux;
    }

    @Override
    public void subscribe(Subscriber<T> subscriber) {
        // 当 Core 层的订阅者订阅时，我们将其转换为 Reactor 的订阅者
        flux.subscribe(
                subscriber::onNext,
                subscriber::onError,
                subscriber::onComplete
        );
    }
}
