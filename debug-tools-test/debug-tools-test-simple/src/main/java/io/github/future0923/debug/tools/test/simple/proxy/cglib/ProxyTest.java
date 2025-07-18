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
package io.github.future0923.debug.tools.test.simple.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author future0923
 */
public class ProxyTest {

    public static void main(String[] args) throws InterruptedException {
        // 创建 Enhancer 对象
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserService.class);
        enhancer.setCallback(new UserServiceInterceptor());
        UserService proxy = (UserService) enhancer.create();
        while (true) {
            Thread.sleep(1000);
            // 调用代理对象的方法
            proxy.addUser("DebugTools");
        }
    }
}
