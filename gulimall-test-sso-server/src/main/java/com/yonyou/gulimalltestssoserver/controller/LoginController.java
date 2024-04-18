package com.yonyou.gulimalltestssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {



    @GetMapping("/login.html")
    public String loginPage(@RequestParam(value = "service", required = false) String url, Model model) {
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, String url){
        //登录成功
        if (StringUtils.hasLength(username)){
            return "redirect:"+url;
        }
        return "login";
    }
}
