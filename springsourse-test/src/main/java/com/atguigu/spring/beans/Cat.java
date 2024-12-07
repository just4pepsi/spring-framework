package com.atguigu.spring.beans;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author wule
 * @create 2024-12-07 23:41
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class Cat {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
