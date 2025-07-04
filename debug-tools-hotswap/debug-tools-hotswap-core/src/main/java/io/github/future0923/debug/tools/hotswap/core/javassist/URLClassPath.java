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
package io.github.future0923.debug.tools.hotswap.core.javassist;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A class search-path specified with URL (http).
 *
 * @see javassist.ClassPath
 * @see ClassPool#insertClassPath(ClassPath)
 * @see ClassPool#appendClassPath(ClassPath)
 */
public class URLClassPath implements ClassPath {
    protected String hostname;
    protected int port;
    protected String directory;
    protected String packageName;

    /**
     * Creates a search path specified with URL (http).
     *
     * <p>This search path is used only if a requested
     * class name starts with the name specified by <code>packageName</code>.
     * If <code>packageName</code> is "org.javassist." and a requested class is
     * "org.javassist.test.Main", then the given URL is used for loading that class.
     * The <code>URLClassPath</code> obtains a class file from:
     *
     * <pre>http://www.javassist.org:80/java/classes/org/javassist/test/Main.class
     * </pre>
     *
     * <p>Here, we assume that <code>host</code> is "www.javassist.org",
     * <code>port</code> is 80, and <code>directory</code> is "/java/classes/".
     *
     * <p>If <code>packageName</code> is <code>null</code>, the URL is used
     * for loading any class.
     *
     * @param host              host name
     * @param port              port number
     * @param directory         directory name ending with "/".
     *                          It can be "/" (root directory).
     *                          It must start with "/".
     * @param packageName       package name.  It must end with "." (dot).
     */
    public URLClassPath(String host, int port,
                        String directory, String packageName) {
        hostname = host;
        this.port = port;
        this.directory = directory;
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return hostname + ":" + port + directory;
    }

    /**
     * Opens a class file with http.
     *
     * @return null if the class file could not be found. 
     */
    @Override
    public InputStream openClassfile(String classname) {
        try {
            URLConnection con = openClassfile0(classname);
            if (con != null)
                return con.getInputStream();
        }
        catch (IOException e) {}
        return null;        // not found
    }

    private URLConnection openClassfile0(String classname) throws IOException {
        if (packageName == null || classname.startsWith(packageName)) {
            String jarname
                    = directory + classname.replace('.', '/') + ".class";
            return fetchClass0(hostname, port, jarname);
        }
        return null;    // not found
    }

    /**
     * Returns the URL.
     *
     * @return null if the class file could not be obtained. 
     */
    @Override
    public URL find(String classname) {
        try {
            URLConnection con = openClassfile0(classname);
            InputStream is = con.getInputStream();
            if (is != null) {
                is.close();
                return con.getURL();
            }
        }
        catch (IOException e) {}
        return null; 
    }

    /**
     * Reads a class file on an http server.
     *
     * @param host              host name
     * @param port              port number
     * @param directory         directory name ending with "/".
     *                          It can be "/" (root directory).
     *                          It must start with "/".
     * @param classname         fully-qualified class name
     */
    public static byte[] fetchClass(String host, int port,
                                    String directory, String classname)
        throws IOException
    {
        byte[] b;
        URLConnection con = fetchClass0(host, port,
                directory + classname.replace('.', '/') + ".class");
        int size = con.getContentLength();
        InputStream s = con.getInputStream();
        try {
            if (size <= 0)
                b = ClassPoolTail.readStream(s);
            else {
                b = new byte[size];
                int len = 0;
                do {
                    int n = s.read(b, len, size - len);
                    if (n < 0)
                        throw new IOException("the stream was closed: "
                                              + classname);

                    len += n;
                } while (len < size);
            }
        }
        finally {
            s.close();
        }

        return b;
    }

    private static URLConnection fetchClass0(String host, int port,
                                             String filename)
        throws IOException
    {
        URL url;
        try {
            url = new URL("http", host, port, filename);
        }
        catch (MalformedURLException e) {
            // should never reache here.
            throw new IOException("invalid URL?");
        }

        URLConnection con = url.openConnection();
        con.connect();
        return con;
    }
}
