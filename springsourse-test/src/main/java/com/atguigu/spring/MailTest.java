package com.atguigu.spring;

import com.atguigu.spring.beans.Cat;
import com.atguigu.spring.beans.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wule
 * @create 2024-10-15 21:38
 */
public class MailTest {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans2.xml");
		Cat bean = context.getBean(Cat.class);
		System.out.println(bean);
	}
	public static void test01(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		Person bean = context.getBean(Person.class);
		System.out.println(bean);
	}
}
