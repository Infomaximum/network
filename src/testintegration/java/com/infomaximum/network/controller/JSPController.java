package com.infomaximum.network.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kris on 13.06.17.
 */
@Controller
@RequestMapping("/jsp")
public class JSPController {

    @RequestMapping(value={"", "/"}, method = RequestMethod.GET)
    public String index(final ModelMap model, @RequestParam("message") final String message) {
        List<String> messages = new ArrayList<String>();
        messages.add(message);

        model.addAttribute("messages", messages);
        return "index";
    }
}
