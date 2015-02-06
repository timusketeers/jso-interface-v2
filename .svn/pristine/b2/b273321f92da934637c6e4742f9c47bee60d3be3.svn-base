package com.howbuy.jso.service.network.attachment;

import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.utils.Utils;
import com.howbuy.jso.service.queue.NioQueue;
import com.howbuy.network.entity.Result;

/**
 * 附件
 * @author li.zhang
 * 2014-9-24
 *
 */
public class Attachment
{
    /** 心跳请求队列. **/
    private NioQueue<CmdReq> heartbeatReqQueue = new NioQueue<CmdReq>();
    
    /** 响应还没有返回的心跳列表. **/
    private NioQueue<CmdReq> uncnfmHeartBeats = new NioQueue<CmdReq>();
    
    /** 业务请求指令队列. **/
    private NioQueue<CmdReq> reqQueue = new NioQueue<CmdReq>();
    
    /** 业务指令响应队列. **/
    private NioQueue<CmdResp> respQueue = new NioQueue<CmdResp>();
    
    /**
     * 根据seqId从队列中取出队列的response.
     * @param seqId
     * @return
     */
    public CmdResp fromRespQueue(int seqId)
    {
        CmdResp resp = null;
        if (null == respQueue || respQueue.isEmpty())
        {
            return resp;
        }
        
        for (int i = 0; i < respQueue.getQueueSize(); i++)
        {
            CmdResp item = respQueue.get(i);
            if (seqId == item.getSeqId())
            {
                resp = item;
                break;
            }
        }
        
        return resp;
    }
    
    public Result getResult(int seqId)
    {
        Result result = null;
        CmdResp resp = fromRespQueue(seqId);
        if (null == resp)
        {
            return result;
        }
        
        result = Utils.fromCmdResp(resp);
        
        return result;
    }
    
    public Result removeRspQueue(int seqId)
    {
        Result result = null;
        CmdResp item = fromRespQueue(seqId);
        if (null == item)
        {
            return result;
        }
        
        respQueue.remove(item);
        return result;
    }

    public NioQueue<CmdReq> getHeartbeatReqQueue()
    {
        return heartbeatReqQueue;
    }

    public NioQueue<CmdReq> getUncnfmHeartBeats()
    {
        return uncnfmHeartBeats;
    }

    public NioQueue<CmdReq> getReqQueue()
    {
        return reqQueue;
    }

    public NioQueue<CmdResp> getRespQueue()
    {
        return respQueue;
    }
}
