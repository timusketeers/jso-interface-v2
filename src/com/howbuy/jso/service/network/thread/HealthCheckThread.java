package com.howbuy.jso.service.network.thread;

import java.nio.channels.SelectionKey;

import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.config.Config;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.registry.LinkStatusRegistry;
import com.howbuy.jso.service.queue.NioQueue;

/**
 * 健康检查线程，用于检查服务健康状况.
 * @author li.zhang
 * 2014-9-22
 *
 */
public class HealthCheckThread extends Thread
{
    /** key **/
    private SelectionKey key;
    
    /** 连续异常次数. 如果连续5次都没有在心跳超时时间内接受到心跳消息，则报出异常. **/
    private int continuousExceptionNum;
    
    /**
     * 构造方法
     * @param key
     */
    public HealthCheckThread(SelectionKey key)
    {
        this.key = key;
    }

    public void run()
    {
        LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
        
        Config config = Config.getInstance();
        long continuousExceptNum = config.getContinuousExceptionNum();
        
        boolean isNormal = false;
        Attachment attachment = (Attachment)key.attachment();
        NioQueue<CmdReq> uncnfmHeartBeats = attachment.getUncnfmHeartBeats();//待确认心跳消息队列.
        while (true)
        {
            /**
             * 注意: 我们程序里面有一个潜在的问题，如果服务端始终没有响应，会导致客户端心跳包在队列中解压，长时间的话，可能出现堆空间不足的异常.
             */
            isNormal = checkHeartBeatStatus(uncnfmHeartBeats);
            if (!isNormal)
            {
                continuousExceptionNum++;
            }
            else
            {
                continuousExceptionNum = 0;
                
                //将服务器状态注册为健康的.
                statusRegistry.registryLinkStatus(key, true);
                
            }
            
            if (continuousExceptionNum >= continuousExceptNum)
            {
                //将服务器状态注册为不健康的.
                statusRegistry.registryLinkStatus(key, false);
            }
        }
    }
    
    /**
     * 检查心跳状况.
     * @return
     */
    public static boolean checkHeartBeatStatus(NioQueue<CmdReq> uncnfmHeartBeats)
    {
        boolean isNormal = true;
        
        //得到心跳超时时间
        Config config = Config.getInstance();
        long heartbeatTimout = config.getHeartBeatTimout();
        
        CmdReq preReq = null;
        if (!uncnfmHeartBeats.isEmpty())
        {
            preReq = uncnfmHeartBeats.peek();
        }
        
        CmdReq curReq = null;
        try
        {
            Thread.sleep(heartbeatTimout);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        if (!uncnfmHeartBeats.isEmpty())
        {
            curReq = uncnfmHeartBeats.peek();
        }
        
        //用这个把空指针的东东也包括了进去...
        if (!uncnfmHeartBeats.isEmpty() && curReq == preReq)
        {
            isNormal = false;
        }
        
        return isNormal;
    }
}
