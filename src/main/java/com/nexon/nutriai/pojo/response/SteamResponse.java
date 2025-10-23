package com.nexon.nutriai.pojo.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SteamResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private String role;
    private List<Choice> choices;

    @Getter
    @Setter
    public static class Choice {
        private Integer index;
        private Delta delta;
        private String finishReason;
    }

    @Getter
    @Setter
    public static class Delta {
        private String content;
        private String role;
    }
}
