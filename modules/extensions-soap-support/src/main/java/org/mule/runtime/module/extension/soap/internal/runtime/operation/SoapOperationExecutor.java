/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.soap.internal.runtime.operation;

import static org.mule.runtime.module.extension.soap.internal.loader.InvokeOperationDeclarer.OPERATION_PARAM;
import static org.mule.runtime.module.extension.soap.internal.loader.InvokeOperationDeclarer.REQUEST_PARAM;
import static org.mule.runtime.module.extension.soap.internal.loader.InvokeOperationDeclarer.SERVICE_PARAM;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.justOrEmpty;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.extension.api.runtime.operation.OperationExecutor;
import org.mule.runtime.module.extension.internal.runtime.resolver.ConnectionArgumentResolver;
import org.mule.runtime.module.extension.soap.internal.runtime.connection.ForwardingSoapClient;
import org.mule.services.soap.api.client.SoapClient;
import org.mule.services.soap.api.message.SoapRequest;

import java.io.ByteArrayInputStream;

import org.reactivestreams.Publisher;

/**
 * Implementation of {@link OperationExecutor} which works by using reflection to invoke a method from a class.
 *
 * @since 3.7.0
 */
public final class SoapOperationExecutor implements OperationExecutor {

  private final ConnectionArgumentResolver connectionResolver = new ConnectionArgumentResolver();

  /**
   * {@inheritDoc}
   */
  @Override
  public Publisher<Object> execute(ExecutionContext<OperationModel> executionContext) {
    try {
      String serviceId = executionContext.getParameter(SERVICE_PARAM);
      ForwardingSoapClient connection = (ForwardingSoapClient) connectionResolver.resolve(executionContext);
      SoapClient client = connection.getSoapClient(serviceId);
      return justOrEmpty(client.consume(getRequest(executionContext)));
    } catch (Exception e) {
      return error(e);
    }
  }

  private SoapRequest getRequest(ExecutionContext<OperationModel> context) {
    // Use Argument Resolver
    Object operation = context.getParameter(OPERATION_PARAM);
    String request = context.getParameter(REQUEST_PARAM);
    return SoapRequest.builder().withOperation((String) operation).withContent(new ByteArrayInputStream(request.getBytes())).build();
  }

}
