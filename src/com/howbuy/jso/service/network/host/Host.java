package com.howbuy.jso.service.network.host;

/**
 * 应用节点.
 * @author li.zhang
 * 2014-9-17
 *
 */
public class Host
{
    /** 在哪台主机上部署. **/
    private String ip;
    
    /** 部署的端口. **/
    private int port;

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
    
    public boolean equals(Object obj)
    {
        boolean flag = false;
        if (null == obj)
        {
            return flag;
        }
        
        if (!Host.class.isInstance(obj))
        {
            return flag;
        }
        
        Host other = (Host)obj;
        if (other.ip.equals(this.ip) && other.port == this.port)
        {
            flag = true;
        }
        
        return flag;
    }
    
    public int hashCode()
    {
        int hashcode = ip.hashCode() << 2 + port;
        return hashcode;
    }
}
