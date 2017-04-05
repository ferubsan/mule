/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.soap.extension;

import static org.mule.test.soap.extension.FootballSoapExtension.TEST_SERVICE_URL;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.soap.SoapServiceProvider;
import org.mule.runtime.extension.api.soap.WebServiceDefinition;

import com.google.common.collect.ImmutableList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class LaLigaServiceProvider implements SoapServiceProvider {

  public static final String LA_LIGA = "La Liga";
  public static final String LA_LIGA_SERVICE_A = "LaLigaServiceA";
  public static final String LA_LIGA_PORT_A = "LaLigaPortA";

  @Parameter
  private String firstDivision;

  @Parameter
  private String secondDivision;

  @Override
  public List<WebServiceDefinition> getWebServiceDefinitions() {
    return ImmutableList.<WebServiceDefinition>builder().add(getFirstDivisionService()).add(getSecondDivisionService()).build();
  }

  private WebServiceDefinition getFirstDivisionService() {
    try {
      return new WebServiceDefinition("A", firstDivision, new URL(TEST_SERVICE_URL), LA_LIGA_SERVICE_A, LA_LIGA_PORT_A);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private WebServiceDefinition getSecondDivisionService() {
    try {
      return new WebServiceDefinition("B", secondDivision, new URL(TEST_SERVICE_URL), "ServiceB", "PortB");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
