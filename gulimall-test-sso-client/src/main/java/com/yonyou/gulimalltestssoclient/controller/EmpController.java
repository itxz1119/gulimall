package com.yonyou.gulimalltestssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class EmpController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    @GetMapping("/emp")
    public String getEmp(Model model, HttpSession session){
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null){
            return "redirect:" + ssoServerUrl + "?service=http://client1.com:8081/emp";
        }
        ArrayList<Object> list = new ArrayList<>();
        list.add("isr1");
        list.add("isr2");
        model.addAttribute("emps", list);
        return "list";
    }
}
