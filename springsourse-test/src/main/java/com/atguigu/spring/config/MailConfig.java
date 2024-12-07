package com.atguigu.spring.config;

import com.atguigu.spring.beans.Cat;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 配置类
 *
 * @author wule
 * @create 2024-10-15 22:29
 */
@ComponentScan("com.atguigu.spring")
//@Import({Person.class, MailConfig.MyImportRegister.class})
@Configuration
public class MailConfig {

//	@Bean
//	public Person person(){
//		Person person = new Person();
//		person.setName("张三");
//		return person;
//	}

	/**
	 * BeanDefinitionRegistry：Bean定义信息注册中心：图纸中心;
	 * 它里面都是BeanDefinition
	 *
	 * <bean class="com.atguigu.spring.bean.Person" id="person">
	 * <property name="name" value="张三"/>
	 * </bean>
	 * 对应
	 * RootBeanDefinition
	 */
	static class MyImportRegister implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
											BeanDefinitionRegistry registry) {
			// BeanDefinition
			RootBeanDefinition catDefinition = new RootBeanDefinition();
			catDefinition.setBeanClass(Cat.class);
			//	catDefinition.setInitMethodName("aaa");
			//可以声明定义信息，包括我需要自动装配什么？
			// catDefinition.setInstanceSupplier(()-> new Cat());
			//Spring 这个实例的类型，名字
			registry.registerBeanDefinition("tomCat", catDefinition);
		}
	}
}
