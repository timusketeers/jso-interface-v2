package com.howbuy.jso.service.network.sequence;

public class SeqIdGenerator
{
    /** seq起始值.**/
    private static final int BASE_VALUE = 0;
    
    /** seq_seed. **/
    private static int seq_seed = BASE_VALUE;
    
    /**
     * 得到序列Id.
     * @return
     */
    public synchronized static int nextSeqId()
    {
        if (seq_seed == Integer.MAX_VALUE)
        {
            seq_seed = BASE_VALUE;
        }
        
        return ++seq_seed;
    }
}
