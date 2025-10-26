package com.nexon.nutriai.util;

import org.springframework.web.server.ServerWebExchange;

public class WebFluxUtil {

    public static final String CURRENT_USER_ATTR = "currentUser";
    public static final String CHAT_ID_ATTR = "chatId";

    public static String getPhone(ServerWebExchange exchange) {

        return exchange.getAttribute(CURRENT_USER_ATTR);
    }

    public static String getChatId(ServerWebExchange exchange) {
        return exchange.getAttribute(CHAT_ID_ATTR);
    }
}
