package com.howbuy.jso.service.network.registry;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务状态注册表,以此来反应服务是健康的还是不健康的.
 * @author li.zhang
 * 2015-1-27
 */
public class LinkStatusRegistry
{
    /** 实例. **/
    private static final LinkStatusRegistry INSTANCE = new LinkStatusRegistry();
    
    /** key为一个链路令牌关系,value为对应的服务的健康状况. **/
    private Map<SelectionKey, Boolean> linkStatuses = new HashMap<SelectionKey, Boolean>();

    /**
     * 私有构造方法
     */
    private LinkStatusRegistry()
    {
        
    }
    
    /**
     * 获得实例.
     * @return
     */
    public static LinkStatusRegistry getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * 注册链路状态
     * @param selectionKey 链路令牌关系
     * @param status 健康状况
     */
    public void registryLinkStatus(SelectionKey selectionKey, boolean status)
    {
        Map<SelectionKey, Boolean> statuses = new HashMap<SelectionKey, Boolean>();
        statuses.putAll(linkStatuses);
        statuses.put(selectionKey, status);
        
        linkStatuses = statuses;
    }
    
    /**
     * 根据selectionKey得到对应的服务健康状态.
     * @param selectionKey 链路令牌关系
     * @return
     */
    public Boolean getLinkStatus(SelectionKey selectionKey)
    {
        Boolean status = null;
        Map<SelectionKey, Boolean> statuses = new HashMap<SelectionKey, Boolean>();
        statuses.putAll(linkStatuses);
        
        status = statuses.get(selectionKey);
        return status;
    }
}
