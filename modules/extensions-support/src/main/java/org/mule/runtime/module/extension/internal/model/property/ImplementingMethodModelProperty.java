/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.model.property;

import org.mule.runtime.extension.api.introspection.ModelProperty;
import org.mule.runtime.extension.api.introspection.operation.OperationModel;
import org.mule.runtime.module.extension.internal.introspection.describer.model.MethodElement;

import java.lang.reflect.Method;

/**
 * An immutable model property which indicates that the owning {@link OperationModel} was derived from a given {@link #method}
 *
 * @since 4.0
 */
public final class ImplementingMethodModelProperty implements ModelProperty {

  private final Method method;
  private final MethodElement element;

  /**
   * Creates a new instance referencing the given {@code method}
   *
   * @param method a {@link Method} which defines the owning {@link OperationModel}
   * @throws IllegalArgumentException if {@code method} is {@code null}
   */
  public ImplementingMethodModelProperty(Method method, MethodElement element) {
    this.element = element;
    //checkArgument(method != null, "method cannot be null");
    this.method = method;
  }

  /**
   * @return a {@link Method} which defines the owning {@link OperationModel}
   */
  public Method getMethod() {
    return method;
  }

  public MethodElement getMethodElement() {
    return element;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code implementingMethod}
   */
  @Override
  public String getName() {
    return "implementingMethod";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isExternalizable() {
    return false;
  }
}
