package com.atguigu.spring.beans;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author wule
 * @create 2024-12-07 23:41
 */
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class Cat implements InitializingBean {
	private String name;

	public String getName() {
		return name;
	}

	@Value("${JAVA_HOME}")
	public void setName(String name) {
		System.out.println("cat setName...");
		this.name = name;
	}

	public Cat() {
		System.out.println("cat constructor...");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("cat afterPropertiesSet...");
	}
}
