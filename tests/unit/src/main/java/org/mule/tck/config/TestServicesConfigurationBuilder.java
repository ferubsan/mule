/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tck.config;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.mule.runtime.api.el.ExpressionExecutor;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.registry.MuleRegistry;
import org.mule.runtime.core.api.util.Pair;
import org.mule.runtime.core.config.builders.AbstractConfigurationBuilder;
import org.mule.service.http.api.HttpService;
import org.mule.services.soap.api.SoapService;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;

import com.mulesoft.weave.el.WeaveExpressionExecutor;

import java.util.List;

/**
 * Registers services instances into the {@link MuleRegistry} of a {@link MuleContext}.
 * <p>
 * This is to be used only in tests that do not leverage the service injection mechanism.
 *
 * @since 4.0
 */
public class TestServicesConfigurationBuilder extends AbstractConfigurationBuilder {

  private static final SimpleUnitTestSupportSchedulerService schedulerService = new SimpleUnitTestSupportSchedulerService();

  private final Pair<String, ExpressionExecutor> expressionService;
  private final Pair<String, SoapService> soapService;
  private final Pair<String, HttpService> httpService;

  private TestServicesConfigurationBuilder(Pair<String, ExpressionExecutor> expressionService,
                                           Pair<String, SoapService> soapService,
                                           Pair<String, HttpService> httpService) {
    this.expressionService = expressionService;
    this.soapService = soapService;
    this.httpService = httpService;
  }

  @Override
  public void doConfigure(MuleContext muleContext) throws Exception {
    MuleRegistry registry = muleContext.getRegistry();
    registry.registerObject(schedulerService.getName(), spy(schedulerService));

    if (expressionService != null) {
      registry.registerObject(expressionService.getFirst(), expressionService.getSecond());
    }
    if (httpService != null) {
      registry.registerObject(httpService.getFirst(), httpService.getSecond());
    }
    if (soapService != null) {
      registry.registerObject(soapService.getFirst(), soapService.getSecond());
    }
  }

  public void stopServices() throws MuleException {
    final List<Scheduler> schedulers = schedulerService.getSchedulers();
    try {
      assertThat(schedulers, empty());
    } finally {
      schedulerService.stop();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static TestServicesConfigurationBuilder mocks() {
    return builder().withMockHttpService().withMockHttpService().withMockExpressionExecutor().build();
  }

  public static class Builder {

    private Builder() {
    }

    private static final String MOCK_HTTP_SERVICE = "mockHttpService";
    private static final String MOCK_SOAP_SERVICE = "mockSoapService";
    private static final String MOCK_EXPR_EXECUTOR = "mockExpressionExecutor";

    private Pair<String, SoapService> soapService;
    private Pair<String, HttpService> httpService;
    private Pair<String, ExpressionExecutor> expressionService;

    public Builder withSoapService(SoapService soapService) {
      this.soapService = new Pair<>(soapService.getName(), soapService);
      return this;
    }

    public Builder withMockSoapService() {
      this.soapService = new Pair<>(MOCK_SOAP_SERVICE, mock(SoapService.class));
      return this;
    }

    public Builder withHttpService(HttpService service) {
      this.httpService = new Pair<>(service.getName(), service);
      return this;
    }

    public Builder withMockHttpService() {
      this.httpService = new Pair<>(MOCK_HTTP_SERVICE, mock(HttpService.class));
      return this;
    }

    public Builder withExpressionExecutor() {
      final WeaveExpressionExecutor exprExecutor = new WeaveExpressionExecutor();
      this.expressionService = new Pair<>(exprExecutor.getName(), exprExecutor);
      return this;
    }

    public Builder withMockExpressionExecutor() {
      this.expressionService = new Pair<>(MOCK_EXPR_EXECUTOR, mock(ExpressionExecutor.class));
      return this;
    }

    public TestServicesConfigurationBuilder build() {
      return new TestServicesConfigurationBuilder(expressionService, soapService, httpService);
    }
  }

}
