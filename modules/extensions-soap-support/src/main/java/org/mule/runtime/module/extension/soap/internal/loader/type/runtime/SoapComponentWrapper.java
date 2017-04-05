/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.soap.internal.loader.type.runtime;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import org.mule.runtime.extension.api.annotation.soap.SoapTransportProviders;
import org.mule.runtime.module.extension.internal.loader.java.type.runtime.TypeWrapper;

import java.util.List;
import java.util.Optional;

abstract class SoapComponentWrapper extends TypeWrapper {

  SoapComponentWrapper(Class<?> aClass) {
    super(aClass);
  }

  public List<SoapTransportProviderTypeWrapper> getTransportProviders() {
    Optional<SoapTransportProviders> annotation = this.getAnnotation(SoapTransportProviders.class);
    if (annotation.isPresent()) {
      return stream(annotation.get().value()).map(SoapTransportProviderTypeWrapper::new).collect(toList());
    }
    return emptyList();
  }
}
