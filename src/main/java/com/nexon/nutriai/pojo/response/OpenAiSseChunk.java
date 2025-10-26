package com.nexon.nutriai.domain.service.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 主数据块
@Getter
@Setter
public class OpenAiSseChunk {

    String id;
    String object;
    long created;
    String model;
    List<Choice> choices;

    public <E> OpenAiSseChunk(String id, String object, long created, String model, List<Choice> choices) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
        this.choices = choices;
    }

    // choices 数组中的元素
    public record Choice(int index, Delta delta, String finish_reason) {}

    // delta 对象
    public record Delta(String content) {}
}





