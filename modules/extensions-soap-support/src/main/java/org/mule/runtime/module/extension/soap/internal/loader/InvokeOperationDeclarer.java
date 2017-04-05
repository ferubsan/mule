/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.soap.internal.loader;

import static org.mule.metadata.java.api.JavaTypeLoader.JAVA;
import static org.mule.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.StringType;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.module.extension.internal.loader.java.property.ConnectivityModelProperty;
import org.mule.runtime.module.extension.internal.loader.java.property.OperationExecutorModelProperty;
import org.mule.runtime.module.extension.soap.internal.runtime.connection.ForwardingSoapClient;
import org.mule.runtime.module.extension.soap.internal.runtime.operation.SoapOperationExecutorFactory;

public class InvokeOperationDeclarer {

  static final String OPERATION_NAME = "invoke";
  static final String OPERATION_DESCRIPTION = "invokes Web Service operations";

  public static final String SERVICE_PARAM = "service";
  public static final String OPERATION_PARAM = "operation";
  public static final String REQUEST_PARAM = "request";

  private static final BaseTypeBuilder TYPE_BUILDER = BaseTypeBuilder.create(JAVA);
  private static final StringType STRING_TYPE = TYPE_BUILDER.stringType().build();

  public OperationDeclarer declare(ExtensionDeclarer declarer, ClassTypeLoader loader) {

    OperationDeclarer operation = declarer.withOperation(OPERATION_NAME).describedAs(OPERATION_DESCRIPTION)
      .withModelProperty(new OperationExecutorModelProperty(new SoapOperationExecutorFactory()));

    operation.withOutput().ofType(TYPE_BUILDER.stringType().build());
    operation.withOutputAttributes().ofType(TYPE_BUILDER.nullType().build());

    operation.requiresConnection(true)
      .withModelProperty(new ConnectivityModelProperty(ForwardingSoapClient.class));

    ParameterGroupDeclarer group = operation.blocking(true).onParameterGroup(DEFAULT_GROUP_NAME);

    group.withRequiredParameter(SERVICE_PARAM).ofType(STRING_TYPE);
    group.withRequiredParameter(OPERATION_PARAM).ofType(STRING_TYPE);

    // Replace with Headers/Body/Attachments/TransportHeaders and add metadata for each
    group.withRequiredParameter(REQUEST_PARAM).ofType(STRING_TYPE);

    return operation;
  }

}
