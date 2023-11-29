/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional debugrmation regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rothsCode.litehdfs.common.netty.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * rocketMq
 */
@Slf4j
public abstract class ServiceThread implements Runnable {

  private static final long JOIN_TIME = 90 * 1000;
  protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);
  //Make it able to restart the thread
  private final AtomicBoolean started = new AtomicBoolean(false);
  protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
  protected volatile boolean stopped = false;
  protected boolean isDaemon = false;
  private Thread thread;

  public ServiceThread() {

  }

  public abstract String getServiceName();

  public void start() {
    if (!started.compareAndSet(false, true)) {
      return;
    }
    stopped = false;
    this.thread = new Thread(this, getServiceName());
    this.thread.setDaemon(isDaemon);
    this.thread.start();
  }

  public void shutdown() {
    this.shutdown(false);
  }

  public void shutdown(final boolean interrupt) {
    if (!started.compareAndSet(true, false)) {
      return;
    }
    this.stopped = true;
    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }

    try {
      if (interrupt) {
        this.thread.interrupt();
      }

      long beginTime = System.currentTimeMillis();
      if (!this.thread.isDaemon()) {
        this.thread.join(this.getJoinTime());
      }
      long elapsedTime = System.currentTimeMillis() - beginTime;
    } catch (InterruptedException e) {
      log.error("Interrupted", e);
    }
  }

  public long getJoinTime() {
    return JOIN_TIME;
  }

  @Deprecated
  public void stop() {
    this.stop(false);
  }

  @Deprecated
  public void stop(final boolean interrupt) {
    if (!started.get()) {
      return;
    }
    this.stopped = true;
    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }

    if (interrupt) {
      this.thread.interrupt();
    }
  }

  public void makeStop() {
    if (!started.get()) {
      return;
    }
    this.stopped = true;
  }

  public void wakeup() {
    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }
  }

  protected void waitForRunning(long interval) {
    if (hasNotified.compareAndSet(true, false)) {
      this.onWaitEnd();
      return;
    }

    //entry to wait
    waitPoint.reset();

    try {
      waitPoint.await(interval, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.error("Interrupted", e);
    } finally {
      hasNotified.set(false);
      this.onWaitEnd();
    }
  }

  protected void onWaitEnd() {
  }

  public boolean isStopped() {
    return stopped;
  }

  public boolean isDaemon() {
    return isDaemon;
  }

  public void setDaemon(boolean daemon) {
    isDaemon = daemon;
  }
}
