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

import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.nio.charset.Charset;

/**
 * 常用Content-Type类型枚举
 *
 * @author looly
 * @since 4.0.11
 */
public enum ContentType {

	/**
	 * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
	 */
	FORM_URLENCODED("application/x-www-form-urlencoded"),
	/**
	 * 文件上传编码，浏览器会把整个表单以控件为单位分割，并为每个部分加上Content-Disposition，并加上分割符(boundary)
	 */
	MULTIPART("multipart/form-data"),
	/**
	 * Rest请求JSON编码
	 */
	JSON("application/json"),
	/**
	 * Rest请求XML编码
	 */
	XML("application/xml"),
	/**
	 * text/plain编码
	 */
	TEXT_PLAIN("text/plain"),
	/**
	 * Rest请求text/xml编码
	 */
	TEXT_XML("text/xml"),
	/**
	 * text/html编码
	 */
	TEXT_HTML("text/html"),
	/**
	 * application/octet-stream编码
	 */
	OCTET_STREAM("application/octet-stream"),
	/**
	 * text/event-stream编码
	 */
	EVENT_STREAM("text/event-stream");

	private final String value;

	/**
	 * 构造
	 *
	 * @param value ContentType值
	 */
	ContentType(String value) {
		this.value = value;
	}

	/**
	 * 获取value值
	 *
	 * @return value值
	 * @since 5.2.6
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getValue();
	}

	/**
	 * 输出Content-Type字符串，附带编码信息
	 *
	 * @param charset 编码
	 * @return Content-Type字符串
	 */
	public String toString(Charset charset) {
		return build(this.value, charset);
	}

	/**
	 * 是否为默认Content-Type，默认包括{@code null}和application/x-www-form-urlencoded
	 *
	 * @param contentType 内容类型
	 * @return 是否为默认Content-Type
	 * @since 4.1.5
	 */
	public static boolean isDefault(String contentType) {
		return null == contentType || isFormUrlEncode(contentType);
	}

	/**
	 * 是否为application/x-www-form-urlencoded
	 *
	 * @param contentType 内容类型
	 * @return 是否为application/x-www-form-urlencoded
	 */
	public static boolean isFormUrlEncode(String contentType) {
		return StrUtil.startWithIgnoreCase(contentType, FORM_URLENCODED.toString());
	}

	/**
	 * 从请求参数的body中判断请求的Content-Type类型，支持的类型有：
	 *
	 * <pre>
	 * 1. application/json
	 * 1. application/xml
	 * </pre>
	 *
	 * @param body 请求参数体
	 * @return Content-Type类型，如果无法判断返回null
	 */
	public static ContentType get(String body) {
		ContentType contentType = null;
		if (StrUtil.isNotBlank(body)) {
			char firstChar = StrUtil.trimStart(body).charAt(0);
			switch (firstChar) {
				case '{':
				case '[':
					// JSON请求体
					contentType = JSON;
					break;
				case '<':
					// XML请求体
					contentType = XML;
					break;

				default:
					break;
			}
		}
		return contentType;
	}

	/**
	 * 输出Content-Type字符串，附带编码信息
	 *
	 * @param contentType Content-Type类型
	 * @param charset     编码
	 * @return Content-Type字符串
	 * @since 4.5.4
	 */
	public static String build(String contentType, Charset charset) {
		return StrUtil.format("{};charset={}", contentType, charset.name());
	}

	/**
	 * 输出Content-Type字符串，附带编码信息
	 *
	 * @param contentType Content-Type 枚举类型
	 * @param charset     编码
	 * @return Content-Type字符串
	 * @since 5.7.15
	 */
	public static String build(ContentType contentType, Charset charset) {
		return build(contentType.getValue(), charset);
	}
}
