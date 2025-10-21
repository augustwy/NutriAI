package com.nexon.nutriai.api;

import reactor.core.publisher.Flux;

public interface ChatAPI {

    Flux<String> chatRecipe(String question, String chatId);
}
