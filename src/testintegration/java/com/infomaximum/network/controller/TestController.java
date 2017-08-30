package com.infomaximum.network.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

	@RequestMapping("/ping")
	public String ping() {
		return "pong";
	}

	@RequestMapping("/ping1")
	public String ping1() {
		return "pong1";
	}

}