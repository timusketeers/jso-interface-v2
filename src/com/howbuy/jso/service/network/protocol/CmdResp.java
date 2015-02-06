package com.howbuy.jso.service.network.protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.howbuy.jso.service.network.stream.BinaryReadStream2;
import com.howbuy.jso.service.network.stream.BinaryWriteStream3;

/**
 * 指令响应对应的实体.
 * @author li.zhang
 * 2014-9-22
 *
 */
public class CmdResp
{
    /** 命令的标识.  **/
    private short cmd;
    
    /** 表示序列. **/
    private int seqId;
    
    /** 返回码. **/
    private int retCode;
    
    /** 参数列表.**/
    private Param[] params;
    
    /**
     * 将对应的字节流转换成CmdResp对象.
     * @param bytes
     * @return
     */
    public static CmdResp fromByteStream(byte[] bytes)
    {
        CmdResp resp = null;
        if (null == bytes || 0 == bytes.length)
        {
            return resp;
        }
        
        resp = new CmdResp();
        BinaryReadStream2 reader = new BinaryReadStream2(bytes);
        
        //read cmd
        AtomicReference<Short> cmd = new AtomicReference<Short>();
        reader.ReadShort(cmd);
        resp.setCmd(cmd.get());
        
        //read seqId
        AtomicInteger seq = new AtomicInteger();
        reader.ReadInt(seq);
        resp.setSeqId(seq.get());
            
        //read retCode
        AtomicInteger ret = new AtomicInteger();
        reader.ReadInt(ret);
        resp.setRetCode(ret.get());
        
        Param param = null;
        List<Param> params = new ArrayList<Param>();
        while (!reader.IsEnd())
        {
            param = new Param();
            AtomicReference<String> paramValue = new AtomicReference<String>();
            AtomicLong valuelen = new AtomicLong();
            try
            {
                reader.ReadString(paramValue, 0, valuelen);
                
                param.setHeader(valuelen.get());
                param.setParamstr(paramValue.get());
                
                params.add(param);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        
        Param[] paramArray = new Param[params.size()];
        params.toArray(paramArray);
        
        resp.setParams(paramArray);
        
        return resp;
    }
    
    public byte[] toByteStream()
    {
        BinaryWriteStream3 writer = new BinaryWriteStream3();
        
        writer.Write(cmd);
        writer.Write(seqId);
        writer.Write(retCode);
        
        if (null == params || 0 == params.length)
        {
            writer.Flush();
            return writer.getData();
        }
        
        for (int i = 0; i < params.length; i++)
        {
            String param = params[i].getParamstr();
            writer.Write(param, param.length());
        }
        
        writer.Flush();
        return writer.getData();
    }

    public short getCmd()
    {
        return cmd;
    }

    public void setCmd(short cmd)
    {
        this.cmd = cmd;
    }

    public int getSeqId()
    {
        return seqId;
    }

    public void setSeqId(int seqId)
    {
        this.seqId = seqId;
    }

    public int getRetCode()
    {
        return retCode;
    }

    public void setRetCode(int retCode)
    {
        this.retCode = retCode;
    }

    public Param[] getParams()
    {
        return params;
    }

    public void setParams(Param[] params)
    {
        this.params = params;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("cmd:").append(cmd).append(",")
               .append("seqId:").append(seqId).append(",")
               .append("retCode:").append(retCode).append(",");
        if (null == params || 0 == params.length)
        {
            builder.append("params:[]");
        }
        else
        {
            builder.append("params:[");
            for (int i = 0; i < params.length; i++)
            {
                if (i == params.length - 1)
                {
                    builder.append(params[i].toString());
                }
                else
                {
                    builder.append(params[i].toString()).append(",");
                }
            }
            
            builder.append("]");
        }
        
        return builder.toString();
    }
}
