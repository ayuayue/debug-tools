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
package io.github.future0923.debug.tools.hotswap.core.javassist.bytecode;

import java.io.IOException;
import java.io.OutputStream;

final class ByteStream extends OutputStream {
    private byte[] buf;
    private int count;

    public ByteStream() { this(32); }

    public ByteStream(int size) {
        buf = new byte[size];
        count = 0;
    }

    public int getPos() { return count; }
    public int size() { return count; }

    public void writeBlank(int len) {
        enlarge(len);
        count += len;
    }

    @Override
    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    @Override
    public void write(byte[] data, int off, int len) {
        enlarge(len);
        System.arraycopy(data, off, buf, count, len);
        count += len;
    }

    @Override
    public void write(int b) {
        enlarge(1);
        int oldCount = count;
        buf[oldCount] = (byte)b;
        count = oldCount + 1;
    }

    public void writeShort(int s) {
        enlarge(2);
        int oldCount = count;
        buf[oldCount] = (byte)(s >>> 8);
        buf[oldCount + 1] = (byte)s;
        count = oldCount + 2;
    }

    public void writeInt(int i) {
        enlarge(4);
        int oldCount = count;
        buf[oldCount] = (byte)(i >>> 24);
        buf[oldCount + 1] = (byte)(i >>> 16);
        buf[oldCount + 2] = (byte)(i >>> 8);
        buf[oldCount + 3] = (byte)i;
        count = oldCount + 4;
    }

    public void writeLong(long i) {
        enlarge(8);
        int oldCount = count;
        buf[oldCount] = (byte)(i >>> 56);
        buf[oldCount + 1] = (byte)(i >>> 48);
        buf[oldCount + 2] = (byte)(i >>> 40);
        buf[oldCount + 3] = (byte)(i >>> 32);
        buf[oldCount + 4] = (byte)(i >>> 24);
        buf[oldCount + 5] = (byte)(i >>> 16);
        buf[oldCount + 6] = (byte)(i >>> 8);
        buf[oldCount + 7] = (byte)i;
        count = oldCount + 8;
    }

    public void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeUTF(String s) {
        int sLen = s.length();
        int pos = count;
        enlarge(sLen + 2);

        byte[] buffer = buf;
        buffer[pos++] = (byte)(sLen >>> 8);
        buffer[pos++] = (byte)sLen;
        for (int i = 0; i < sLen; ++i) {
            char c = s.charAt(i);
            if (0x01 <= c && c <= 0x7f)
                buffer[pos++] = (byte)c;
            else {
                writeUTF2(s, sLen, i);
                return;
            }
        }

        count = pos;
    }

    private void writeUTF2(String s, int sLen, int offset) {
        int size = sLen;
        for (int i = offset; i < sLen; i++) {
            int c = s.charAt(i);
            if (c > 0x7ff)
                size += 2;  // 3 bytes code
            else if (c == 0 || c > 0x7f)
                ++size;     // 2 bytes code
        }

        if (size > 65535)
            throw new RuntimeException(
                    "encoded string too long: " + sLen + size + " bytes");

        enlarge(size + 2);
        int pos = count;
        byte[] buffer = buf;
        buffer[pos] = (byte)(size >>> 8);
        buffer[pos + 1] = (byte)size;
        pos += 2 + offset;
        for (int j = offset; j < sLen; ++j) {
            int c = s.charAt(j);
            if (0x01 <= c && c <= 0x7f)
                buffer[pos++] = (byte) c;
            else if (c > 0x07ff) {
                buffer[pos] = (byte)(0xe0 | ((c >> 12) & 0x0f));
                buffer[pos + 1] = (byte)(0x80 | ((c >> 6) & 0x3f));
                buffer[pos + 2] = (byte)(0x80 | (c & 0x3f));
                pos += 3;
            }
            else {
                buffer[pos] = (byte)(0xc0 | ((c >> 6) & 0x1f));
                buffer[pos + 1] = (byte)(0x80 | (c & 0x3f));
                pos += 2;
            }
        }

        count = pos;
    }

    public void write(int pos, int value) {
        buf[pos] = (byte)value;
    }

    public void writeShort(int pos, int value) {
        buf[pos] = (byte)(value >>> 8);
        buf[pos + 1] = (byte)value;
    }

    public void writeInt(int pos, int value) {
        buf[pos] = (byte)(value >>> 24);
        buf[pos + 1] = (byte)(value >>> 16);
        buf[pos + 2] = (byte)(value >>> 8);
        buf[pos + 3] = (byte)value;
    }

    public byte[] toByteArray() {
        byte[] buf2 = new byte[count];
        System.arraycopy(buf, 0, buf2, 0, count);
        return buf2;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public void enlarge(int delta) {
        int newCount = count + delta;
        if (newCount > buf.length) {
            int newLen = buf.length << 1;
            byte[] newBuf = new byte[newLen > newCount ? newLen : newCount];
            System.arraycopy(buf, 0, newBuf, 0, count);
            buf = newBuf;
        }
    }
}
