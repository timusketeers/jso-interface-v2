package com.howbuy.jso.service.queue;

import java.util.Vector;

/**
 * 队列.
 * @author li.zhang
 * 2014-9-23
 *
 */
public class NioQueue<T>
{
    /** 队列. **/
    private Vector<T> queue = new Vector<T>();
    
    /**
     * 入队列.
     * @param buffer
     */
    public void offer(T buffer)
    {
        queue.add(buffer);
    }
    
    /**
     * 从队列中拉出.
     * @return
     */
    public T poll()
    {
        return queue.remove(0);
    }
    
    /**
     * 获取队列最前端的元素.但不出队.
     * @return
     */
    public T peek()
    {
        return queue.get(0);
    }
    
    /**
     * 获得index索引处的元素.
     * @param index
     * @return
     */
    public T get(int index)
    {
        return queue.get(index);
    }
    
    /**
     * 删除队列中的某个元素.
     * @param item
     * @return
     */
    public boolean remove(T item)
    {
        return queue.remove(item);
    }
    
    /**
     * 判断队列是否为空.
     * @return
     */
    public boolean isEmpty()
    {
        return queue.isEmpty();
    }
    
    /**
     * 得到队列长度.
     * @return
     */
    public int getQueueSize()
    {
        return queue.size();
    }
    
    /**
     * 清空队列
     */
    public void clear()
    {
        queue.clear();
    }
}
