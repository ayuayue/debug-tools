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
package io.github.future0923.debug.tools.base.hutool.core.convert.impl;

import io.github.future0923.debug.tools.base.hutool.core.convert.AbstractConverter;
import io.github.future0923.debug.tools.base.hutool.core.convert.ConvertException;
import io.github.future0923.debug.tools.base.hutool.core.date.DateTime;
import io.github.future0923.debug.tools.base.hutool.core.date.DateUtil;
import io.github.future0923.debug.tools.base.hutool.core.date.format.GlobalCustomFormat;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.time.temporal.TemporalAccessor;
import java.util.Calendar;

/**
 * 日期转换器
 *
 * @author Looly
 */
public class DateConverter extends AbstractConverter<java.util.Date> {
	private static final long serialVersionUID = 1L;

	private final Class<? extends java.util.Date> targetType;
	/**
	 * 日期格式化
	 */
	private String format;

	/**
	 * 构造
	 *
	 * @param targetType 目标类型
	 */
	public DateConverter(Class<? extends java.util.Date> targetType) {
		this.targetType = targetType;
	}

	/**
	 * 构造
	 *
	 * @param targetType 目标类型
	 * @param format     日期格式
	 */
	public DateConverter(Class<? extends java.util.Date> targetType, String format) {
		this.targetType = targetType;
		this.format = format;
	}

	/**
	 * 获取日期格式
	 *
	 * @return 设置日期格式
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 设置日期格式
	 *
	 * @param format 日期格式
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	protected java.util.Date convertInternal(Object value) {
		if (value == null || (value instanceof CharSequence && StrUtil.isBlank(value.toString()))) {
			return null;
		}
		if (value instanceof TemporalAccessor) {
			return wrap(DateUtil.date((TemporalAccessor) value));
		} else if (value instanceof Calendar) {
			return wrap(DateUtil.date((Calendar) value));
		} else if (value instanceof Number) {
			return wrap(((Number) value).longValue());
		} else {
			// 统一按照字符串处理
			final String valueStr = convertToStr(value);
			final DateTime dateTime = StrUtil.isBlank(this.format) //
					? DateUtil.parse(valueStr) //
					: DateUtil.parse(valueStr, this.format);
			if (null != dateTime) {
				return wrap(dateTime);
			}
		}

		throw new ConvertException("Can not convert {}:[{}] to {}", value.getClass().getName(), value, this.targetType.getName());
	}

	/**
	 * java.util.Date转为子类型
	 *
	 * @param date Date
	 * @return 目标类型对象
	 */
	private java.util.Date wrap(DateTime date) {
		// 返回指定类型
		if (java.util.Date.class == targetType) {
			return date.toJdkDate();
		}
		if (DateTime.class == targetType) {
			return date;
		}
		if (java.sql.Date.class == targetType) {
			return date.toSqlDate();
		}
		if (java.sql.Time.class == targetType) {
			return new java.sql.Time(date.getTime());
		}
		if (java.sql.Timestamp.class == targetType) {
			return date.toTimestamp();
		}

		throw new UnsupportedOperationException(StrUtil.format("Unsupported target Date type: {}", this.targetType.getName()));
	}

	/**
	 * java.util.Date转为子类型
	 *
	 * @param mills Date
	 * @return 目标类型对象
	 */
	private java.util.Date wrap(long mills) {
		if(GlobalCustomFormat.FORMAT_SECONDS.equals(this.format)){
			// Unix时间戳
			return DateUtil.date(mills * 1000);
		}

		// 返回指定类型
		if (java.util.Date.class == targetType) {
			return new java.util.Date(mills);
		}
		if (DateTime.class == targetType) {
			return DateUtil.date(mills);
		}
		if (java.sql.Date.class == targetType) {
			return new java.sql.Date(mills);
		}
		if (java.sql.Time.class == targetType) {
			return new java.sql.Time(mills);
		}
		if (java.sql.Timestamp.class == targetType) {
			return new java.sql.Timestamp(mills);
		}

		throw new UnsupportedOperationException(StrUtil.format("Unsupported target Date type: {}", this.targetType.getName()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<java.util.Date> getTargetType() {
		return (Class<java.util.Date>) this.targetType;
	}
}
