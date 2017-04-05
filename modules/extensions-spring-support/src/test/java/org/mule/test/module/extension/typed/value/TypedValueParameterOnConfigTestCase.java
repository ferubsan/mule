/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.extension.typed.value;

import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
import org.mule.runtime.api.metadata.MediaType;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
// TODO(pablo.kraan): tests - fix this test - requires aexternal extension
public class TypedValueParameterOnConfigTestCase extends AbstractTypedValueTestCase {

  @Override
  protected String[] getConfigFiles() {
    return new String[] {"typed-value-on-config-config.xml"};
  }

  @After
  public void cleanUp() {
    TypedValueSource.onSuccessValue = null;
  }

  @Test
  public void typedValueOnDynamicConfig() throws Exception {
    TypedValueExtension extension =
        (TypedValueExtension) flowRunner("typedValueOnDynamicConfig").run().getMessage().getPayload().getValue();

    assertTypedValue(extension.stringTypedValue, "string", APPLICATION_JSON, UTF8);
    assertTypedValue(extension.differedDoor.getAddress(), "address", MediaType.ANY, UTF8);
  }

  @Test
  public void typedValueOnStaticConfig() throws Exception {
    TypedValueExtension extension =
        (TypedValueExtension) flowRunner("typedValueOnStaticConfig").run().getMessage().getPayload().getValue();

    assertTypedValue(extension.stringTypedValue, "string", MediaType.ANY, null);
    assertTypedValue(extension.differedDoor.getAddress(), "address", MediaType.ANY, null);
  }
}
