package com.nexon.nutriai.service;

import com.nexon.nutriai.api.ChatAPI;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.repository.DialogueLogRepository;
import com.nexon.nutriai.repository.entity.DialogueLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final ChatAPI chatAPI;
    private final DialogueLogRepository dialogueLogRepository;

    public String getChatModel() {
        return chatAPI.getModel();
    }

    public Flux<String> recommendRecipe(String phone, String question, String chatId) {

        // 获取流式响应
        Flux<String> responseStream = chatAPI.recommendRecipe(phone, question, chatId);

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

    public Flux<ChatHistory> getChatHistoryStream(String conversationId) {
        return Flux.fromIterable(chatAPI.messages(conversationId))
                .mapNotNull(message -> {
                    if (message instanceof UserMessage userMessage) {
                        String text = userMessage.getText();
                        int start = text.indexOf("|----------|");
                        int end = text.lastIndexOf("|----------|");
                        if (start != -1 && end != -1) {
                            String substring = text.substring(start + 12, end)
                                    .replaceAll("\r", "")
                                    .replaceAll("\n", "");
                            return new ChatHistory("user", substring);
                        }
                    } else if (message instanceof AssistantMessage assistantMessage) {
                        return new ChatHistory("assistant", assistantMessage.getText());
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }

    public boolean interruptRequest(String chatId) {
        return chatAPI.interruptRequest(chatId);
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
