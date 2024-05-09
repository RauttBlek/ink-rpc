package com.ink.rpc.retry;

import com.ink.rpc.spi.SpiLoader;

public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    public static final RetryStrategy defaultRetryStrategy = new NoRetryStrategy();

    public static RetryStrategy getRetryStrategyInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
