package com.tencent.cloudrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class IndexController {
    /**
     * 主页页面
     * @return API response html
     */
    @GetMapping
    public ModelAndView index() {
        return new ModelAndView("index");
    }
}
