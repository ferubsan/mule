/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.scheduler.internal;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;

import org.mule.runtime.core.util.concurrent.WaitPolicy;
import org.mule.service.scheduler.internal.exception.SchedulerBusyException;

import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

/**
 * Dynamically determines the {@link RejectedExecutionHandler} implementation to use according to the {@link ThreadGroup} of the
 * current thread.
 * 
 * @see AbortPolicy
 * @see WaitPolicy
 * 
 * @since 4.0
 */
public final class ByCallerThreadGroupPolicy implements RejectedExecutionHandler {

  private final AbortPolicy abort = new AbortPolicy() {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      throw new SchedulerBusyException();
    }
  };
  private final WaitPolicy wait = new WaitPolicy();

  private final Set<ThreadGroup> waitGroups;

  /**
   * Builds a new {@link ByCallerThreadGroupPolicy} with the given {@code waitGroups}.
   * 
   * @param waitGroups the group of threads for which a {@link WaitPolicy} will be applied. For the rest, an {@link AbortPolicy}
   *        will be applied.
   */
  public ByCallerThreadGroupPolicy(Set<ThreadGroup> waitGroups) {
    this.waitGroups = unmodifiableSet(waitGroups);
  }

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    if (currentThread().getThreadGroup() == null || waitGroups.contains(currentThread().getThreadGroup())) {
      wait.rejectedExecution(r, executor);
    } else {
      abort.rejectedExecution(r, executor);
    }
  }

}
