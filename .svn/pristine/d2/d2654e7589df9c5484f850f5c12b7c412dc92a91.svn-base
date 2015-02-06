package com.howbuy.jso.service.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.host.Host;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.registry.LinkStatusRegistry;
import com.howbuy.jso.service.network.thread.HealthCheckThread;
import com.howbuy.jso.service.network.thread.HeartbeatThread;
import com.howbuy.jso.service.network.utils.Utils;
import com.howbuy.jso.service.queue.NioQueue;

/**
 * 渠道监控线程.
 * @author li.zhang
 * 2014-9-23
 *
 */
public class ChannelMonitor extends Thread
{
    /** logger **/
    private static Logger logger = Logger.getLogger(ChannelMonitor.class);
    
    /** 渠道选择器. **/
    private final Selector selector;
    
    /** 主机选择器. **/
    private final HostSelector hostSelector;
    
    /** 试连次数. **/
    private int tryConnectCount;
    
    /** key为selectionKey，value为pkg列表. **/
    private Map<SelectionKey, List<Pkg>> pkgs = new HashMap<SelectionKey, List<Pkg>>();
    
    /**
     * 构造方法
     * @param selector
     */
    public ChannelMonitor(HostSelector selector)
    {
        this.hostSelector = selector;
        this.selector = selector.getSelector();
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            try
            {   
                //解决cpu占用率高的问题.
                Thread.sleep(30);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
            int selectedCount = 0;
            
            try
            {
                selectedCount = selector.selectNow();
            }
            catch (IOException e)
            {
                continue;
            }
            
            //非阻塞.
            if (0 >= selectedCount)
            {
                continue;
            }
            
            try
            {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext())
                {
                    SelectionKey key = iterator.next();

                    //连接就绪
                    if (key.isValid() && key.isConnectable())
                    {
                        doConnect(key);
                    }

                    //渠道读就绪.
                    if (key.isValid() && key.isReadable())
                    {
                        doRead(key);
                    }

                    //渠道写就绪.
                    if (key.isValid() && key.isWritable())
                    {
                        doWrite(key);
                    }
                }
                
                selectedKeys.clear();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //连接就绪
    private void doConnect(SelectionKey key)
    {
        System.out.println("doConnect...");
        SocketChannel channel = (SocketChannel)key.channel();
        
        try
        {
            boolean connected = channel.finishConnect();
            if (connected)
            {
                //重连成功后, 从撤销列表中删除.
                removeFromCancelledSet(key);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            
            //注册链路状态为不正常.
            LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
            statusRegistry.registryLinkStatus(key, false);
            
            closeChannel(channel);
            return;
        }
        
        //关联附件
        key.attach(new Attachment());
        
        //启动心跳服务线程.
        HeartbeatThread hearbeat = new HeartbeatThread(key);
        hearbeat.start();
        
        //健康检查线程.
        HealthCheckThread healthcheckThread = new HealthCheckThread(key);
        healthcheckThread.start();
        
        //如果试连次数大于或者等于主机个数,则说明链路初始化已经完成.
        ++tryConnectCount;
        
        //一个渠道channel和一个选择器selector只会有一个注册关系,即只有一个selectionKey.
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);//重新设置注册关系中感兴趣的就绪事件.
    }

    //渠道读就绪
    private void doRead(SelectionKey key)
    {
        try
        {
            System.out.println("doRead...");
            Attachment attachment = (Attachment)key.attachment();
            
            Pkg pkg = null;
            List<Pkg> pkgList = pkgs.get(key);
            if (null == pkgList || pkgList.isEmpty())
            {
                pkgList = new ArrayList<Pkg>();
                pkg = new Pkg();
                pkgList.add(pkg);
                pkgs.put(key, pkgList);
                
                readPkg(key, pkg, true);
            }
            else
            {
                pkg = pkgList.get(0);
                readPkg(key, pkg, false);
            }
            
            Pkg curPkg = pkg;
            if (curPkg.getRemainLen() > 0)
            {
                return;
            }
            
            //得到整个包的数据.
            byte[] pkgData = curPkg.receivedData();
            
            //删除
            pkgs.get(key).remove(curPkg);
            
            //打印客户端发来的请求流..
            printStream(pkgData);
            
            CmdResp resp = CmdResp.fromByteStream(pkgData);
            if (null == resp)
            {
                return;
            }
            
            switch (CmdEnum.forCommand(resp.getCmd()))
            {
                case HEART_BEAT:
                    //System.out.println("成功收到心跳响应from:[" + channel.socket().getPort() + "]");
                    onHeartBeatRsp(resp, key);
                    
                    break;
                    
                case CLEAR_ACK:
                    System.out.println("成功收到clearAck响应:[" + resp.toString() + "]");
                    logger.info("成功收到clearAck响应:[" + resp.toString() + "]");
                    onClearAckRsp(resp, attachment);
                    break;
                    
                case CLEAR_DIS_ACK:
                    System.out.println("成功收到clearDisAck响应:[" + resp.toString() + "]");
                    logger.info("成功收到clearDisAck响应:[" + resp.toString() + "]");
                    onClearDisAckRsp(resp, attachment);
                    break;
                    
                case CLEAR_DIS_ACK_DTL:
                    System.out.println("成功收到clearDisAckDtl响应:[" + resp.toString() + "]");
                    logger.info("成功收到clearDisAckDtl响应:[" + resp.toString() + "]");
                    onClearAckDtlRsp(resp, attachment);
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 读取一个完整的包，如果只有一部分包中数据，则阻塞等待一个完整的包数据到来.
     * @param key key
     * @param pkg pkg
     * @param firstPkg 表示是不是完整包的第一部分.
     * @return
     */
    private void readPkg(SelectionKey key, Pkg pkg, boolean firstPkg)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        
        try
        {
            while (channel.read(buffer) > 0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            removeFromSelectedSet(key);
            return;
        }
        
        buffer.flip();
        byte[] pkgPartData = new byte[buffer.limit()];
        buffer.get(pkgPartData);
        
        //客户端可能一下子接收到服务端发送过来的多个包.所以下面这么处理.
        ByteBuffer wrapper = ByteBuffer.wrap(pkgPartData);
        if (firstPkg)
        {
            int packageLen = wrapper.getInt();
            int remainLen = packageLen - Integer.SIZE / 8;
            pkg.setPackageLen(packageLen);
            pkg.setRemainLen(remainLen);
            
            pkg.put2Cache(packageLen);
        }
        
        //如果接收到的数据长度大于包剩余的长度.
        Pkg curPkg = pkg;
        Pkg antherPkg = null;
        while (wrapper.position() != pkgPartData.length)
        {
            int pkgRemainLen = curPkg.getRemainLen();
            
            if (0 == pkgRemainLen)
            {
                antherPkg = new Pkg();
                int anotherPkgLen = wrapper.getInt();
                int anotherPkgRemainLen = anotherPkgLen - Integer.SIZE / 8;
                antherPkg.setPackageLen(anotherPkgLen);
                antherPkg.setRemainLen(anotherPkgRemainLen);
                
                antherPkg.put2Cache(anotherPkgLen);
                
                pkgs.get(key).add(antherPkg);
                
                curPkg = antherPkg;
                continue;
            }
            
            if ((pkgPartData.length - wrapper.position()) > pkgRemainLen)
            {
                curPkg.put2Cache(pkgPartData, wrapper.position(), pkgRemainLen);
                curPkg.decreaseRemainLen(pkgRemainLen);
                
                wrapper.position(wrapper.position() + pkgRemainLen);
                
                antherPkg = new Pkg();
                int anotherPkgLen = wrapper.getInt();
                int anotherPkgRemainLen = anotherPkgLen - Integer.SIZE / 8;
                antherPkg.setPackageLen(anotherPkgLen);
                antherPkg.setRemainLen(anotherPkgRemainLen);
                
                antherPkg.put2Cache(anotherPkgLen);
                
                pkgs.get(key).add(antherPkg);
                
                curPkg = antherPkg;
            }
            else
            {
                int len = wrapper.limit() - wrapper.position();
                curPkg.put2Cache(pkgPartData, wrapper.position(), len);
                curPkg.decreaseRemainLen(len);
                
                wrapper.position(wrapper.position() + len);
            }
        }
    }

    //渠道写就绪.
    private void doWrite(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        Attachment attachment = (Attachment)key.attachment();
        NioQueue<CmdReq> heartbeatQueue = attachment.getHeartbeatReqQueue();
        NioQueue<CmdReq> uncnfmHeartBeats = attachment.getUncnfmHeartBeats();
        NioQueue<CmdReq> reqQueue = attachment.getReqQueue();
        
        if (reqQueue.isEmpty() && heartbeatQueue.isEmpty())
        {
            return;
        }
        
        try
        {
            if (!heartbeatQueue.isEmpty())
            {
                CmdReq req = heartbeatQueue.poll();
                uncnfmHeartBeats.offer(req);//将该心跳消息放到心跳待确认列表中..
                logger.info("Dispatch heartbeat command, seqId is " + req.getSeqId() + ".");
                channel.write(ByteBuffer.wrap(req.toByteStream()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            removeFromSelectedSet(key);
        }
            
        try
        {
            if (!reqQueue.isEmpty())
            {
                CmdReq req = reqQueue.poll();
                logger.info("Dispatch CmdReq:[" + req.toString() + "]");
                
                channel.write(ByteBuffer.wrap(req.toByteStream()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            removeFromSelectedSet(key);
        }
    }
    
    private void removeFromCancelledSet(SelectionKey key)
    {
        LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        hostSelector.removeFromCancelledHost(hostinfo);
        hostSelector.addSelectedHost(hostinfo);    
        
        //在注册列表中注册连接连接.
        hostSelector.getRegistry().put(ip + "_" + host.getPort(), key);
        
        //注册链路状态为正常.
        statusRegistry.registryLinkStatus(key, true);
    }

    private void removeFromSelectedSet(SelectionKey key)
    {
        LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
        
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        hostSelector.removeSelectedHost(hostinfo);
        hostSelector.addCancelledHost(hostinfo);
        
        //从注册表中删除服务主机host对应的客户端连接.
        hostSelector.getRegistry().remove(ip + "_" + port);
        
        //注册链路状态为不正常.
        statusRegistry.registryLinkStatus(key, false);
        
        closeChannel(channel);
    }
    
    private void closeChannel(SocketChannel channel)
    {
        try
        {
            channel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 心跳响应处理逻辑.
     * @param key
     */
    private void onHeartBeatRsp(CmdResp resp, SelectionKey key)
    {
        Attachment attachment = (Attachment)key.attachment();
        
        //删除心跳待确认队列中已经被确认的心跳包.
        NioQueue<CmdReq> uncnfmHeartBeats = attachment.getUncnfmHeartBeats();
        CmdReq req = Utils.correspondingCmdReq(uncnfmHeartBeats, resp.getSeqId());
        uncnfmHeartBeats.remove(req);
        
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        if (channel.isConnected())
        {
            hostSelector.addSelectedHost(hostinfo);
            hostSelector.removeFromCancelledHost(hostinfo);
        }
        else
        {
            hostSelector.removeSelectedHost(hostinfo);
            hostSelector.addCancelledHost(hostinfo);
        }
        
        //如果试连次数大于或者等于主机个数,则说明链路初始化已经完成.
        if (tryConnectCount >= hostSelector.getHostCount())
        {
            //此时每个服务器都有一个连接到该服务器的客户端连接,如果服务器可达.
            hostSelector.setChannelInitEnd(true);
        }
    }
    
    private void onClearAckRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();
        respQueue.offer(resp);
    }

    private void onClearDisAckRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();
        respQueue.offer(resp);
    }

    private void onClearAckDtlRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();    
        respQueue.offer(resp);
    }
    
    private void printStream(byte[] data)
    {
        StringBuilder builder = new StringBuilder();
        builder.delete(0, builder.length());
        builder.append("[");
        for (int i = 0; i < data.length; i++)
        {
            builder.append(data[i]).append(" ");
        }
        builder.append("]");
        
        System.out.println("[Server]:"+ builder.toString());
    }
}
