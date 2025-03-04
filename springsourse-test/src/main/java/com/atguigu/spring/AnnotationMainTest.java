package com.atguigu.spring;

import com.atguigu.spring.beans.Hello;
import com.atguigu.spring.config.MailConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 测试注解版
 *
 * @author wule
 * @create 2024-10-15 22:23
 */
public class AnnotationMainTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MailConfig.class);
		Hello bean = context.getBean(Hello.class);
		System.out.println(bean);

		//循环引用,原理测试
		//AOP,原理测试
//		HelloService helloService = context.getBean(HelloService.class);
//		//代理对象来调用方法
//		helloService.sayHello("zhangsan");
		// 测试单例
//		Person bean = context.getBean(Person.class);
//		System.out.println(bean);

//		Person p1 = context.getBean(Person.class);
//		Cat cat1 = p1.getCat();
//		Person p2 = context.getBean(Person.class);
//		Cat cat2 = p2.getCat();
//		System.out.println(cat1 == cat2); //true

//		Person p = context.getBean(Person.class);
//		ApplicationContext applicationContext = p.getApplicationContext();
//		System.out.println(applicationContext == context);
	}
}
