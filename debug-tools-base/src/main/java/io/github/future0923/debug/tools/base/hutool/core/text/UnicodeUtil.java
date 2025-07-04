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
package io.github.future0923.debug.tools.base.hutool.core.text;

import io.github.future0923.debug.tools.base.hutool.core.util.CharUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.HexUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

/**
 * 提供Unicode字符串和普通字符串之间的转换
 *
 * @author 兜兜毛毛, looly
 * @since 4.0.0
 */
public class UnicodeUtil {

	/**
	 * Unicode字符串转为普通字符串<br>
	 * Unicode字符串的表现方式为：\\uXXXX
	 *
	 * @param unicode Unicode字符串
	 * @return 普通字符串
	 */
	public static String toString(String unicode) {
		if (StrUtil.isBlank(unicode)) {
			return unicode;
		}

		final int len = unicode.length();
		StringBuilder sb = new StringBuilder(len);
		int i;
		int pos = 0;
		while ((i = StrUtil.indexOfIgnoreCase(unicode, "\\u", pos)) != -1) {
			sb.append(unicode, pos, i);//写入Unicode符之前的部分
			pos = i;
			if (i + 5 < len) {
				char c;
				try {
					c = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
					sb.append(c);
					pos = i + 6;//跳过整个Unicode符
				} catch (NumberFormatException e) {
					//非法Unicode符，跳过
					sb.append(unicode, pos, i + 2);//写入"\\u"
					pos = i + 2;
				}
			} else {
				//非Unicode符，结束
				break;
			}
		}

		if (pos < len) {
			sb.append(unicode, pos, len);
		}
		return sb.toString();
	}

	/**
	 * 字符编码为Unicode形式
	 *
	 * @param c 被编码的字符
	 * @return Unicode字符串
	 * @since 5.6.2
	 * @see HexUtil#toUnicodeHex(char)
	 */
	public static String toUnicode(char c) {
		return HexUtil.toUnicodeHex(c);
	}

	/**
	 * 字符编码为Unicode形式
	 *
	 * @param c 被编码的字符
	 * @return Unicode字符串
	 * @since 5.6.2
	 * @see HexUtil#toUnicodeHex(int)
	 */
	public static String toUnicode(int c) {
		return HexUtil.toUnicodeHex(c);
	}

	/**
	 * 字符串编码为Unicode形式
	 *
	 * @param str 被编码的字符串
	 * @return Unicode字符串
	 */
	public static String toUnicode(String str) {
		return toUnicode(str, true);
	}

	/**
	 * 字符串编码为Unicode形式
	 *
	 * @param str         被编码的字符串
	 * @param isSkipAscii 是否跳过ASCII字符（只跳过可见字符）
	 * @return Unicode字符串
	 */
	public static String toUnicode(String str, boolean isSkipAscii) {
		if (StrUtil.isEmpty(str)) {
			return str;
		}

		final int len = str.length();
		final StringBuilder unicode = new StringBuilder(str.length() * 6);
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (isSkipAscii && CharUtil.isAsciiPrintable(c)) {
				unicode.append(c);
			} else {
				unicode.append(HexUtil.toUnicodeHex(c));
			}
		}
		return unicode.toString();
	}
}
