/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.soap;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.services.soap.SoapTestUtils.assertSimilarXml;
import org.mule.functional.junit4.SoapExtensionFunctionalTestCase;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.util.IOUtils;
import org.mule.runtime.module.extension.internal.soap.services.FootballService;
import org.mule.runtime.module.extension.internal.soap.services.LaLigaService;
import org.mule.service.http.api.HttpService;
import org.mule.services.http.impl.service.HttpServiceImplementation;
import org.mule.services.soap.SoapServiceImplementation;
import org.mule.services.soap.TestHttpSoapServer;
import org.mule.services.soap.api.SoapService;
import org.mule.services.soap.api.message.SoapResponse;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.soap.extension.FootballSoapExtension;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class InvokeOperationExecutionTestCase extends SoapExtensionFunctionalTestCase {

  @Rule
  public DynamicPort footballPort = new DynamicPort("footballPort");

  @Rule
  public DynamicPort laLigaPort = new DynamicPort("laLigaPort");

  private final TestHttpSoapServer footballService = new TestHttpSoapServer(footballPort.getNumber(), new FootballService());
  private final TestHttpSoapServer laLigaService = new TestHttpSoapServer(laLigaPort.getNumber(), new LaLigaService());

  // TODO - MULE-11119: Remove once the service is injected higher up on the hierarchy
  private final SchedulerService schedulerService = new SimpleUnitTestSupportSchedulerService();
  private final HttpService httpService = new HttpServiceImplementation(schedulerService);
  private final SoapService soapService = new SoapServiceImplementation(httpService);

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    footballService.init();
    laLigaService.init();
    System.setProperty("footballAddress", footballService.getDefaultAddress());
    System.setProperty("laLigaAddress", laLigaService.getDefaultAddress());
  }

  @Override
  protected void doSetUp() throws Exception {
    startIfNeeded(httpService);
  }

  @After
  public void after() throws Exception {
    footballService.stop();
    laLigaService.stop();
  }

  @Override
  protected String getConfigFile() {
    return "soap-config.xml";
  }

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class<?>[] {FootballSoapExtension.class};
  }

  @Test
  public void simpleNoParamsOperation() throws Exception {
    Event response = flowRunner("getLeagues").withPayload(getBodyXml("getLeagues", "")).run();
    Object value = response.getMessage().getPayload().getValue();
    assertSimilarXml(getBodyXml("getLeaguesResponse", "<league>Calcio</league><league>La Liga</league>"),
                     IOUtils.toString(((SoapResponse) value).getContent()));
  }

  @Test
  public void operationWithHeaders() {

  }

  @Test
  public void downloadAttachment() {

  }


  private String getBodyXml(String tagName, String content){
    String ns = "http://services.soap.internal.extension.module.runtime.mule.org/";
    return String.format("<con:%s xmlns:con=\"%s\">%s</con:%s>", tagName, ns, content, tagName);
  }

  @Override
  protected SoapService getSoapService() {
    return soapService;
  }

  @Override
  protected HttpService getHttpService() {
    return httpService;
  }

  @Override
  protected void doTearDown() throws Exception {
    super.doTearDown();
    stopIfNeeded(httpService);
    stopIfNeeded(schedulerService);
  }
}
