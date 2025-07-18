/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.base.hutool.core.bean;

import io.github.future0923.debug.tools.base.hutool.core.lang.func.Func0;
import io.github.future0923.debug.tools.base.hutool.core.map.ReferenceConcurrentMap;
import io.github.future0923.debug.tools.base.hutool.core.map.WeakConcurrentMap;

import java.beans.PropertyDescriptor;
import java.util.Map;

/**
 * Bean属性缓存<br>
 * 缓存用于防止多次反射造成的性能问题
 *
 * @author Looly
 */
public enum BeanInfoCache {
	INSTANCE;

	private final WeakConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> pdCache = new WeakConcurrentMap<>();
	private final WeakConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> ignoreCasePdCache = new WeakConcurrentMap<>();

	/**
	 * 获得属性名和{@link PropertyDescriptor}Map映射
	 *
	 * @param beanClass  Bean的类
	 * @param ignoreCase 是否忽略大小写
	 * @return 属性名和{@link PropertyDescriptor}Map映射
	 */
	public Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> beanClass, boolean ignoreCase) {
		return getCache(ignoreCase).get(beanClass);
	}

	/**
	 * 获得属性名和{@link PropertyDescriptor}Map映射
	 *
	 * @param beanClass  Bean的类
	 * @param ignoreCase 是否忽略大小写
	 * @param supplier   缓存对象产生函数
	 * @return 属性名和{@link PropertyDescriptor}Map映射
	 * @since 5.4.1
	 */
	public Map<String, PropertyDescriptor> getPropertyDescriptorMap(
			Class<?> beanClass,
			boolean ignoreCase,
			Func0<Map<String, PropertyDescriptor>> supplier) {
		return getCache(ignoreCase).computeIfAbsent(beanClass, (key)->supplier.callWithRuntimeException());
	}

	/**
	 * 加入缓存
	 *
	 * @param beanClass                      Bean的类
	 * @param fieldNamePropertyDescriptorMap 属性名和{@link PropertyDescriptor}Map映射
	 * @param ignoreCase                     是否忽略大小写
	 */
	public void putPropertyDescriptorMap(Class<?> beanClass, Map<String, PropertyDescriptor> fieldNamePropertyDescriptorMap, boolean ignoreCase) {
		getCache(ignoreCase).put(beanClass, fieldNamePropertyDescriptorMap);
	}

	/**
	 * 清空缓存
	 *
	 * @since 5.7.21
	 */
	public void clear() {
		this.pdCache.clear();
		this.ignoreCasePdCache.clear();
	}

	/**
	 * 根据是否忽略字段名的大小写，返回不用Cache对象
	 *
	 * @param ignoreCase 是否忽略大小写
	 * @return {@link ReferenceConcurrentMap}
	 * @since 5.4.1
	 */
	private ReferenceConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> getCache(boolean ignoreCase) {
		return ignoreCase ? ignoreCasePdCache : pdCache;
	}
}
