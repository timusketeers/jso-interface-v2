package com.howbuy.jso.service.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.howbuy.jso.service.network.host.Host;

/**
 * 渠道重连任务.
 * @author li.zhang
 * 2014-9-23
 *
 */
public class ChannelReconnectTask implements Runnable
{
    /** hostSelector **/
    private final HostSelector hostSelector;
    
    /** selector **/
    private final Selector selector;
    
    /**
     * 构造方法
     * @param hostSelector
     */
    public ChannelReconnectTask(HostSelector hostSelector)
    {
        this.hostSelector = hostSelector;
        this.selector = hostSelector.getSelector();
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                //每隔5秒尝试重新连接下之前断链的服务器.
                Thread.sleep(10000);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            
            Set<Host> hosts = hostSelector.getCancelledHosts();
            if (null == hosts || hosts.isEmpty())
            {
                continue;
            }
            
            Iterator<Host> iterator = hosts.iterator();
            while (iterator.hasNext())
            {
                Host host = iterator.next();
                System.out.println("Reconnect server:[" + host.getIp() + ":" + host.getPort() + "]");
                SocketChannel channel = null;
                try
                {
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    channel.connect(new InetSocketAddress(host.getIp(), host.getPort()));
                    
                    /**
                     * channel.register(selector, SelectionKey.OP_CONNECT)和selector.select()方法会有同步阻塞的问题。
                     * 
                     * 之所以会有同步阻塞的问题，是因为一个是在selector上注册channel，一个是从selector上读可用的channel。慎之慎之!
                     * 
                     * 所以，这里会有一句selector.wakeup()来解决这个问题.
                     */
                    selector.wakeup();
                    channel.register(selector, SelectionKey.OP_CONNECT);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
