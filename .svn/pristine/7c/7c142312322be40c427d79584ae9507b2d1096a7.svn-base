package com.howbuy.jso.service.network.utils;

import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.protocol.Param;
import com.howbuy.jso.service.queue.NioQueue;
import com.howbuy.network.entity.Result;

public class Utils
{
    public static Result fromCmdResp(CmdResp resp)
    {
        Result result = null;
        if (null == resp)
        {
            return result;
        }
        
        result = new Result();
        
        String returnCode = "";
        String returnMsg = "";
        Param[] params = resp.getParams();
        if (null != params && 0 < params.length)
        {
            try
            {
                String jsonstr = new String(params[0].getParamstr().getBytes("ISO-8859-1"), Charset.defaultCharset().name());
                if (!StringUtils.isEmpty(jsonstr))
                {
                    JSONObject jsonObj = JSONObject.parseObject(jsonstr);
                    
                    returnCode = concatStr((String)jsonObj.get("ret"));
                    returnMsg = (String)jsonObj.get("msg");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        result.setRetCode(returnCode);
        result.setRetMsg(returnMsg);
        
        return result;
    }

    private static String concatStr(String str)
    {
        if (null == str || 0 == str.length())
        {
            return str;
        }
        
        if (str.length() >= 4)
        {
            return str;
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4 - str.length(); i++)
        {
            builder.append("0");
        }
        
        builder.append(str);
        
        return builder.toString();
    }

    /**
     * 根据seqId得到队列中相应seqId所对应的CmdReq对象.
     * @param queue
     * @param seqId
     * @return
     */
    public static CmdReq correspondingCmdReq(NioQueue<CmdReq> queue, int seqId)
    {
        CmdReq req = null;
        if (null == queue || queue.isEmpty())
        {
            return req;
        }
        
        for (int i = 0; i < queue.getQueueSize(); i++)
        {
            if (seqId == queue.get(i).getSeqId())
            {
                req = queue.get(i);
                break;
            }
        }
        return req;
    }
    
}
