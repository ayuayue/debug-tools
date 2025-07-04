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

import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollectionUtil;
import io.github.future0923.debug.tools.base.hutool.core.io.resource.Resource;
import io.github.future0923.debug.tools.base.hutool.core.map.CaseInsensitiveMap;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.CharsetUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * http基类
 *
 * @param <T> 子类类型，方便链式编程
 * @author Looly
 */
@SuppressWarnings("unchecked")
public abstract class HttpBase<T> {

	/**
	 * 默认的请求编码、URL的encode、decode编码
	 */
	protected static final Charset DEFAULT_CHARSET = CharsetUtil.CHARSET_UTF_8;

	/**
	 * HTTP/1.0
	 */
	public static final String HTTP_1_0 = "HTTP/1.0";
	/**
	 * HTTP/1.1
	 */
	public static final String HTTP_1_1 = "HTTP/1.1";

	/**
	 * 是否聚合重复请求头
	 */
	protected boolean isHeaderAggregated = false;
	/**
	 * 存储头信息
	 */
	protected Map<String, List<String>> headers = new HashMap<>();
	/**
	 * 编码
	 */
	protected Charset charset = DEFAULT_CHARSET;
	/**
	 * http版本
	 */
	protected String httpVersion = HTTP_1_1;
	/**
	 * 存储主体
	 */
	protected Resource body;

	// ---------------------------------------------------------------- Headers start

	/**
	 * 根据name获取头信息<br>
	 * 根据RFC2616规范，header的name不区分大小写
	 *
	 * @param name Header名
	 * @return Header值
	 */
	public String header(String name) {
		final List<String> values = headerList(name);
		if (CollectionUtil.isEmpty(values)) {
			return null;
		}
		return values.get(0);
	}

	/**
	 * 根据name获取头信息列表
	 *
	 * @param name Header名
	 * @return Header值
	 * @since 3.1.1
	 */
	public List<String> headerList(String name) {
		if (StrUtil.isBlank(name)) {
			return null;
		}

		final CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>(this.headers);
		return headersIgnoreCase.get(name.trim());
	}

	/**
	 * 根据name获取头信息
	 *
	 * @param name Header名
	 * @return Header值
	 */
	public String header(Header name) {
		if (null == name) {
			return null;
		}
		return header(name.toString());
	}

	/**
	 * 设置一个header<br>
	 * 如果覆盖模式，则替换之前的值，否则加入到值列表中
	 *
	 * @param name       Header名
	 * @param value      Header值
	 * @param isOverride 是否覆盖已有值
	 * @return T 本身
	 */
	public T header(String name, String value, boolean isOverride) {
		if (null != name && null != value) {
			final List<String> values = headers.get(name.trim());
			if (isOverride || CollectionUtil.isEmpty(values)) {
				final ArrayList<String> valueList = new ArrayList<>();
				valueList.add(value);
				headers.put(name.trim(), valueList);
			} else {
				values.add(value.trim());
			}
		}
		return (T) this;
	}

	/**
	 * 设置一个header<br>
	 * 如果覆盖模式，则替换之前的值，否则加入到值列表中
	 *
	 * @param name       Header名
	 * @param value      Header值
	 * @param isOverride 是否覆盖已有值
	 * @return T 本身
	 */
	public T header(Header name, String value, boolean isOverride) {
		return header(name.toString(), value, isOverride);
	}

	/**
	 * 设置一个header<br>
	 * 覆盖模式，则替换之前的值
	 *
	 * @param name  Header名
	 * @param value Header值
	 * @return T 本身
	 */
	public T header(Header name, String value) {
		return header(name.toString(), value, true);
	}

	/**
	 * 设置一个header<br>
	 * 覆盖模式，则替换之前的值
	 *
	 * @param name  Header名
	 * @param value Header值
	 * @return T 本身
	 */
	public T header(String name, String value) {
		return header(name, value, true);
	}

	/**
	 * 设置请求头
	 *
	 * @param headers    请求头
	 * @param isOverride 是否覆盖已有头信息
	 * @return this
	 * @since 4.6.3
	 */
	public T headerMap(Map<String, String> headers, boolean isOverride) {
		if (MapUtil.isEmpty(headers)) {
			return (T) this;
		}

		for (Entry<String, String> entry : headers.entrySet()) {
			this.header(entry.getKey(), StrUtil.nullToEmpty(entry.getValue()), isOverride);
		}
		return (T) this;
	}

	/**
	 * 设置请求头<br>
	 * 不覆盖原有请求头
	 *
	 * @param headers 请求头
	 * @return this
	 */
	public T header(Map<String, List<String>> headers) {
		return header(headers, false);
	}

