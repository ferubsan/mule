/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.soap.internal.runtime.connection;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.services.soap.api.client.SoapClientConfiguration.builder;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.extension.api.soap.SoapServiceProvider;
import org.mule.runtime.extension.api.soap.WebServiceDefinition;
import org.mule.services.soap.api.SoapService;
import org.mule.services.soap.api.client.SoapClient;
import org.mule.services.soap.api.client.SoapClientConfiguration;
import org.mule.services.soap.api.client.SoapClientFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ForwardingSoapClient {

  private final LoadingCache<WebServiceDefinition, SoapClient> clientsCache;
  private final List<WebServiceDefinition> webServiceDefinitions;

  ForwardingSoapClient(SoapService service, SoapServiceProvider serviceProvider) {
    this.webServiceDefinitions = serviceProvider.getWebServiceDefinitions();
    this.clientsCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1, MINUTES)
      .build(new SoapClientCacheLoader(service));
  }

  public SoapClient getSoapClient(String id) throws MuleException {
    WebServiceDefinition wsd = webServiceDefinitions.stream().filter(ws -> ws.getServiceId().equals(id)).findAny()
      .orElseThrow(() -> new IllegalArgumentException("Could not find a soap client id [" + id + "]"));
    try {
      return clientsCache.get(wsd);
    } catch (ExecutionException e) {
      throw new MuleRuntimeException(createStaticMessage("Error while retrieving soap client id [" + id + "]"), e);
    }
  }

  public void disconnect() {
    clientsCache.asMap().values().forEach(sc -> {
      try {
        sc.stop();
      } catch (MuleException e) {
        throw new MuleRuntimeException(createStaticMessage("A problem occurred while disconnecting client: '%s'", sc), e);
      }
    });
  }

  private class SoapClientCacheLoader extends CacheLoader<WebServiceDefinition, SoapClient> {

    private final SoapService service;

    private SoapClientCacheLoader(SoapService service) {
      this.service = service;
    }

    @Override
    public SoapClient load(WebServiceDefinition definition) throws Exception {
      SoapClientFactory clientFactory = service.getClientFactory();
      SoapClientConfiguration config = builder()
        .withService(definition.getService())
        .withPort(definition.getPort())
        .withWsdlLocation(definition.getWsdlUrl().toString())
        .build();
      return clientFactory.create(config);
    }
  }
}
