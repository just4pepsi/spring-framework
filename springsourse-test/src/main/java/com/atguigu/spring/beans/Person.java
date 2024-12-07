package com.atguigu.spring.beans;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * @author wule
 * @create 2024-10-15 22:00
 */
@Component
public class Person {
	private String name;
	// @Autowired  //依赖的组件是多实例就不能Autowired
	private Cat cat;

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
}
