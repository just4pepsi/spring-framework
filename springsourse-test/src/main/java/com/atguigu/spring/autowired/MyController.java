package com.atguigu.spring.autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author wule
 * @create 2025-01-13 16:08
 */
@Controller
public class MyController {


	@Autowired
	private MyService myService;


	public void showService(){
		System.out.println("myService = " + myService);
	}
}