	/**
	 * 设置请求头
	 *
	 * @param headers    请求头
	 * @param isOverride 是否覆盖已有头信息
	 * @return this
	 * @since 4.0.8
	 */
	public T header(Map<String, List<String>> headers, boolean isOverride) {
		if (MapUtil.isEmpty(headers)) {
			return (T) this;
		}

		String name;
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			name = entry.getKey();
			for (String value : entry.getValue()) {
				this.header(name, StrUtil.nullToEmpty(value), isOverride);
			}
		}
		return (T) this;
	}

	/**
	 * 新增请求头<br>
	 * 不覆盖原有请求头
	 *
	 * @param headers 请求头
	 * @return this
	 * @since 4.0.3
	 */
	public T addHeaders(Map<String, String> headers) {
		if (MapUtil.isEmpty(headers)) {
			return (T) this;
		}

		for (Entry<String, String> entry : headers.entrySet()) {
			this.header(entry.getKey(), StrUtil.nullToEmpty(entry.getValue()), false);
		}
		return (T) this;
	}

	/**
	 * 移除一个头信息
	 *
	 * @param name Header名
	 * @return this
	 */
	public T removeHeader(String name) {
		if (name != null) {
			headers.remove(name.trim());
		}
		return (T) this;
	}

	/**
	 * 移除一个头信息
	 *
	 * @param name Header名
	 * @return this
	 */
	public T removeHeader(Header name) {
		return removeHeader(name.toString());
	}

	/**
	 * 获取headers
	 *
	 * @return Headers Map
	 */
	public Map<String, List<String>> headers() {
		return Collections.unmodifiableMap(headers);
	}

	/**
	 * 清除所有头信息，包括全局头信息
	 *
	 * @return this
	 * @since 5.7.13
	 */
	public T clearHeaders() {
		this.headers.clear();
		return (T) this;
	}

	/**
	 * 设置是否需要聚合重复的请求头，将重复的请求头聚合为,拼接
	 *
	 * @param aggregate 是否需要聚合
	 * @return this
	 * @since 5.8.37
	 */
	public T headerAggregation(boolean aggregate) {
		this.isHeaderAggregated = aggregate;
		return (T) this;
	}

	/**
	 * 获取是否需要聚合请求头状态
	 *
	 * @return isHeaderAggregated 请求头聚合状态
	 * @since 5.8.37
	 */
	public boolean isHeaderAggregated() {
		return isHeaderAggregated;
	}
	// ---------------------------------------------------------------- Headers end

	/**
	 * 返回http版本
	 *
	 * @return String
	 */
	public String httpVersion() {
		return httpVersion;
	}

	/**
	 * 设置http版本，此方法不会影响到实际请求的HTTP版本，只用于帮助判断是否connect:Keep-Alive
	 *
	 * @param httpVersion Http版本，{@link HttpBase#HTTP_1_0}，{@link HttpBase#HTTP_1_1}
	 * @return this
	 */
	public T httpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
		return (T) this;
	}

	/**
	 * 获取bodyBytes存储字节码
	 *
	 * @return byte[]
	 */
	public byte[] bodyBytes() {
		return this.body == null ? null : this.body.readBytes();
	}

	/**
	 * 返回字符集
	 *
	 * @return 字符集
	 */
	public String charset() {
		return charset.name();
	}

	/**
	 * 设置字符集
	 *
	 * @param charset 字符集
	 * @return T 自己
	 * @see CharsetUtil
	 */
	public T charset(String charset) {
		if (StrUtil.isNotBlank(charset)) {
			charset(Charset.forName(charset));
		}
		return (T) this;
	}

	/**
	 * 设置字符集
	 *
	 * @param charset 字符集
	 * @return T 自己
	 * @see CharsetUtil
	 */
	public T charset(Charset charset) {
		if (null != charset) {
			this.charset = charset;
		}
		return (T) this;
	}

	@Override
	public String toString() {
		final StringBuilder sb = StrUtil.builder();
		sb.append("Headers: ").append(StrUtil.CRLF);
		for (Entry<String, List<String>> entry : this.headers.entrySet()) {
			sb.append("    ").append(
							entry.getKey()).append(": ").append(CollUtil.join(entry.getValue(), ","))
					.append(StrUtil.CRLF);
		}

		sb.append("Body: ").append(StrUtil.CRLF);
		sb.append("    ").append(StrUtil.str(this.bodyBytes(), this.charset)).append(StrUtil.CRLF);

		return sb.toString();
	}
}
