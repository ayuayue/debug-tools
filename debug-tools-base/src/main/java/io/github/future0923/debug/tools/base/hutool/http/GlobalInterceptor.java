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
package io.github.future0923.debug.tools.base.hutool.http;

/**
 * 全局的拦截器<br>
 * 包括请求拦截器和响应拦截器
 *
 * @author looly
 * @since 5.8.0
 */
public enum GlobalInterceptor {
	INSTANCE;

	private final HttpInterceptor.Chain<HttpRequest> requestInterceptors = new HttpInterceptor.Chain<>();
	private final HttpInterceptor.Chain<HttpResponse> responseInterceptors = new HttpInterceptor.Chain<>();

	/**
	 * 设置拦截器，用于在请求前重新编辑请求
	 *
	 * @param interceptor 拦截器实现
	 * @return this
	 */
	synchronized public GlobalInterceptor addRequestInterceptor(HttpInterceptor<HttpRequest> interceptor) {
		this.requestInterceptors.addChain(interceptor);
		return this;
	}

	/**
	 * 设置拦截器，用于在响应读取后完成编辑或读取
	 *
	 * @param interceptor 拦截器实现
	 * @return this
	 */
	synchronized public GlobalInterceptor addResponseInterceptor(HttpInterceptor<HttpResponse> interceptor) {
		this.responseInterceptors.addChain(interceptor);
		return this;
	}

	/**
	 * 清空请求和响应拦截器
	 *
	 * @return this
	 */
	public GlobalInterceptor clear() {
		clearRequest();
		clearResponse();
		return this;
	}

	/**
	 * 清空请求拦截器
	 *
	 * @return this
	 */
	synchronized public GlobalInterceptor clearRequest() {
		requestInterceptors.clear();
		return this;
	}

	/**
	 * 清空响应拦截器
	 *
	 * @return this
	 */
	synchronized public GlobalInterceptor clearResponse() {
		responseInterceptors.clear();
		return this;
	}

	/**
	 * 复制请求过滤器列表
	 *
	 * @return {@link cn.hutool.http.HttpInterceptor.Chain}
	 */
	HttpInterceptor.Chain<HttpRequest> getCopiedRequestInterceptor() {
		final HttpInterceptor.Chain<HttpRequest> copied = new HttpInterceptor.Chain<>();
		for (HttpInterceptor<HttpRequest> interceptor : this.requestInterceptors) {
			copied.addChain(interceptor);
		}
		return copied;
	}

	/**
	 * 复制响应过滤器列表
	 *
	 * @return {@link cn.hutool.http.HttpInterceptor.Chain}
	 */
	HttpInterceptor.Chain<HttpResponse> getCopiedResponseInterceptor() {
		final HttpInterceptor.Chain<HttpResponse> copied = new HttpInterceptor.Chain<>();
		for (HttpInterceptor<HttpResponse> interceptor : this.responseInterceptors) {
			copied.addChain(interceptor);
		}
		return copied;
	}
}
