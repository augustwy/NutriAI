package com.nexon.nutriai.api;

import reactor.core.publisher.Flux;

public interface ChatAPI {

    Flux<String> recommendRecipe(String question, String chatId);
}
