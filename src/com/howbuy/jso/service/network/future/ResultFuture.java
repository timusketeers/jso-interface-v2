package com.howbuy.jso.service.network.future;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.registry.LinkStatusRegistry;
import com.howbuy.network.entity.Result;

/**
 * 表示seqId对应的异步结果封装.
 * @author li.zhang
 * 2014-9-24
 *
 */
public class ResultFuture implements Future<Result>
{   
    /** key **/
    private final SelectionKey key;
    
    /** attachment **/
    private final Attachment attachment;
    
    /** seqId **/
    private final int seqId;
        
    /**
     * 构造方法
     * @param key
     * @param seqId
     */
    public ResultFuture(SelectionKey key, int seqId)
    {
        this.key = key;
        this.attachment = (Attachment)key.attachment();
        this.seqId = seqId;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public boolean isDone()
    {
        boolean done = false;
        if (null == attachment)
        {
            return done;
        }
        
        SocketChannel channel = (SocketChannel)key.channel();
        if (!channel.isConnected())
        {
            done = true;
        }
        else
        {
            //如果响应已经回来...
            if (null != attachment.fromRespQueue(seqId))
            {
                done = true;
            }
            else
            {
                //如果响应始终没有回来，则观察心跳消息,如果心跳响应消息也开始收不到了，则直接返回错误，指出并行计算服务器可能崩溃了...
                LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
                Boolean linkStatus = statusRegistry.getLinkStatus(key);
                if (null == linkStatus || !linkStatus)
                {
                    done = true;
                }
            }
        }
        
        return done;
    }

    @Override
    public Result get() throws InterruptedException, ExecutionException
    {
        //如果没有完成,这里将一直阻塞.
        while (!isDone())
        {
            /**
             * 如果这里没有让当前线程睡眠，cpu会一直阻塞在这里，等待执行完成才释放cpu，如果应用的执行时间比较长，
             * 
             * 不在这里添加睡眠的话，会导致cpu占用很高.因此，这里会有这个休眠的动作.
             */
            Thread.sleep(500);
        }
        
        Result result = null;
        
        SocketChannel channel = (SocketChannel)key.channel();
        
        //如果selectionKey已经失效.
        if (!key.isValid())
        {
            result = new ExceptionResultFuture("0999", "Selection key is not valid...").get();
            return result;
        }
        
        //如果断连..
        if (!channel.isConnected())
        {
            result = new ExceptionResultFuture("0999", "This server abruptly encounter network interruption.").get();
            return result;
        }
        
        //如果计算服务器健康状况是不健康,则...
        LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
        Boolean linkStatus = statusRegistry.getLinkStatus(key);
        if (null == linkStatus || !linkStatus)
        {
            result = new ExceptionResultFuture("0999", "link status is not normal, may be socket is broken or the calc server is shutdown,please check.").get();
            return result;
        }
        
        result = attachment.getResult(seqId);
        
        //将对应的响应从响应队列中删除,防止内存溢出.
        attachment.removeRspQueue(seqId);
        
        return result;
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException,
        ExecutionException, TimeoutException
    {
        Result result = null;
        
        long current = System.currentTimeMillis();
        long miseconds = unit.toMillis(timeout);
        
        //如果任务没有完成并且没有超时就一直阻塞在这里.
        boolean done = false;
        while (true)
        {
            if (isDone())
            {
                done = true;
                break;
            }
            
            if ((current + miseconds) < System.currentTimeMillis())
            {
                result = new ExceptionResultFuture("0999", "execute time out.").get();
                return result;
            }
        }
            
        if (done)
        {
            //如果计算服务器健康状况是不健康,则...
            LinkStatusRegistry statusRegistry = LinkStatusRegistry.getInstance();
            Boolean linkStatus = statusRegistry.getLinkStatus(key);
            if (null == linkStatus || !linkStatus)
            {
                result = new ExceptionResultFuture("0999", "link status is not normal, may be socket is broken or the calc server is shutdown,please check.").get();
                return result;
            }
            
            result = attachment.getResult(seqId);
        }
        
        //将对应的响应从响应队列中删除,防止内存溢出.
        attachment.removeRspQueue(seqId);
        
        return result;
    }
}
