package com.atguigu.spring;

import com.atguigu.spring.circle.A;
import com.atguigu.spring.config.MailConfig;
import com.atguigu.spring.listener.AppEventPublisher;
import com.atguigu.spring.listener.ChangeEvent;
import com.atguigu.spring.listener.MessageEvent;
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
		//测试事件
		AppEventPublisher eventPublisher = context.getBean(AppEventPublisher.class);
		eventPublisher.publish(new A());
		eventPublisher.publish(new MessageEvent("hello，你好"));
		eventPublisher.publish(new ChangeEvent(eventPublisher,"sending..."));
		//测试aop
//		HelloService bean = context.getBean(HelloService.class);
//		bean.sayHello("zhangsan");
//		Cat bean = context.getBean(Cat.class);
//		System.out.println(bean);

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
