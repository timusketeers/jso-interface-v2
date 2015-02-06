package com.howbuy.jso.service.network;

import java.util.concurrent.Future;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.future.ExceptionResultFuture;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.Param;
import com.howbuy.jso.service.network.thread.task.FutureTask;
import com.howbuy.jso.service.network.thread.task.Task;
import com.howbuy.network.entity.Result;

/**
 * JsoExecutor
 * @author li.zhang
 * 2014-9-18
 *
 */
public class JsoExecutor
{
    /** INSTANCE **/
    private static final JsoExecutor INSTANCE = new JsoExecutor();
    
    /**
     * 私有构造方法.
     */
    private JsoExecutor()
    {
        
    }
    
    /**
     * 获取实例.
     * @return
     */
    public static JsoExecutor getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * 执行入口.
     * @param args args[0]:cmd, arg[1]:taCode
     * @return
     */
    public Future<Result> execute(String... args)
    {
        Future<Result> result = null;
        if (null == args || 0 == args.length)
        {
            result = new ExceptionResultFuture("0999", "Args error, please check the args.");
            return result;
        }
        
        String cmd = args[0];
        HostSelector.getInstance();
        
        CmdReq req = new CmdReq();
        switch (CmdEnum.forName(cmd))
        {
            case CLEAR_DIS_ACK:
                req.setCmd(CmdEnum.CLEAR_DIS_ACK.getCommand());
                Param[] params = new Param[1];
                params[0] = new Param();
                params[0].setParamstr(args[1]);
                req.setParams(params);
                break;
                
            case CLEAR_ACK:
                req.setCmd(CmdEnum.CLEAR_DIS_ACK.getCommand());
                break;
                
            case CLEAR_DIS_ACK_DTL:
                req.setCmd(CmdEnum.CLEAR_DIS_ACK.getCommand());
                break;
    
            default:
                result = new ExceptionResultFuture("0999", "There is no command named as " + cmd);
                return result;
        }
        
        Task task = new FutureTask(req);
        result = task.submit();
        
        return result;
    }
}
