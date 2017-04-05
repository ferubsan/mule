/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.functional.junit4;

import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.extension.api.loader.ExtensionModelLoader;
import org.mule.runtime.module.extension.soap.internal.loader.SoapExtensionModelLoader;
import org.mule.service.http.api.HttpService;
import org.mule.services.soap.api.SoapService;
import org.mule.tck.config.TestServicesConfigurationBuilder;

import java.util.List;

/**
 *
 * @since 4.0
 */
public abstract class SoapExtensionFunctionalTestCase extends ExtensionFunctionalTestCase {

  protected ExtensionModelLoader getExtensionModelLoader() {
    return new SoapExtensionModelLoader();
  }

  @Override
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    super.addBuilders(builders);
    builders.add(TestServicesConfigurationBuilder.builder()
                   .withHttpService(getHttpService())
                   .withSoapService(getSoapService())
                   .withExpressionExecutor()
                   .build());
  }

  protected abstract HttpService getHttpService();

  protected abstract SoapService getSoapService();

}
