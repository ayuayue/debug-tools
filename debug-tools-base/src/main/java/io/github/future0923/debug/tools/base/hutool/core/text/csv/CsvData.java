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


import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * CSV数据，包括头部信息和行数据，参考：FastCSV
 *
 * @author Looly
 */
public class CsvData implements Iterable<CsvRow>, Serializable {
	private static final long serialVersionUID = 1L;

	private final List<String> header;
	private final List<CsvRow> rows;

	/**
	 * 构造
	 *
	 * @param header 头信息, 可以为null
	 * @param rows 行
	 */
	public CsvData(final List<String> header, final List<CsvRow> rows) {
		this.header = header;
		this.rows = rows;
	}

	/**
	 * 总行数
	 *
	 * @return 总行数
	 */
	public int getRowCount() {
		return this.rows.size();
	}

	/**
	 * 获取头信息列表，如果无头信息为{@code Null}，返回列表为只读列表
	 *
	 * @return the header row - might be {@code null} if no header exists
	 */
	public List<String> getHeader() {
		if(null == this.header){
			return null;
		}
		return Collections.unmodifiableList(this.header);
	}

	/**
	 * 获取指定行，从0开始
	 *
	 * @param index 行号
	 * @return 行数据
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public CsvRow getRow(final int index) {
		return this.rows.get(index);
	}

	/**
	 * 获取所有行
	 *
	 * @return 所有行
	 */
	public List<CsvRow> getRows() {
		return this.rows;
	}

	@Override
	public Iterator<CsvRow> iterator() {
		return this.rows.iterator();
	}

	@Override
	public String toString() {
		return "CsvData{" +
				"header=" + header +
				", rows=" + rows +
				'}';
	}
}
