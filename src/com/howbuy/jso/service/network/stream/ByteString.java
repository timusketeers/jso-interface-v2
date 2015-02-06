package com.howbuy.jso.service.network.stream;

import org.apache.commons.lang.ArrayUtils;

public class ByteString
{
    int step = 128 * 1024;

    byte[] buffer = null;

    int curpos = 0;

    public ByteString(int len)
    {

        buffer = new byte[step];
        curpos = len;
    }

    @SuppressWarnings("unused")
    private int position()
    {
        return curpos;
    }

    private int capacity()
    {
        return buffer.length;
    }

    private void capacity(int newCapacity)
    {
        byte[] bytes = new byte[newCapacity];
        System.arraycopy(buffer, 0, bytes, 0, curpos);
        buffer = bytes;
    }

    private void expand(int pos, int expectedRemaining)
    {
        int end = pos + expectedRemaining;
        int newCapacity = end;

        if (newCapacity > capacity())
        {
            if (newCapacity < capacity() + step)
            {
                newCapacity = capacity() + step;
            }

            capacity(newCapacity);
        }
    }

    private void expand(int expectedRemaining)
    {
        expand(curpos, expectedRemaining);
    }

    public void append(char c)
    {
        append((byte)c);
    }

    public void append(byte b)
    {
        expand(1);
        buffer[curpos++] = b;
    }

    public void append(byte[] bytes)
    {
        expand(bytes.length);
        System.arraycopy(bytes, 0, buffer, curpos, bytes.length);
        curpos += bytes.length;
    }

    public void append(byte[] bytes, int len)
    {
        expand(len);
        System.arraycopy(bytes, 0, buffer, curpos, len);
        curpos += len;
    }

    public void append(String str, int len)
    {
        char[] chars = str.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++)
        {
            bytes[i] = (byte) chars[i];
        }
        append(bytes, len);
    }

    public void insert(int pos, byte b)
    {

        insert(pos, new byte[] { b });
    }

    public void insert(int pos, byte[] bytes)
    {

        insert(pos, bytes, bytes.length);
    }

    public void insert(int pos, char c)
    {

        insert(pos, (byte) c);
    }

    public void insert(int pos, byte[] bytes, int len)
    {
        if (pos <= curpos)
        {
            expand(curpos, len);
            byte[] tail = new byte[curpos - pos];
            System.arraycopy(buffer, pos, tail, 0, tail.length);
            System.arraycopy(bytes, 0, buffer, pos, len);
            System.arraycopy(tail, 0, buffer, pos + len, tail.length);

            curpos = pos + len + tail.length;
        }
    }

    public void insert(int pos, String str, int len)
    {

        char[] chars = str.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++)
        {
            bytes[i] = (byte) chars[i];
        }
        insert(pos, bytes, len);
    }

    public long length()
    {
        return curpos;
    }

    public byte[] getBytes()
    {
        return ArrayUtils.subarray(buffer, 0, curpos);
    }

    public byte[] getBytes(int from)
    {
        return ArrayUtils.subarray(buffer, from, curpos);
    }

    public byte getByte(int pos)
    {

        return buffer[pos];
    }

    public void clear()
    {

        buffer = new byte[step];
        curpos = 0;
    }

    public void put(int offset, byte[] bytes, int len)
    {

        if (offset + len > length() && bytes.length < len)
        {
            return;
        }
        System.arraycopy(bytes, 0, buffer, offset, len);
    }

    public void put(int offset, byte b)
    {

        if (offset < length())
        {
            buffer[offset] = b;
        }
    }

    public void flip()
    {

    }
}
