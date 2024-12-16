package com.atguigu.spring.beans;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Component;

/**
 * Aware接口：帮我们装配Spring底层的一些组件
 * 1、Bean的功能增强全部都是有 BeanPostProcessor+InitializingBean（合起来完成的）
 * 2、骚操作就是 BeanPostProcessor+InitializingBean
 * @author wule
 * @create 2024-10-15 22:00
 */
@Component
public class Person implements ApplicationContextAware, MessageSourceAware {
	private String name;
	// @Autowired  //依赖的组件是多实例就不能Autowired
	private Cat cat;

	//	@Autowired
	private ApplicationContext applicationContext;
	private MessageSource messageSource;

	public Person() {
		System.out.println("创建Person");
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// 利用回调机制，把ioc容器传入
		this.applicationContext = applicationContext;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Lookup  //去容器中找。@Bean的这种方式注册的Person @Lookup不生效
	public Cat getCat() {
		return cat;
	}

	public void setCat(Cat cat) {
		this.cat = cat;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				'}';
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
