package com.nexon.nutriai.mcp;

import com.nexon.nutriai.pojo.dto.UserInformationDTO;
import com.nexon.nutriai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserMCPService {

    private final UserService userService;

    @Tool(description = "通过用户手机号获取用户信息")
    public String getUserInformation(@ToolParam(description = "用户的手机号码，用于查询用户基本信息") String phone) {
        UserInformationDTO userInformation = userService.getUserInformation(phone);
        return null != userInformation ? userInformation.buildUserInformation() : "用户信息获取失败";
    }
}
