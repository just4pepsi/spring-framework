package com.atguigu.spring.config;

import com.atguigu.spring.beans.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 * @author wule
 * @create 2024-10-15 22:29
 */
@Configuration
public class MailConfig {

	@Bean
	public Person person(){
		Person person = new Person();
		person.setName("张三");
		return person;
	}
}
