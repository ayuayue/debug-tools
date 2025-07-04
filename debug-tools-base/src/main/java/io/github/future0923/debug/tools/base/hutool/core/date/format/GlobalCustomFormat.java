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
package io.github.future0923.debug.tools.base.hutool.core.date.format;

import io.github.future0923.debug.tools.base.hutool.core.date.DateUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.map.SafeConcurrentHashMap;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * 全局自定义格式<br>
 * 用于定义用户指定的日期格式和输出日期的关系
 *
 * @author looly
 * @since 5.7.2
 */
public class GlobalCustomFormat {

	/**
	 * 格式：秒时间戳（Unix时间戳）
	 */
	public static final String FORMAT_SECONDS = "#sss";
	/**
	 * 格式：毫秒时间戳
	 */
	public static final String FORMAT_MILLISECONDS = "#SSS";

	private static final Map<CharSequence, Function<Date, String>> formatterMap;
	private static final Map<CharSequence, Function<CharSequence, Date>> parserMap;

	static {
		formatterMap = new SafeConcurrentHashMap<>();
		parserMap = new SafeConcurrentHashMap<>();

		// Hutool预设的几种自定义格式
		putFormatter(FORMAT_SECONDS, (date) -> String.valueOf(Math.floorDiv(date.getTime(), 1000)));
		putParser(FORMAT_SECONDS, (dateStr) -> DateUtil.date(Math.multiplyExact(Long.parseLong(dateStr.toString()), 1000)));

		putFormatter(FORMAT_MILLISECONDS, (date) -> String.valueOf(date.getTime()));
		putParser(FORMAT_MILLISECONDS, (dateStr) -> DateUtil.date(Long.parseLong(dateStr.toString())));
	}

	/**
	 * 加入日期格式化规则
	 *
	 * @param format 格式
	 * @param func   格式化函数
	 */
	public static void putFormatter(String format, Function<Date, String> func) {
		Assert.notNull(format, "Format must be not null !");
		Assert.notNull(func, "Function must be not null !");
		formatterMap.put(format, func);
	}

	/**
	 * 加入日期解析规则
	 *
	 * @param format 格式
	 * @param func   解析函数
	 */
	public static void putParser(String format, Function<CharSequence, Date> func) {
		Assert.notNull(format, "Format must be not null !");
		Assert.notNull(func, "Function must be not null !");
		parserMap.put(format, func);
	}

	/**
	 * 检查指定格式是否为自定义格式
	 *
	 * @param format 格式
	 * @return 是否为自定义格式
	 */
	public static boolean isCustomFormat(String format) {
		return formatterMap.containsKey(format);
	}

	/**
	 * 使用自定义格式格式化日期
	 *
	 * @param date   日期
	 * @param format 自定义格式
	 * @return 格式化后的日期
	 */
	public static String format(Date date, CharSequence format) {
		if (null != formatterMap) {
			final Function<Date, String> func = formatterMap.get(format);
			if (null != func) {
				return func.apply(date);
			}
		}

		return null;
	}

	/**
	 * 使用自定义格式格式化日期
	 *
	 * @param temporalAccessor 日期
	 * @param format           自定义格式
	 * @return 格式化后的日期
	 */
	public static String format(TemporalAccessor temporalAccessor, CharSequence format) {
		return format(DateUtil.date(temporalAccessor), format);
	}

	/**
	 * 使用自定义格式解析日期
	 *
	 * @param dateStr 日期字符串
	 * @param format  自定义格式
	 * @return 格式化后的日期
	 */
	public static Date parse(CharSequence dateStr, String format) {
		if (null != parserMap) {
			final Function<CharSequence, Date> func = parserMap.get(format);
			if (null != func) {
				return func.apply(dateStr);
			}
		}

		return null;
	}
}
