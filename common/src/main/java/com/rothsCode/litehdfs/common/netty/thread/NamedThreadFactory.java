package com.rothsCode.litehdfs.common.netty.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rothsCode
 */
public class NamedThreadFactory implements ThreadFactory {

  private boolean daemon;
  private String prefix;
  private AtomicInteger threadId = new AtomicInteger();

  public NamedThreadFactory(String prefix) {
    this(prefix, true);
  }

  public NamedThreadFactory(String prefix, boolean daemon) {
    this.prefix = prefix;
    this.daemon = daemon;
  }

  @Override
  public Thread newThread(Runnable r) {
    return new DefaultThread(prefix + threadId.getAndIncrement(), r, daemon);
  }
}
