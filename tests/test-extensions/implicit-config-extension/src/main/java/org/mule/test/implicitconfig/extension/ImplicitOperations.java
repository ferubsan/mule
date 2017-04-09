/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.implicitconfig.extension;

import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.tck.testmodels.fruit.Apple;

public class ImplicitOperations {

  public ImplicitConfigExtension getConfig(@UseConfig ImplicitConfigExtension config) {
    return config;
  }

  public Apple getConnection(@Connection Apple connection) {
    return connection;
  }
}
