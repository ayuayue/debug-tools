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

import io.github.future0923.debug.tools.base.hutool.core.io.FastByteArrayOutputStream;
import io.github.future0923.debug.tools.base.hutool.core.io.StreamProgress;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 下载封装，下载统一使用{@code GET}请求，默认支持30x跳转
 *
 * @author looly
 * @since 5.6.4
 */
public class HttpDownloader {

	/**
	 * 下载远程文本
	 *
	 * @param url           请求的url
	 * @param customCharset 自定义的字符集，可以使用{@code CharsetUtil#charset} 方法转换
	 * @param streamPress   进度条 {@link StreamProgress}
	 * @return 文本
	 */
	public static String downloadString(String url, Charset customCharset, StreamProgress streamPress) {
		final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
		download(url, out, true, streamPress);
		return null == customCharset ? out.toString() : out.toString(customCharset);
	}

	/**
	 * 下载远程文件数据，支持30x跳转
	 *
	 * @param url 请求的url
	 * @return 文件数据
	 */
	public static byte[] downloadBytes(String url) {
		return downloadBytes(url, 0);
	}

	/**
	 * 下载远程文件数据，支持30x跳转
	 *
	 * @param url     请求的url
	 * @param timeout 超时毫秒数
	 * @return 文件数据
	 * @since 5.8.28
	 */
	public static byte[] downloadBytes(String url, int timeout) {
		return requestDownload(url, timeout).bodyBytes();
	}

	/**
	 * 下载远程文件
	 *
	 * @param url             请求的url
	 * @param targetFileOrDir 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
	 * @param timeout         超时，单位毫秒，-1表示默认超时
	 * @param streamProgress  进度条
	 * @return 文件大小
	 */
	public static long downloadFile(String url, File targetFileOrDir, int timeout, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] is null !");

		return requestDownload(url, timeout).writeBody(targetFileOrDir, streamProgress);
	}

	/**
	 * 下载文件-避免未完成的文件<br>
	 * 来自：https://gitee.com/chinabugotech/hutool/pulls/407<br>
	 * 此方法原理是先在目标文件同级目录下创建临时文件，下载之，等下载完毕后重命名，避免因下载错误导致的文件不完整。
	 *
	 * @param url             请求的url
	 * @param targetFileOrDir 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
	 * @param tempFileSuffix  临时文件后缀，默认".temp"
	 * @param timeout         超时，单位毫秒，-1表示默认超时
	 * @param streamProgress  进度条
	 * @return 下载大小
	 * @since 5.7.12
	 */
	public static long downloadFile(String url, File targetFileOrDir, String tempFileSuffix, int timeout, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] is null !");

		return requestDownload(url, timeout).writeBody(targetFileOrDir, tempFileSuffix, streamProgress);
	}

	/**
	 * 下载远程文件，返回文件
	 *
	 * @param url             请求的url
	 * @param targetFileOrDir 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
	 * @param timeout         超时，单位毫秒，-1表示默认超时
	 * @param streamProgress  进度条
	 * @return 文件
	 */
	public static File downloadForFile(String url, File targetFileOrDir, int timeout, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] is null !");

		// writeBody后会自动关闭网络流
		return requestDownload(url, timeout).writeBodyForFile(targetFileOrDir, streamProgress);
	}

	/**
	 * 下载远程文件
	 *
	 * @param url            请求的url
	 * @param out            将下载内容写到输出流中 {@link OutputStream}
	 * @param isCloseOut     是否关闭输出流
	 * @param streamProgress 进度条
	 * @return 文件大小
	 */
	public static long download(String url, OutputStream out, boolean isCloseOut, StreamProgress streamProgress) {
		Assert.notNull(out, "[out] is null !");

		// writeBody后会自动关闭网络流
		return requestDownload(url, -1).writeBody(out, isCloseOut, streamProgress);
	}

	/**
	 * 请求下载文件
	 *
	 * @param url     请求下载文件地址
	 * @param timeout 超时时间
	 * @return HttpResponse
	 * @since 5.4.1
	 */
	private static HttpResponse requestDownload(String url, int timeout) {
		Assert.notBlank(url, "[url] is blank !");

		final HttpRequest request = HttpUtil.createGet(url, true);
		if (timeout > 0) {
			// 只有用户自定义了超时时长才有效，否则使用全局默认的超时时长。
			request.timeout(timeout);
		}


		final HttpResponse response = request.executeAsync();
		if (response.isOk()) {
			return response;
		}

		throw new HttpException("Server response error with status code: [{}]", response.getStatus());
	}
}
