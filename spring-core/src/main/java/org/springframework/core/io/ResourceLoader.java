/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * 用于加载资源（例如类路径或文件系统资源）的策略接口。
 * 一个 ApplicationContext 需要提供此功能以及扩展的 ResourcePatternResolver 支持。
 * Strategy interface (策略接口) for loading resources (for example, class path or file system
 * resources). An {@link org.springframework.context.ApplicationContext}
 * is required to provide this functionality plus extended
 * {@link org.springframework.core.io.support.ResourcePatternResolver} support.
 *	DefaultResourceLoader 是一个独立的实现，可在 ApplicationContext 外部使用，并被 ResourceEditor 使用。
 * <p>{@link DefaultResourceLoader} is a standalone implementation that is
 * usable outside an ApplicationContext and is also used by {@link ResourceEditor}.
 *	当在 ApplicationContext 中运行时，类型为 Resource 和 Resource[] 的 Bean 属性可以从字符串中填充，使用特定上下文的资源加载策略。
 * <p>Bean properties of type {@code Resource} and {@code Resource[]} can be populated
 * from Strings when running in an ApplicationContext, using the particular
 * context's resource loading strategy.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	/** 用于从类路径加载的伪 URL 前缀："classpath:"。 */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	/**
	 * 返回指定资源位置的 Resource 句柄。
	 * 该句柄应始终是一个可重用的资源描述符，允许进行多次 Resource#getInputStream() 调用。
	 * 必须支持完全限定的 URLs，例如 "file:C:/test.dat"。
	 * 必须支持类路径伪-URLs，例如 "classpath:test.dat"。
	 * 应支持相对文件路径，例如 "WEB-INF/test.dat"。
	 * （这将是实现特定的，通常由 ApplicationContext 实现提供。）
	 * 请注意，Resource 句柄并不意味着资源存在；我们需要调用 Resource#exists 来检查其存在性。
	 * Return a {@code Resource} handle for the specified resource location.
	 * <p>The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p><ul>
	 * <li>Must support fully qualified URLs, for example, "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, for example, "classpath:test.dat".
	 * <li>Should support relative file paths, for example, "WEB-INF/test.dat".
	 * (This will be implementation-specific, typically provided by an
	 * ApplicationContext implementation.)
	 * </ul>
	 * <p>Note that a {@code Resource} handle does not imply an existing resource;
	 * you need to invoke {@link Resource#exists} to check for existence.
	 * @param location the resource location
	 * @return a corresponding {@code Resource} handle (never {@code null})
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	Resource getResource(String location);

	/**
	 * 公开此 ResourceLoader 使用的 ClassLoader。
	 * 需要直接访问 ClassLoader 的客户端可以与 ResourceLoader 以统一的方式这样做，而不是依赖线程上下文 ClassLoader。
	 * Expose the {@link ClassLoader} used by this {@code ResourceLoader}.
	 * <p>Clients which need to access the {@code ClassLoader} directly can do so
	 * in a uniform manner with the {@code ResourceLoader}, rather than relying
	 * on the thread context {@code ClassLoader}.
	 * @return the {@code ClassLoader}
	 * (only {@code null} if even the system {@code ClassLoader} isn't accessible)
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable
	ClassLoader getClassLoader();

}
