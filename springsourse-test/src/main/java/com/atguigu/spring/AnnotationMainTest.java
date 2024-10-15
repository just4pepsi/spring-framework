package com.atguigu.spring;

import com.atguigu.spring.beans.Person;
import com.atguigu.spring.config.MailConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 测试注解版
 * @author wule
 * @create 2024-10-15 22:23
 */
public class AnnotationMainTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MailConfig.class);
		Person bean = context.getBean(Person.class);
		System.out.println(bean);
	}
}
