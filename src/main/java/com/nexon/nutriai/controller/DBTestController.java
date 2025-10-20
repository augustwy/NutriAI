package com.nexon.nutriai.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.pojo.BaseResponse;
import com.nexon.nutriai.repository.DemoRepository;
import com.nexon.nutriai.repository.entity.Demo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test")
public class DBTestController {


    private DemoRepository demoRepository;
    public DBTestController(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    @PostMapping("/insert")
    public ResponseEntity<BaseResponse> insert(@RequestParam("username") String username, @RequestParam("password") String password) {
        demoRepository.save(new Demo(username, password));
        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping("/update")
    public ResponseEntity<BaseResponse> update(@RequestParam("id") Long id, @RequestParam("username") String username, @RequestParam("password") String password) {
        demoRepository.save(new Demo(id, username, password));
        return ResponseEntity.ok(BaseResponse.success());
    }

    @GetMapping("/query")
    public ResponseEntity<BaseResponse> insert(@RequestParam("username") String username) {
        List<Demo> all = demoRepository.findAll(Example.of(new Demo(username, null)));
        log.info("query result: {}", JSONObject.toJSONString(all));
        return ResponseEntity.ok(BaseResponse.success());
    }
}
