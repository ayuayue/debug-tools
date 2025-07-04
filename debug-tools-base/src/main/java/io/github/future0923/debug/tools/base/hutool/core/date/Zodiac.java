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
package io.github.future0923.debug.tools.base.hutool.core.date;

import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;

import java.util.Calendar;
import java.util.Date;

/**
 * 星座 来自：https://blog.csdn.net/u010758605/article/details/48317881
 *
 * @author looly
 * @since 4.4.3
 */
public class Zodiac {

	/** 星座分隔时间日 */
	private static final int[] DAY_ARR = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };
	/** 星座 */
	private static final String[] ZODIACS = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
	private static final String[] CHINESE_ZODIACS = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };

	/**
	 * 通过生日计算星座
	 *
	 * @param date 出生日期
	 * @return 星座名
	 */
	public static String getZodiac(Date date) {
		return getZodiac(DateUtil.calendar(date));
	}

	/**
	 * 通过生日计算星座
	 *
	 * @param calendar 出生日期
	 * @return 星座名
	 */
	public static String getZodiac(Calendar calendar) {
		if (null == calendar) {
			return null;
		}
		return getZodiac(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 通过生日计算星座
	 *
	 * @param month 月，从0开始计数
	 * @param day 天
	 * @return 星座名
	 * @since 4.5.0
	 */
	public static String getZodiac(Month month, int day) {
		return getZodiac(month.getValue(), day);
	}

	/**
	 * 通过生日计算星座
	 *
	 * @param month 月，从0开始计数，见{@link Month#getValue()}
	 * @param day 天
	 * @return 星座名
	 */
	public static String getZodiac(int month, int day) {
		Assert.checkBetween(month,
			Month.JANUARY.getValue(),
			Month.DECEMBER.getValue(), "Unsupported month value, must be [0,12]");
		// 在分隔日前为前一个星座，否则为后一个星座
		return day < DAY_ARR[month] ? ZODIACS[month] : ZODIACS[month + 1];
	}

	// ----------------------------------------------------------------------------------------------------------- 生肖
	/**
	 * 通过生日计算生肖，只计算1900年后出生的人
	 *
	 * @param date 出生日期（年需农历）
	 * @return 星座名
	 */
	public static String getChineseZodiac(Date date) {
		return getChineseZodiac(DateUtil.calendar(date));
	}

	/**
	 * 通过生日计算生肖，只计算1900年后出生的人
	 *
	 * @param calendar 出生日期（年需农历）
	 * @return 星座名
	 */
	public static String getChineseZodiac(Calendar calendar) {
		if (null == calendar) {
			return null;
		}
		return getChineseZodiac(calendar.get(Calendar.YEAR));
	}

	/**
	 * 计算生肖，只计算1900年后出生的人
	 *
	 * @param year 农历年
	 * @return 生肖名
	 */
	public static String getChineseZodiac(int year) {
		if (year < 1900) {
			return null;
		}
		return CHINESE_ZODIACS[(year - 1900) % CHINESE_ZODIACS.length];
	}
}
