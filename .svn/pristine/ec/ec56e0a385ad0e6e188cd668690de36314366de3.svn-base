package com.howbuy.jso.service.network;

import java.nio.ByteBuffer;

/**
 * 包中数据的缓存.
 * @author li.zhang
 * 2015-1-14
 *
 */
public class PkgCache
{
    /** cache. **/
    private ByteBuffer cache = ByteBuffer.allocate(1024);
    
    /**
     * 将包的一部分数据放到缓存中.
     * @param packageLen packageLen
     */
    public void put2Cache(int packageLen)
    {
        cache.putInt(packageLen);
    }
    
    /**
     * 存入缓存中.
     * @param part 包的部分数据
     */
    public void put2Cache(byte[] part)
    {
        if (cache.remaining() < part.length)
        {
            ByteBuffer temp = ByteBuffer.allocate(cache.capacity() + part.length);
            byte[] packageData = new byte[cache.limit()];
            cache.get(packageData);
            
            temp.put(packageData);
            temp.put(part);
            
            cache = temp;
        }
        else
        {
            cache.put(part);
        }
    }

    /**
     * 存入缓存中.
     * @param part 包的部分数据
     */
    public void put2Cache(byte[] part, int index, int len)
    {
        if (cache.remaining() < len)
        {
            ByteBuffer temp = ByteBuffer.allocate(cache.capacity() + len);
            byte[] packageData = new byte[cache.limit()];
            cache.get(packageData);
            
            temp.put(packageData);
            temp.put(part, index, len);
            
            cache = temp;
        }
        else
        {
            cache.put(part, index, len);
        }
    }
    
    public ByteBuffer getCache()
    {
        return cache;
    }
}
