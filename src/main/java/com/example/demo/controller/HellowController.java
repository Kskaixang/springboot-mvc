package com.example.demo.controller;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HellowController {
	//代表網址url-service doGet
	@GetMapping("/hello")
	//我回應給前端的網頁就是 ResponseBody
	@ResponseBody
	public String helloME() {
		return "Hello " + new Date();
	}
	
	@GetMapping("/hi")
	@ResponseBody
	public String hi() {
		return "Hi" + new Date();
	}
	
	@GetMapping("/welcome")
	//import org.springframework.ui.Model;  
	//model裡面放的就是要傳給jsp的資料
	public String welcome(Model model) {
		model.addAttribute("name", "Jsp測試名");
		model.addAttribute("now", new Date());
		return "welcome"; //取 welcome.jsp 檔名的部分 所以要一樣
	}

}
