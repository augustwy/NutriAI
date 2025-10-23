package com.nexon.nutriai.config.bean;

import com.alibaba.cloud.ai.toolcalling.time.GetCurrentTimeByTimeZoneIdService;
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
    public GetCurrentTimeByTimeZoneIdService getGetCurrentTimeByTimeZoneIdService() {
        return new GetCurrentTimeByTimeZoneIdService();
    }

    @Bean
    public TimeTools timeTools(GetCurrentTimeByTimeZoneIdService service) {
        return new TimeTools(service);
    }
}
