package com.nexon.nutriai.ports.input.service;

import com.nexon.nutriai.domain.entity.DialogueLog;
import com.nexon.nutriai.domain.service.ChatHistory;
import com.nexon.nutriai.ports.output.ResultStream;
import com.nexon.nutriai.ports.output.ai.ChatAPI;
import com.nexon.nutriai.ports.output.repository.DialogueLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RecipeService {

    private final ChatAPI chatAPI;
    private final DialogueLogPort dialogueLogPort;

    public String getChatModel() {
        return chatAPI.getModel();
    }

    public ResultStream<String> recommendRecipe(String phone, String question, String chatId) {

        // 获取流式响应
        ResultStream<String> responseStream = chatAPI.recommendRecipe(phone, question, chatId);

        // 收集完整响应并保存
        StringBuilder completeResponse = new StringBuilder();

        responseStream.subscribe(new ResultStream.Subscriber<>() {
            @Override
            public void onNext(String item) {
                completeResponse.append(item);
            }

            @Override
            public void onError(Throwable error) {
                // 保存错误记录
                saveDialogueLog(chatId, phone, question, "Error: " + error.getMessage());
            }

            @Override
            public void onComplete() {
//                log.info("完整响应: {}", completeResponse);
                // 保存完整对话记录
                saveDialogueLog(chatId, phone, question, completeResponse.toString());
            }
        });

        return responseStream;
    }

    public List<ChatHistory> getChatHistoryStream(String conversationId) {
//        return Flux.fromIterable(chatAPI.messages(conversationId))
//                .mapNotNull(message -> {
//                    if (message instanceof UserMessage userMessage) {
//                        String text = userMessage.getText();
//                        int start = text.indexOf("|----------|");
//                        int end = text.lastIndexOf("|----------|");
//                        if (start != -1 && end != -1) {
//                            String substring = text.substring(start + 12, end)
//                                    .replaceAll("\r", "")
//                                    .replaceAll("\n", "");
//                            return new ChatHistory("user", substring);
//                        }
//                    } else if (message instanceof AssistantMessage assistantMessage) {
//                        return new ChatHistory("assistant", assistantMessage.getText());
//                    }
//                    return null;
//                })
//                .filter(Objects::nonNull);
        return chatAPI.messages(conversationId);
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
        dialogueLogPort.save(dialogueLog);
    }
}
