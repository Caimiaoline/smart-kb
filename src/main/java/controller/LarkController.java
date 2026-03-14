package com.example.smartkb.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lark")
public class LarkController {

    // 飞书发来的消息会进到这个方法里
    @PostMapping("/receive")
    public String receive(@RequestBody String body) {
        // 在 IDEA 底部控制台打印收到的内容
        System.out.println("【飞书消息】: " + body);

        // 必须给飞书返回这个 JSON，不然飞书会报错
        return "{\"msg\": \"success\"}";
    }
}