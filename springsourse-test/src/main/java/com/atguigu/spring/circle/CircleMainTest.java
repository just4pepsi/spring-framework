package com.atguigu.spring.circle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 * @author wule
 * @create 2025-06-17 16:13
 */
public class CircleMainTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CircleConfig.class);
		A a = context.getBean(A.class);
	}
}
