package com.howbuy.jso.service.network;

/**
 * 服务名枚举值.
 * @author li.zhang
 * 2015-1-27
 *
 */
public enum Services
{
    /** 计算服务. **/
    CALC_SERVIICE("calc_service");
    
    private String str;
    
    private Services(String str)
    {
        this.str = str;
    }
    
    public String getName()
    {
        return str;
    }
}
