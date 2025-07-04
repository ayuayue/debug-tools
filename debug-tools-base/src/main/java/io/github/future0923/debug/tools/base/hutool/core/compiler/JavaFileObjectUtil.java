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
package io.github.future0923.debug.tools.base.hutool.core.compiler;

import io.github.future0923.debug.tools.base.hutool.core.io.file.FileNameUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ZipUtil;

import javax.tools.JavaFileObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * {@link JavaFileObject} 相关工具类封装
 *
 * @author lzpeng, looly
 * @since 5.5.2
 */
public class JavaFileObjectUtil {

	/**
	 * 获取指定文件下的所有待编译的java文件，并以{@link JavaFileObject}形式返回
	 *
	 * @param file 文件或目录，文件支持.java、.jar和.zip文件
	 * @return 所有待编译的 {@link JavaFileObject}
	 */
	public static List<JavaFileObject> getJavaFileObjects(File file) {
		final List<JavaFileObject> result = new ArrayList<>();
		final String fileName = file.getName();

		if (isJavaFile(fileName)) {
			result.add(new JavaSourceFileObject(file.toURI()));
		} else if (isJarOrZipFile(fileName)) {
			result.addAll(getJavaFileObjectByZipOrJarFile(file));
		}
		return result;
	}

	/**
	 * 是否是jar 或 zip 文件
	 *
	 * @param fileName 文件名
	 * @return 是否是jar 或 zip 文件
	 */
	public static boolean isJarOrZipFile(String fileName) {
		return FileNameUtil.isType(fileName, "jar", "zip");
	}

	/**
	 * 是否是java文件
	 *
	 * @param fileName 文件名
	 * @return 是否是.java文件
	 */
	public static boolean isJavaFile(String fileName) {
		return FileNameUtil.isType(fileName, "java");
	}

	/**
	 * 通过zip包或jar包创建Java文件对象
	 *
	 * @param file 压缩文件
	 * @return Java文件对象
	 */
	private static List<JavaFileObject> getJavaFileObjectByZipOrJarFile(File file) {
		final List<JavaFileObject> collection = new ArrayList<>();
		final ZipFile zipFile = ZipUtil.toZipFile(file, null);
		ZipUtil.read(zipFile, (zipEntry) -> {
			final String name = zipEntry.getName();
			if (isJavaFile(name)) {
				collection.add(new JavaSourceFileObject(name, ZipUtil.getStream(zipFile, zipEntry)));
			}
		});
		return collection;
	}
}
