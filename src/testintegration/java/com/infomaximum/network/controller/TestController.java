package com.infomaximum.network.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

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

	@RequestMapping(value = "/file")
	public HttpEntity<byte[]> getFile(@RequestParam("size") int size) {
		Random random = new Random();

		byte[] content = new byte[size];
		random.nextBytes(content);

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_CBOR);
		header.set(HttpHeaders.CONTENT_DISPOSITION,	"attachment; filename=test.tmp");
		header.setContentLength(content.length);

		return new HttpEntity<byte[]>(content, header);
	}
}