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
package io.github.future0923.debug.tools.base.hutool.core.net.url;

import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.collection.ListUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.net.RFC3986;
import io.github.future0923.debug.tools.base.hutool.core.net.URLDecoder;
import io.github.future0923.debug.tools.base.hutool.core.util.CharUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * URL中Path部分的封装
 *
 * @author looly
 * @since 5.3.1
 */
public class UrlPath {

	private List<String> segments;
	private boolean withEngTag;

	/**
	 * 构建UrlPath
	 *
	 * @param pathStr 初始化的路径字符串
	 * @param charset decode用的编码，null表示不做decode
	 * @return UrlPath
	 */
	public static UrlPath of(CharSequence pathStr, Charset charset) {
		final UrlPath urlPath = new UrlPath();
		urlPath.parse(pathStr, charset);
		return urlPath;
	}

	/**
	 * 是否path的末尾加 /
	 *
	 * @param withEngTag 是否path的末尾加 /
	 * @return this
	 */
	public UrlPath setWithEndTag(boolean withEngTag) {
		this.withEngTag = withEngTag;
		return this;
	}

	/**
	 * 获取path的节点列表
	 *
	 * @return 节点列表
	 */
	public List<String> getSegments() {
		return ObjectUtil.defaultIfNull(this.segments, ListUtil::empty);
	}

	/**
	 * 获得指定节点
	 *
	 * @param index 节点位置
	 * @return 节点，无节点或者越界返回null
	 */
	public String getSegment(int index) {
		if (null == this.segments || index >= this.segments.size()) {
			return null;
		}
		return this.segments.get(index);
	}

	/**
	 * 添加到path最后面
	 *
	 * @param segment Path节点
	 * @return this
	 */
	public UrlPath add(CharSequence segment) {
		addInternal(fixPath(segment), false);
		return this;
	}

	/**
	 * 添加到path最前面
	 *
	 * @param segment Path节点
	 * @return this
	 */
	public UrlPath addBefore(CharSequence segment) {
		addInternal(fixPath(segment), true);
		return this;
	}

	/**
	 * 解析path
	 *
	 * @param path    路径，类似于aaa/bb/ccc或/aaa/bbb/ccc
	 * @param charset decode编码，null表示不解码
	 * @return this
	 */
	public UrlPath parse(CharSequence path, Charset charset) {
		if (StrUtil.isNotEmpty(path)) {
			// 原URL中以/结尾，则这个规则需保留，issue#I1G44J@Gitee
			if(StrUtil.endWith(path, CharUtil.SLASH)){
				this.withEngTag = true;
			}

			path = fixPath(path);
			if(StrUtil.isNotEmpty(path)){
				final List<String> split = StrUtil.split(path, '/');
				for (String seg : split) {
					addInternal(URLDecoder.decodeForPath(seg, charset), false);
				}
			}
		}

		return this;
	}

	/**
	 * 构建path，前面带'/'<br>
	 * <pre>
	 *     path = path-abempty / path-absolute / path-noscheme / path-rootless / path-empty
	 * </pre>
	 *
	 * @param charset encode编码，null表示不做encode
	 * @return 如果没有任何内容，则返回空字符串""
	 */
	public String build(Charset charset) {
		return build(charset, true);
	}

	/**
	 * 构建path，前面带'/'<br>
	 * <pre>
	 *     path = path-abempty / path-absolute / path-noscheme / path-rootless / path-empty
	 * </pre>
	 *
	 * @param charset encode编码，null表示不做encode
	 * @param encodePercent 是否编码`%`
	 * @return 如果没有任何内容，则返回空字符串""
	 * @since 5.8.0
	 */
	public String build(Charset charset, boolean encodePercent) {
		if (CollUtil.isEmpty(this.segments)) {
			// 没有节点的path取决于是否末尾追加/，如果不追加返回空串，否则返回/
			return withEngTag ? StrUtil.SLASH : StrUtil.EMPTY;
		}

		final char[] safeChars = encodePercent ? null : new char[]{'%'};
		final StringBuilder builder = new StringBuilder();
		for (final String segment : segments) {
			// https://www.ietf.org/rfc/rfc3986.html#section-3.3
			// 此处Path中是允许有`:`的，之前理解有误，应该是相对URI的第一个segment中不允许有`:`
			builder.append(CharUtil.SLASH).append(RFC3986.SEGMENT.encode(segment, charset, safeChars));
		}

		if(withEngTag){
			if (StrUtil.isEmpty(builder)) {
				// 空白追加是保证以/开头
				builder.append(CharUtil.SLASH);
			}else if (false == StrUtil.endWith(builder, CharUtil.SLASH)) {
				// 尾部没有/则追加，否则不追加
				builder.append(CharUtil.SLASH);
			}
		}

		return builder.toString();
	}

	@Override
	public String toString() {
		return build(null);
	}

	/**
	 * 增加节点
	 *
	 * @param segment 节点
	 * @param before  是否在前面添加
	 */
	private void addInternal(CharSequence segment, boolean before) {
		if (this.segments == null) {
			this.segments = new LinkedList<>();
		}

		final String seg = StrUtil.str(segment);
		if (before) {
			this.segments.add(0, seg);
		} else {
			this.segments.add(seg);
		}
	}

	/**
	 * 修正路径，包括去掉前后的/，去掉空白符
	 *
	 * @param path 节点或路径path
	 * @return 修正后的路径
	 */
	private static String fixPath(CharSequence path) {
		Assert.notNull(path, "Path segment must be not null!");
		if ("/".contentEquals(path)) {
			return StrUtil.EMPTY;
		}

		String segmentStr = StrUtil.trim(path);
		segmentStr = StrUtil.removePrefix(segmentStr, StrUtil.SLASH);
		segmentStr = StrUtil.removeSuffix(segmentStr, StrUtil.SLASH);
		segmentStr = StrUtil.trim(segmentStr);
		return segmentStr;
	}
}
