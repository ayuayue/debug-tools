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
package io.github.future0923.debug.tools.base.hutool.core.compress;

import io.github.future0923.debug.tools.base.hutool.core.io.IORuntimeException;
import io.github.future0923.debug.tools.base.hutool.core.io.IoUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Deflate算法<br>
 * Deflate是同时使用了LZ77算法与哈夫曼编码（Huffman Coding）的一个无损数据压缩算法。
 *
 * @author looly
 * @since 5.7.8
 */
public class Deflate implements Closeable {

	private final InputStream source;
	private OutputStream target;
	private final boolean nowrap;

	/**
	 * 创建Deflate
	 *
	 * @param source 源流
	 * @param target 目标流
	 * @param nowrap {@code true}表示兼容Gzip压缩
	 * @return this
	 */
	public static Deflate of(InputStream source, OutputStream target, boolean nowrap) {
		return new Deflate(source, target, nowrap);
	}

	/**
	 * 构造
	 *
	 * @param source 源流
	 * @param target 目标流
	 * @param nowrap {@code true}表示兼容Gzip压缩
	 */
	public Deflate(InputStream source, OutputStream target, boolean nowrap) {
		this.source = source;
		this.target = target;
		this.nowrap = nowrap;
	}

	/**
	 * 获取目标流
	 *
	 * @return 目标流
	 */
	public OutputStream getTarget() {
		return this.target;
	}

	/**
	 * 将普通数据流压缩
	 *
	 * @param level 压缩级别，0~9
	 * @return this
	 */
	public Deflate deflater(int level) {
		target= (target instanceof DeflaterOutputStream) ?
				(DeflaterOutputStream) target : new DeflaterOutputStream(target, new Deflater(level, nowrap));
		IoUtil.copy(source, target);
		try {
			((DeflaterOutputStream)target).finish();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	/**
	 * 将压缩流解压到target中
	 *
	 * @return this
	 */
	public Deflate inflater() {
		target = (target instanceof InflaterOutputStream) ?
				(InflaterOutputStream) target : new InflaterOutputStream(target, new Inflater(nowrap));
		IoUtil.copy(source, target);
		try {
			((InflaterOutputStream)target).finish();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	@Override
	public void close() {
		IoUtil.close(this.target);
		IoUtil.close(this.source);
	}
}
