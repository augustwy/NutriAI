package com.nexon.nutriai.config.bean;

import com.alibaba.cloud.ai.toolcalling.time.GetTimeByZoneIdService;
import com.nexon.nutriai.tools.TimeTools;
import com.nexon.nutriai.tools.UserTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallbackProvider userToolsCallback(UserTools userTools) {
        return MethodToolCallbackProvider.builder().toolObjects(userTools).build();
    }

    @Bean
    public ToolCallbackProvider timeToolsCallback(TimeTools timeTools) {
        return MethodToolCallbackProvider.builder().toolObjects(timeTools).build();
    }

    @Bean
    public GetTimeByZoneIdService getTimeByZoneIdService() {
        return new GetTimeByZoneIdService();
    }

    @Bean
    public TimeTools timeTools(GetTimeByZoneIdService getTimeByZoneIdService) {
        return new TimeTools(getTimeByZoneIdService);
    }
}
