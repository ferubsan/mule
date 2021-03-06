/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.streaming.bytes;

import org.mule.runtime.core.internal.streaming.CursorManager;
import org.mule.runtime.core.internal.streaming.bytes.factory.InMemoryCursorStreamProviderFactory;
import org.mule.runtime.core.internal.streaming.bytes.factory.NullCursorStreamProviderFactory;
import org.mule.runtime.core.streaming.bytes.ByteStreamingManager;
import org.mule.runtime.core.streaming.bytes.CursorStreamProviderFactory;
import org.mule.runtime.core.streaming.bytes.InMemoryCursorStreamConfig;

/**
 * Default implementation of {@link ByteStreamingManager}
 *
 * @since 4.0
 */
public class DefaultByteStreamingManager implements ByteStreamingManager {

  private final CursorManager cursorManager;
  private final ByteBufferManager bufferManager;

  public DefaultByteStreamingManager(CursorManager cursorManager, ByteBufferManager bufferManager) {
    this.cursorManager = cursorManager;
    this.bufferManager = bufferManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorStreamProviderFactory getInMemoryCursorProviderFactory(InMemoryCursorStreamConfig config) {
    return new InMemoryCursorStreamProviderFactory(cursorManager, bufferManager, config);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorStreamProviderFactory getNullCursorProviderFactory() {
    return new NullCursorStreamProviderFactory(cursorManager, bufferManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorStreamProviderFactory getDefaultCursorProviderFactory() {
    return new InMemoryCursorStreamProviderFactory(cursorManager, bufferManager, InMemoryCursorStreamConfig.getDefault());
  }

  protected CursorManager getCursorManager() {
    return cursorManager;
  }

  protected ByteBufferManager getBufferManager() {
    return bufferManager;
  }
}
