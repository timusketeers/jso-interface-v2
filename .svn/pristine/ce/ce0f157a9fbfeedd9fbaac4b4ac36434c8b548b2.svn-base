package com.howbuy.jso.service.network.thread;

import java.nio.channels.SelectionKey;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.config.Config;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.queue.NioQueue;

/**
 * 心跳线程，用于维持链路.
 * @author li.zhang
 * 2014-9-22
 *
 */
public class HeartbeatThread extends Thread
{
    /** key **/
    private SelectionKey key;

    /**
     * 构造方法
     * @param key
     */
    public HeartbeatThread(SelectionKey key)
    {
        this.key = key;
    }

    public void run()
    {
        Config config = Config.getInstance();
        long heartBeatInterval = config.getHeartBeatSendInterval();
        
        Attachment attachment = (Attachment)key.attachment();
        NioQueue<CmdReq> queue = attachment.getHeartbeatReqQueue();//心跳消息队列.
        while (true)
        {
            CmdReq heartbeat = createHeartBeatStream();
            queue.offer(heartbeat);
            
            try
            {
                //每隔5秒发送一次心跳消息.
                Thread.sleep(heartBeatInterval);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 创建心跳请求流.
     * @return
     */
    private CmdReq createHeartBeatStream()
    {
        CmdReq heartbeat = new CmdReq();
        heartbeat.setCmd(CmdEnum.HEART_BEAT.getCommand());
        
        return heartbeat;
    }
}
