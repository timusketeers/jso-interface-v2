package com.howbuy.jso.service.network;

import java.nio.ByteBuffer;

/**
 * 表示一个网络包.
 * @author li.zhang
 * 2015-1-14
 *
 */
public class Pkg
{
    /** 包的长度. **/
    private int packageLen;
    
    /** 表示该包剩余多少个字节的数据还没有读到. **/
    private int remainLen;
    
    /** 包数据缓存. **/
    private PkgCache cache = new PkgCache();

    /**
     * 将包的一部分数据放到缓存中.
     * @param packageLen packageLen
     */
    public void put2Cache(int packageLen)
    {
        cache.put2Cache(packageLen);
    }
    
    /**
     * 将包的一部分数据放到缓存中.
     * @param part 包的一部分数据
     */
    public void put2Cache(byte[] part)
    {
        cache.put2Cache(part);
    }
    
    /**
     * 将包的一部分数据放到缓存中.
     * @param part 包的一部分数据
     */
    public void put2Cache(byte[] part, int index, int len)
    {
        cache.put2Cache(part, index, len);
    }
    
    /**
     * decreaseRemainLen
     * @param length
     */
    public void decreaseRemainLen(int length)
    {
        remainLen = remainLen - length;
    }
    
    /**
     * 已经接收到的数据内容.
     */
    public byte[] receivedData()
    {
        ByteBuffer buffer = cache.getCache();
        
        buffer.flip();
        
        byte[] content = new byte[buffer.limit()];
        buffer.get(content);
        
        return content;
    }
    
    public int getPackageLen()
    {
        return packageLen;
    }

    public void setPackageLen(int packageLen)
    {
        this.packageLen = packageLen;
    }

    public int getRemainLen()
    {
        return remainLen;
    }

    public void setRemainLen(int remainLen)
    {
        this.remainLen = remainLen;
    }

    public PkgCache getCache()
    {
        return cache;
    }
}
