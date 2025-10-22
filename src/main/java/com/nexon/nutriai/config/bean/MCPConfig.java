package com.nexon.nutriai.config.bean;

import com.nexon.nutriai.mcp.UserMCPService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfig {

    @Bean
    public ToolCallbackProvider weatherTools(UserMCPService userMCPService) {
        return MethodToolCallbackProvider.builder().toolObjects(userMCPService).build();
    }
}
