package com.nexon.nutriai.service;

import com.nexon.nutriai.api.ChatAPI;
import com.nexon.nutriai.repository.DialogueLogRepository;
import com.nexon.nutriai.repository.entity.DialogueLog;
import com.nexon.nutriai.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class RecipeService {

    private final ChatAPI chatAPI;
    private final DialogueLogRepository dialogueLogRepository;
    public RecipeService(ChatAPI chatAPI, DialogueLogRepository dialogueLogRepository) {
        this.chatAPI = chatAPI;
        this.dialogueLogRepository = dialogueLogRepository;
    }

    public Flux<String> recommendRecipe(String question, String chatId) {
        String phone = ThreadLocalUtil.getPhone();

        // 获取流式响应
        Flux<String> responseStream = chatAPI.recommendRecipe(question, chatId);

        // 收集完整响应并保存
        StringBuilder completeResponse = new StringBuilder();

        return responseStream
                .doOnNext(completeResponse::append)
                .doOnComplete(() -> {
                    log.info("完整响应: {}", completeResponse);
                    // 保存完整对话记录
                    saveDialogueLog(chatId, phone, question, completeResponse.toString());
                })
                .doOnError(error -> {
                    // 保存错误记录
                    saveDialogueLog(chatId, phone, question, "Error: " + error.getMessage());
                });
    }

    public List<Message> messages(String conversationId) {
        return chatAPI.messages(conversationId);
    }

    private void saveDialogueLog(String chatId, String phone, String question, String response) {
        DialogueLog dialogueLog = new DialogueLog();
        dialogueLog.setRequestId(chatId);
        dialogueLog.setPhone(phone);
        dialogueLog.setQuestion(question);
        dialogueLog.setAnswer(response);
        dialogueLogRepository.save(dialogueLog);
    }
}
