/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.vegan.extension;


import static org.mule.test.vegan.extension.VeganExtension.APPLE;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;

@Configuration(name = APPLE)
@Operations({EatAppleOperation.class, SpreadVeganismOperation.class, VeganFidelityOperation.class})
@Sources({HarvestApplesSource.class, PaulMcCartneySource.class})
@ConnectionProviders(VeganAppleConnectionProvider.class)
public class AppleConfig {

  @Parameter
  private VeganCookBook cookBook;

  public VeganCookBook getCookBook() {
    return cookBook;
  }
}
