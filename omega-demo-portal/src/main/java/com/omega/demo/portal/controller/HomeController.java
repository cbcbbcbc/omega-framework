package com.omega.demo.portal.controller;

import com.omega.demo.api.EchoService;
import com.omega.demo.api.UserService;
import com.omega.demo.api.bean.Greeting;
import com.omega.demo.api.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@Controller
public class HomeController {

    @Autowired
    EchoService echoService;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("index");

        User user = userService.getById("User02");
        if (user == null) {
            mv.addObject("result", "Not Found");
            return mv;
        }

        Greeting g = new Greeting();
        g.setNick("Mr. " + user.getName());
        g.setMessage("nice to meet you");

        mv.addObject("result", echoService.echo("TEST", g));
        return mv;
    }
}
