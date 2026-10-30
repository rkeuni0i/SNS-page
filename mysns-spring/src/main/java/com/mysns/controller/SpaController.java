package com.mysns.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * React Router가 처리하는 화면 경로로 직접 접속하거나 새로고침해도
 * index.html이 내려가도록 forward 한다(빌드 결과물을 jar에 넣어 배포할 때 필요).
 */
@Controller
public class SpaController {

    @GetMapping({"/login", "/signup", "/users", "/users/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
