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
package io.github.future0923.debug.tools.base.hutool.core.io.resource;

import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.URLUtil;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

/**
 * URL资源访问类
 * @author Looly
 *
 */
public class UrlResource implements Resource, Serializable{
	private static final long serialVersionUID = 1L;

	protected URL url;
	private long lastModified = 0;
	protected String name;

	//-------------------------------------------------------------------------------------- Constructor start
	/**
	 * 构造
	 * @param uri URI
	 * @since 5.7.21
	 */
	public UrlResource(URI uri) {
		this(URLUtil.url(uri), null);
	}

	/**
	 * 构造
	 * @param url URL
	 */
	public UrlResource(URL url) {
		this(url, null);
	}

	/**
	 * 构造
	 * @param url URL，允许为空
	 * @param name 资源名称
	 */
	public UrlResource(URL url, String name) {
		this.url = url;
		if(null != url && URLUtil.URL_PROTOCOL_FILE.equals(url.getProtocol())){
			this.lastModified = FileUtil.file(url).lastModified();
		}
		this.name = ObjectUtil.defaultIfNull(name, () -> (null != url ? FileUtil.getName(url.getPath()) : null));
	}

	/**
	 * 构造
	 * @param file 文件路径
	 * @deprecated Please use {@link FileResource}
	 */
	@Deprecated
	public UrlResource(File file) {
		this.url = URLUtil.getURL(file);
	}
	//-------------------------------------------------------------------------------------- Constructor end

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public URL getUrl(){
		return this.url;
	}

	@Override
	public InputStream getStream() throws NoResourceException {
		if(null == this.url){
			throw new NoResourceException("Resource URL is null!");
		}
		return URLUtil.getStream(url);
	}

	@Override
	public boolean isModified() {
		// lastModified == 0表示此资源非文件资源
		return (0 != this.lastModified) && this.lastModified != getFile().lastModified();
	}

	/**
	 * 获得File
	 * @return {@link File}
	 */
	public File getFile(){
		return FileUtil.file(this.url);
	}

	/**
	 * 返回路径
	 * @return 返回URL路径
	 */
	@Override
	public String toString() {
		return (null == this.url) ? "null" : this.url.toString();
	}

	/**
	 * 获取资源长度
	 *
	 * @return 资源长度
	 * @since 5.8.21
	 */
	public long size() {
		return URLUtil.size(this.url);
	}
}
