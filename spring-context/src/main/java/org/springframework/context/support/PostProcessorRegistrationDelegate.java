/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}

	// 执行工厂的后置处理器
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry registry) {
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
			// 先拿到底层默认有的 BeanFactoryPostProcessor
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor registryProcessor) {
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// 首先：从工厂中获取所以实现 PriorityOrdered 的 BeanDefinitionRegistryPostProcessors。 First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));	//从工厂中获取这个组件【getBean整个组件创建】并放入这个集合
					processedBeans.add(ppName);
				}
			}// PriorityOrdered 优先级排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());	//执行BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry【解析配置类此方法执行】
			currentRegistryProcessors.clear();

			// 从工厂中获取所以实现 Ordered 的 BeanDefinitionRegistryPostProcessors。 Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			// !processedBeans.contains(ppName) 避免重复执行
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}	//按照名称大小写排序
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}
		//以前在执行 BeanDefinitionRegistryPostProcessor ,以后来执行 BeanFactoryPostProcessor
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);		//执行所有的 BeanFactoryPostProcessor

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, for example, replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22
		// 获取到容器中所有的 BeanPostProcessor ；Bean的后置处理器
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs a warn message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(
				new BeanPostProcessorChecker(beanFactory, postProcessorNames, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {	//获取到实现了 PriorityOrdered 的 BeanPostProcessor
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 首先，注册所有的实现了PriorityOrdered的BeanPostProcessors。First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// 接下来，注册所有的实现了Ordered的BeanPostProcessors。Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);	//获取后置处理器对象
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// 最后，注册普通的BeanPostProcessors。Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);	// 从容器中获取这个组件
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 注册 BeanPostProcessors
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// 最后，重新注册所有 internal 的 BeanPostProcessors。 Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// 重新注册以下这个后置处理器 Re-register post-processor for detecting inner beans as ApplicationListeners,
		// 把他放到后置处理器的最后一位；虽然前面注册过这里调整顺序 moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	/**
	 * Load and sort the post-processors of the specified type.
	 * @param beanFactory the bean factory to use
	 * @param beanPostProcessorType the post-processor type
	 * @param <T> the post-processor type
	 * @return a list of sorted post-processors for the specified type
	 */
	static <T extends BeanPostProcessor> List<T> loadBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, Class<T> beanPostProcessorType) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(beanPostProcessorType, true, false);
		List<T> postProcessors = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			postProcessors.add(beanFactory.getBean(ppName, beanPostProcessorType));
		}
		sortPostProcessors(postProcessors, beanFactory);
		return postProcessors;

	}

	/**
	 * Selectively invoke {@link MergedBeanDefinitionPostProcessor} instances
	 * registered in the specified bean factory, resolving bean definitions and
	 * any attributes if necessary as well as any inner bean definitions that
	 * they may contain.
	 * @param beanFactory the bean factory to use
	 */
	static void invokeMergedBeanDefinitionPostProcessors(DefaultListableBeanFactory beanFactory) {
		new MergedBeanDefinitionPostProcessorInvoker(beanFactory).invokeMergedBeanDefinitionPostProcessors();
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory dlbf) {
			comparatorToUse = dlbf.getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 *///执行 BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry, ApplicationStartup applicationStartup) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanDefRegistry = applicationStartup.start("spring.context.beandef-registry.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanDefinitionRegistry(registry);	// 核心，配置类的后置处理器会在次解析配置类
			postProcessBeanDefRegistry.end();
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<? extends BeanPostProcessor> postProcessors) {

		if (beanFactory instanceof AbstractBeanFactory abstractBeanFactory) {
			// Bulk addition is more efficient against our CopyOnWriteArrayList there
			abstractBeanFactory.addBeanPostProcessors(postProcessors);
		}
		else {
			for (BeanPostProcessor postProcessor : postProcessors) {
				beanFactory.addBeanPostProcessor(postProcessor);
			}
		}
	}


	/**
	 * BeanPostProcessor that logs a warn message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final String[] postProcessorNames;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory,
				String[] postProcessorNames, int beanPostProcessorTargetCount) {

			this.beanFactory = beanFactory;
			this.postProcessorNames = postProcessorNames;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isWarnEnabled()) {
					Set<String> bppsInCreation = new LinkedHashSet<>(2);
					for (String bppName : this.postProcessorNames) {
						if (this.beanFactory.isCurrentlyInCreation(bppName)) {
							bppsInCreation.add(bppName);
						}
					}
					if (bppsInCreation.size() == 1) {
						String bppName = bppsInCreation.iterator().next();
						if (this.beanFactory.containsBeanDefinition(bppName) &&
								beanName.equals(this.beanFactory.getBeanDefinition(bppName).getFactoryBeanName())) {
							logger.warn("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
									"] is not eligible for getting processed by all BeanPostProcessors " +
									"(for example: not eligible for auto-proxying). The currently created " +
									"BeanPostProcessor " + bppsInCreation + " is declared through a non-static " +
									"factory method on that class; consider declaring it as static instead.");
							return bean;
						}
					}
					logger.warn("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying). Is this bean getting eagerly " +
							"injected/applied to a currently created BeanPostProcessor " + bppsInCreation + "? " +
							"Check the corresponding BeanPostProcessor declaration and its dependencies/advisors. " +
							"If this bean does not have to be post-processed, declare it with ROLE_INFRASTRUCTURE.");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}


	private static final class MergedBeanDefinitionPostProcessorInvoker {

		private final DefaultListableBeanFactory beanFactory;

		private MergedBeanDefinitionPostProcessorInvoker(DefaultListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		private void invokeMergedBeanDefinitionPostProcessors() {
			List<MergedBeanDefinitionPostProcessor> postProcessors = PostProcessorRegistrationDelegate.loadBeanPostProcessors(
					this.beanFactory, MergedBeanDefinitionPostProcessor.class);
			for (String beanName : this.beanFactory.getBeanDefinitionNames()) {
				RootBeanDefinition bd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
				Class<?> beanType = resolveBeanType(bd);
				postProcessRootBeanDefinition(postProcessors, beanName, beanType, bd);
				bd.markAsPostProcessed();
			}
			registerBeanPostProcessors(this.beanFactory, postProcessors);
		}

		private void postProcessRootBeanDefinition(List<MergedBeanDefinitionPostProcessor> postProcessors,
				String beanName, Class<?> beanType, RootBeanDefinition bd) {

			BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, bd);
			postProcessors.forEach(postProcessor -> postProcessor.postProcessMergedBeanDefinition(bd, beanType, beanName));
			for (PropertyValue propertyValue : bd.getPropertyValues().getPropertyValueList()) {
				postProcessValue(postProcessors, valueResolver, propertyValue.getValue());
			}
			for (ValueHolder valueHolder : bd.getConstructorArgumentValues().getIndexedArgumentValues().values()) {
				postProcessValue(postProcessors, valueResolver, valueHolder.getValue());
			}
			for (ValueHolder valueHolder : bd.getConstructorArgumentValues().getGenericArgumentValues()) {
				postProcessValue(postProcessors, valueResolver, valueHolder.getValue());
			}
		}

		private void postProcessValue(List<MergedBeanDefinitionPostProcessor> postProcessors,
				BeanDefinitionValueResolver valueResolver, @Nullable Object value) {
			if (value instanceof BeanDefinitionHolder bdh
					&& bdh.getBeanDefinition() instanceof AbstractBeanDefinition innerBd) {

				Class<?> innerBeanType = resolveBeanType(innerBd);
				resolveInnerBeanDefinition(valueResolver, innerBd, (innerBeanName, innerBeanDefinition)
						-> postProcessRootBeanDefinition(postProcessors, innerBeanName, innerBeanType, innerBeanDefinition));
			}
			else if (value instanceof AbstractBeanDefinition innerBd) {
				Class<?> innerBeanType = resolveBeanType(innerBd);
				resolveInnerBeanDefinition(valueResolver, innerBd, (innerBeanName, innerBeanDefinition)
						-> postProcessRootBeanDefinition(postProcessors, innerBeanName, innerBeanType, innerBeanDefinition));
			}
			else if (value instanceof TypedStringValue typedStringValue) {
				resolveTypeStringValue(typedStringValue);
			}
		}

		private void resolveInnerBeanDefinition(BeanDefinitionValueResolver valueResolver, BeanDefinition innerBeanDefinition,
				BiConsumer<String, RootBeanDefinition> resolver) {

			valueResolver.resolveInnerBean(null, innerBeanDefinition, (name, rbd) -> {
				resolver.accept(name, rbd);
				return Void.class;
			});
		}

		private void resolveTypeStringValue(TypedStringValue typedStringValue) {
			try {
				typedStringValue.resolveTargetType(this.beanFactory.getBeanClassLoader());
			}
			catch (ClassNotFoundException ex) {
				// ignore
			}
		}

		private Class<?> resolveBeanType(AbstractBeanDefinition bd) {
			if (!bd.hasBeanClass()) {
				try {
					bd.resolveBeanClass(this.beanFactory.getBeanClassLoader());
				}
				catch (ClassNotFoundException ex) {
					// ignore
				}
			}
			return bd.getResolvableType().toClass();
		}
	}

}
