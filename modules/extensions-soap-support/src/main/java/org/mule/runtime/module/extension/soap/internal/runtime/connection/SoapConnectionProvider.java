/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.soap.internal.runtime.connection;

import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.soap.SoapServiceProvider;
import org.mule.services.soap.api.SoapService;

import javax.inject.Inject;

public class SoapConnectionProvider implements PoolingConnectionProvider<ForwardingSoapClient> {

  @Inject
  private SoapService service;
  private SoapServiceProvider serviceProvider;

  SoapConnectionProvider(SoapServiceProvider serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public ForwardingSoapClient connect() throws ConnectionException {
    return new ForwardingSoapClient(service, serviceProvider);
  }

  @Override
  public void disconnect(ForwardingSoapClient connection) {
    connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(ForwardingSoapClient connection) {
    return success();
  }
}
