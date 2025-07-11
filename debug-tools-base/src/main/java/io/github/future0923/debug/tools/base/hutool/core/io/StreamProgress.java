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
package io.github.future0923.debug.tools.base.hutool.core.io;

/**
 * Stream进度条<br>
 * 提供流拷贝进度监测，如开始、结束触发，以及进度回调。<br>
 * 注意进度回调的{@code total}参数为总大小，某些场景下无总大小的标记，则此值应为-1或者{@link Long#MAX_VALUE}，表示此参数无效。
 *
 * @author Looly
 */
public interface StreamProgress {

	/**
	 * 开始
	 */
	void start();

	/**
	 * 进行中
	 *
	 * @param total        总大小，如果未知为 -1或者{@link Long#MAX_VALUE}
	 * @param progressSize 已经进行的大小
	 */
	void progress(long total, long progressSize);

	/**
	 * 结束
	 */
	void finish();
}
