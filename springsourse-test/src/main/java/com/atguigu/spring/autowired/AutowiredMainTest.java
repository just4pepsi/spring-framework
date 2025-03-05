package com.atguigu.spring.autowired;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AutowiredMainTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfiguration.class);
        MyController controller = context.getBean(MyController.class);
        controller.showService();
    }
}