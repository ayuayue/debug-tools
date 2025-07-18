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
package io.github.future0923.debug.tools.base.hutool.core.text.csv;

import io.github.future0923.debug.tools.base.hutool.core.bean.BeanUtil;
import io.github.future0923.debug.tools.base.hutool.core.collection.ArrayIter;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.convert.Convert;
import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.hutool.core.io.IORuntimeException;
import io.github.future0923.debug.tools.base.hutool.core.io.IoUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.CharUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.CharsetUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * CSV数据写出器
 *
 * @author Looly
 * @since 4.0.5
 */
public final class CsvWriter implements Closeable, Flushable, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 写出器
	 */
	private final Writer writer;
	/**
	 * 写出配置
	 */
	private final CsvWriteConfig config;
	/**
	 * 是否处于新行开始
	 */
	private boolean newline = true;
	/**
	 * 是否首行，即CSV开始的位置，当初始化时默认为true，一旦写入内容，为false
	 */
	private boolean isFirstLine = true;

	// --------------------------------------------------------------------------------------------------- Constructor start

	/**
	 * 构造，覆盖已有文件（如果存在），默认编码UTF-8
	 *
	 * @param filePath File CSV文件路径
	 */
	public CsvWriter(String filePath) {
		this(FileUtil.file(filePath));
	}

	/**
	 * 构造，覆盖已有文件（如果存在），默认编码UTF-8
	 *
	 * @param file File CSV文件
	 */
	public CsvWriter(File file) {
		this(file, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 构造，覆盖已有文件（如果存在）
	 *
	 * @param filePath File CSV文件路径
	 * @param charset  编码
	 */
	public CsvWriter(String filePath, Charset charset) {
		this(FileUtil.file(filePath), charset);
	}

	/**
	 * 构造，覆盖已有文件（如果存在）
	 *
	 * @param file    File CSV文件
	 * @param charset 编码
	 */
	public CsvWriter(File file, Charset charset) {
		this(file, charset, false);
	}

	/**
	 * 构造
	 *
	 * @param filePath File CSV文件路径
	 * @param charset  编码
	 * @param isAppend 是否追加
	 */
	public CsvWriter(String filePath, Charset charset, boolean isAppend) {
		this(FileUtil.file(filePath), charset, isAppend);
	}

	/**
	 * 构造
	 *
	 * @param file     CSV文件
	 * @param charset  编码
	 * @param isAppend 是否追加
	 */
	public CsvWriter(File file, Charset charset, boolean isAppend) {
		this(file, charset, isAppend, null);
	}

	/**
	 * 构造
	 *
	 * @param filePath CSV文件路径
	 * @param charset  编码
	 * @param isAppend 是否追加
	 * @param config   写出配置，null则使用默认配置
	 */
	public CsvWriter(String filePath, Charset charset, boolean isAppend, CsvWriteConfig config) {
		this(FileUtil.file(filePath), charset, isAppend, config);
	}

	/**
	 * 构造
	 *
	 * @param file     CSV文件
	 * @param charset  编码
	 * @param isAppend 是否追加，append模式下，endingLineBreak自动设置为true
	 * @param config   写出配置，null则使用默认配置
	 */
	public CsvWriter(File file, Charset charset, boolean isAppend, CsvWriteConfig config) {
		this(FileUtil.getWriter(file, charset, isAppend), isAppend?(config==null?CsvWriteConfig.defaultConfig().setEndingLineBreak(true):config.setEndingLineBreak(true)):config);
	}

	/**
	 * 构造，使用默认配置
	 *
	 * @param writer {@link Writer}
	 */
	public CsvWriter(Writer writer) {
		this(writer, null);
	}

	/**
	 * 构造
	 *
	 * @param writer Writer
	 * @param config 写出配置，null则使用默认配置
	 */
	public CsvWriter(Writer writer, CsvWriteConfig config) {
		this.writer = (writer instanceof BufferedWriter) ? writer : new BufferedWriter(writer);
		this.config = ObjectUtil.defaultIfNull(config, CsvWriteConfig::defaultConfig);
	}
	// --------------------------------------------------------------------------------------------------- Constructor end

	/**
	 * 设置是否始终使用文本分隔符，文本包装符，默认false，按需添加
	 *
	 * @param alwaysDelimitText 是否始终使用文本分隔符，文本包装符，默认false，按需添加
	 * @return this
	 */
	public CsvWriter setAlwaysDelimitText(boolean alwaysDelimitText) {
		this.config.setAlwaysDelimitText(alwaysDelimitText);
		return this;
	}

	/**
	 * 设置换行符
	 *
	 * @param lineDelimiter 换行符
	 * @return this
	 */
	public CsvWriter setLineDelimiter(char[] lineDelimiter) {
		this.config.setLineDelimiter(lineDelimiter);
		return this;
	}

	/**
	 * 将多行写出到Writer
	 *
	 * @param lines 多行数据
	 * @return this
	 * @throws IORuntimeException IO异常
	 */
	public CsvWriter write(String[]... lines) throws IORuntimeException {
		return write(new ArrayIter<>(lines));
	}

	/**
	 * 将多行写出到Writer
	 *
	 * @param lines 多行数据，每行数据可以是集合或者数组
	 * @return this
	 * @throws IORuntimeException IO异常
	 */
	public CsvWriter write(Iterable<?> lines) throws IORuntimeException {
		if (CollUtil.isNotEmpty(lines)) {
			for (Object values : lines) {
				appendLine(Convert.toStrArray(values));
			}
			flush();
		}
		return this;
	}

	/**
	 * 将一个 CsvData 集合写出到Writer
	 *
	 * @param csvData CsvData
	 * @return this
	 * @since 5.7.4
	 */
	public CsvWriter write(CsvData csvData) {
		if (csvData != null) {
			// 1、写header
			final List<String> header = csvData.getHeader();
			if (CollUtil.isNotEmpty(header)) {
				this.writeHeaderLine(header.toArray(new String[0]));
			}
			// 2、写内容
			this.write(csvData.getRows());
			flush();
		}
		return this;
	}

	/**
	 * 将一个Bean集合写出到Writer，并自动生成表头
	 *
	 * @param beans Bean集合
	 * @return this
	 */
	public CsvWriter writeBeans(Iterable<?> beans) {
		return writeBeans(beans, (String[]) null);
	}

	/**
	 * 将一个Bean集合写出到Writer，并自动生成表头
	 *
	 * @param beans Bean集合
	 * @param properties Bean 中指定的可以导出的属性
	 * @return this
	 */
	public CsvWriter writeBeans(Iterable<?> beans, String... properties) {
		return writeBeans(beans, true, properties);
	}

	/**
	 * 将一个Bean集合写出到Writer，并自动生成表头
	 *
	 * @param beans Bean集合
	 * @param writeHeaderLine 是否写出表头，即Bean的字段名称列表作为首行
	 * @param properties Bean 中指定的可以导出的属性
	 * @return this
	 * @since 5.8.29
	 */
	@SuppressWarnings("resource")
	public CsvWriter writeBeans(Iterable<?> beans, boolean writeHeaderLine, String... properties) {
		if (CollUtil.isNotEmpty(beans)) {
			boolean isFirst = writeHeaderLine;
			Map<String, Object> map;
			for (Object bean : beans) {
				map = BeanUtil.beanToMap(bean, properties);
				if (isFirst) {
					writeHeaderLine(map.keySet().toArray(new String[0]));
					isFirst = false;
				}
				writeLine(Convert.toStrArray(map.values()));
			}
			flush();
		}
		return this;
	}

	/**
	 * 写出一行头部行，支持标题别名
	 *
	 * @param fields 字段列表 ({@code null} 值会被做为空值追加
	 * @return this
	 * @throws IORuntimeException IO异常
	 * @since 5.7.10
	 */
	public CsvWriter writeHeaderLine(String... fields) throws IORuntimeException {
		final Map<String, String> headerAlias = this.config.headerAlias;
		if (MapUtil.isNotEmpty(headerAlias)) {
			// 标题别名替换
			String alias;
			for (int i = 0; i < fields.length; i++) {
				alias = headerAlias.get(fields[i]);
				if (null != alias) {
					fields[i] = alias;
				}
			}
		}
		return writeLine(fields);
	}

	/**
	 * 写出一行
	 *
	 * @param fields 字段列表 ({@code null} 值会被做为空值追加)
	 * @return this
	 * @throws IORuntimeException IO异常
	 * @since 5.5.7
	 */
	public CsvWriter writeLine(String... fields) throws IORuntimeException {
		if (ArrayUtil.isEmpty(fields)) {
			return writeLine();
		}
		appendLine(fields);
		return this;
	}

	/**
	 * 追加新行（换行）
	 *
	 * @return this
	 * @throws IORuntimeException IO异常
	 */
	public CsvWriter writeLine() throws IORuntimeException {
		try {
			writer.write(config.lineDelimiter);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		newline = true;
		return this;
	}

	/**
	 * 写出一行注释，注释符号可自定义<br>
	 * 如果注释符不存在，则抛出异常
	 *
	 * @param comment 注释内容
	 * @return this
	 * @see CsvConfig#commentCharacter
	 * @since 5.5.7
	 */
	public CsvWriter writeComment(String comment) {
		Assert.notNull(this.config.commentCharacter, "Comment is disable!");
		try {
			if(isFirstLine){
				// 首行不补换行符
				isFirstLine = false;
			}else {
				writer.write(config.lineDelimiter);
			}
			writer.write(this.config.commentCharacter);
			writer.write(comment);
			newline = true;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	@SuppressWarnings("resource")
	@Override
	public void close() {
		if(this.config.endingLineBreak){
			//https://gitee.com/chinabugotech/hutool/issues/I75K5G
			writeLine();
		}
		IoUtil.close(this.writer);
	}

	@Override
	public void flush() throws IORuntimeException {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	// --------------------------------------------------------------------------------------------------- Private method start

	/**
	 * 追加一行，末尾会自动换行，但是追加前不会换行
	 *
	 * @param fields 字段列表 ({@code null} 值会被做为空值追加)
	 * @throws IORuntimeException IO异常
	 */
	private void appendLine(String... fields) throws IORuntimeException {
		try {
			doAppendLine(fields);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * 追加一行，末尾会自动换行，但是追加前不会换行
	 *
	 * @param fields 字段列表 ({@code null} 值会被做为空值追加)
	 * @throws IOException IO异常
	 */
	private void doAppendLine(String... fields) throws IOException {
		if (null != fields) {
			if(isFirstLine){
				// 首行不补换行符
				isFirstLine = false;
			}else {
				writer.write(config.lineDelimiter);
			}
			for (String field : fields) {
				appendField(field);
			}
			newline = true;
		}
	}

	/**
	 * 在当前行追加字段值，自动添加字段分隔符，如果有必要，自动包装字段
	 *
	 * @param value 字段值，{@code null} 会被做为空串写出
	 * @throws IOException IO异常
	 */
	private void appendField(final String value) throws IOException {
		boolean alwaysDelimitText = config.alwaysDelimitText;
		char textDelimiter = config.textDelimiter;
		char fieldSeparator = config.fieldSeparator;

		if (false == newline) {
			writer.write(fieldSeparator);
		} else {
			newline = false;
		}

		if (null == value) {
			if (alwaysDelimitText) {
				writer.write(new char[]{textDelimiter, textDelimiter});
			}
			return;
		}

		final char[] valueChars = value.toCharArray();
		boolean needsTextDelimiter = alwaysDelimitText;
		boolean containsTextDelimiter = false;

		for (final char c : valueChars) {
			if (c == textDelimiter) {
				// 字段值中存在包装符
				containsTextDelimiter = needsTextDelimiter = true;
				break;
			} else if (c == fieldSeparator || c == CharUtil.LF || c == CharUtil.CR) {
				// 包含分隔符或换行符需要包装符包装
				needsTextDelimiter = true;
			}
		}

		// 包装符开始
		if (needsTextDelimiter) {
			writer.write(textDelimiter);
		}

		// 正文
		if (containsTextDelimiter) {
			for (final char c : valueChars) {
				// 转义文本包装符
				if (c == textDelimiter) {
					writer.write(textDelimiter);
				}
				writer.write(c);
			}
		} else {
			writer.write(valueChars);
		}

		// 包装符结尾
		if (needsTextDelimiter) {
			writer.write(textDelimiter);
		}
	}
	// --------------------------------------------------------------------------------------------------- Private method end
}
