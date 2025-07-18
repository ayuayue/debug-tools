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
package io.github.future0923.debug.tools.base.hutool.http.server.filter;

import io.github.future0923.debug.tools.base.hutool.core.exceptions.ExceptionUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;
import io.github.future0923.debug.tools.base.hutool.http.server.HttpServerRequest;
import io.github.future0923.debug.tools.base.hutool.http.server.HttpServerResponse;

/**
 * 默认异常处理拦截器
 *
 * @author looly
 */
public class DefaultExceptionFilter extends ExceptionFilter {

	private final static String TEMPLATE_ERROR = "<!DOCTYPE html><html><head><title>Hutool - Error report</title><style>h1,h3 {color:white; background-color: gray;}</style></head><body><h1>HTTP Status {} - {}</h1><hr size=\"1\" noshade=\"noshade\" /><p>{}</p><hr size=\"1\" noshade=\"noshade\" /><h3>Hutool</h3></body></html>";

	@Override
	public void afterException(final HttpServerRequest req, final HttpServerResponse res, final Throwable e) {
		String content = ExceptionUtil.stacktraceToString(e);
		content = content.replace("\n", "<br/>\n");
		content = StrUtil.format(TEMPLATE_ERROR, 500, req.getURI(), content);

		res.sendError(500, content);
	}
}
