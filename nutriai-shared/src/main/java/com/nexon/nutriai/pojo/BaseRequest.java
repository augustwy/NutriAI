package com.nexon.nutriai.pojo;

public record BaseRequest<T>(String phone, String chatId, T data) {

}
