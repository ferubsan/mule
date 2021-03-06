/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.el;

import static java.util.Optional.empty;
import static org.apache.commons.lang.SystemUtils.FILE_SEPARATOR;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.metadata.DataType.OBJECT;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.api.metadata.DataType.fromType;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.ATTRIBUTES;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.DATA_TYPE;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.ERROR;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.FLOW;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.PAYLOAD;
import static org.mule.runtime.core.el.DataWeaveExpressionLanguage.VARIABLES;

import org.junit.Before;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Attributes;
import org.mule.runtime.api.message.Error;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.message.NullAttributes;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.config.MuleManifest;
import org.mule.runtime.core.message.BaseAttributes;
import org.mule.runtime.core.util.SystemUtils;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("Expression Language")
@Stories("Support DW")
public class DataWeaveExpressionLanguageTestCase extends AbstractMuleContextTestCase {

  private DataWeaveExpressionLanguage expressionLanguage;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() {
    expressionLanguage = new DataWeaveExpressionLanguage(muleContext);
  }

  @Test
  public void stringExpression() throws Exception {
    TypedValue result = expressionLanguage.evaluate("\"hey\"", testEvent(), BindingContext.builder().build());
    assertThat(result.getValue(), is("hey"));
    assertThat(result.getDataType(), is(equalTo(STRING)));
  }

  @Test
  public void withPrefixExpression() throws Exception {
    TypedValue result = expressionLanguage.evaluate("#[dw:\"hey\"]", testEvent(), BindingContext.builder().build());
    assertThat(result.getValue(), is("hey"));
    assertThat(result.getDataType(), is(equalTo(STRING)));
  }

  @Test
  public void attributesBinding() throws Exception {
    Event event = getEventWithError(empty());
    Attributes attributes = new SomeAttributes();
    InternalMessage message = (InternalMessage) Message.builder().nullPayload().attributes(attributes).build();
    when(event.getMessage()).thenReturn(message);

    TypedValue result = expressionLanguage.evaluate(ATTRIBUTES, event, BindingContext.builder().build());
    assertThat(result.getValue(), is(equalTo(attributes)));
    assertThat(result.getDataType().getType(), is((equalTo(SomeAttributes.class))));
  }

  @Test
  public void errorBinding() throws Exception {
    Error error = mock(Error.class);
    Optional<Error> opt = Optional.of(error);
    Event event = getEventWithError(opt);
    doReturn(testEvent().getMessage()).when(event).getMessage();

    TypedValue result = expressionLanguage.evaluate(ERROR, event, BindingContext.builder().build());
    assertThat(result.getValue(), is(sameInstance(error)));
  }

  @Test
  public void errorMessageContainsDataWeaveExceptionCauseMessage() throws Exception {
    Error error = mock(Error.class);
    Optional<Error> opt = Optional.of(error);
    Event event = getEventWithError(opt);
    doReturn(testEvent().getMessage()).when(event).getMessage();
    String expressionThatThrowsException = "payload + 'foo'";

    expectedEx.expect(ExpressionRuntimeException.class);
    expectedEx.expectMessage(containsString("Type mismatch for '+' function"));
    expectedEx.expectMessage(containsString("evaluating expression: \"" + expressionThatThrowsException));

    expressionLanguage.evaluate(expressionThatThrowsException, event, BindingContext.builder().build());
  }

  @Test
  public void payloadBinding() throws Exception {
    Event event = getEventWithError(empty());
    InternalMessage message = mock(InternalMessage.class, RETURNS_DEEP_STUBS);
    when(event.getMessage()).thenReturn(message);
    TypedValue payload = new TypedValue<>("hey", STRING);
    when(message.getPayload()).thenReturn(payload);

    TypedValue result = expressionLanguage.evaluate(PAYLOAD, event, BindingContext.builder().build());
    assertThat(result.getValue(), is(equalTo(payload.getValue())));
    assertThat(result.getDataType(), is(equalTo(payload.getDataType())));
  }

  @Test
  public void dataTypeBinding() throws Exception {
    Event event = getEventWithError(empty());
    InternalMessage message = mock(InternalMessage.class, RETURNS_DEEP_STUBS);
    when(event.getMessage()).thenReturn(message);
    TypedValue payload = new TypedValue<>("hey", STRING);
    when(message.getPayload()).thenReturn(payload);

    TypedValue result = expressionLanguage.evaluate(DATA_TYPE, event, BindingContext.builder().build());
    assertThat(result.getValue(), is(equalTo(STRING)));
    assertThat(fromType(DataType.class).isCompatibleWith(result.getDataType()), is(true));
  }

  @Test
  public void variablesBindings() throws Exception {
    Event event = getEventWithError(empty());
    String var1 = "var1";
    String var2 = "var2";
    when(event.getVariableNames()).thenReturn(Sets.newHashSet(var1, var2));
    TypedValue varValue = new TypedValue<>(null, OBJECT);
    when(event.getVariable(var1)).thenReturn(varValue);
    when(event.getVariable(var2)).thenReturn(varValue);

    TypedValue result = expressionLanguage.evaluate(VARIABLES, event, BindingContext.builder().build());
    assertThat(result.getValue(), is(instanceOf(Map.class)));
    assertThat((Map<String, TypedValue>) result.getValue(), hasEntry(var1, varValue));
    assertThat((Map<String, TypedValue>) result.getValue(), hasEntry(var2, varValue));
  }

  @Test
  public void singleVariableBindings() throws Exception {
    Event event = getEventWithError(empty());
    String var1 = "var1";
    String var2 = "var2";
    when(event.getVariableNames()).thenReturn(Sets.newHashSet(var1, var2));
    String varValue = "mangoose";
    TypedValue var = new TypedValue<>(varValue, STRING);
    when(event.getVariable(var1)).thenReturn(var);
    when(event.getVariable(var2)).thenReturn(var);

    TypedValue resultVar1 = expressionLanguage.evaluate(var1, event, BindingContext.builder().build());
    assertThat(resultVar1.getValue(), is(varValue));
    assertThat(resultVar1.getDataType(), is(STRING));
    TypedValue resultVar2 = expressionLanguage.evaluate(var2, event, BindingContext.builder().build());
    assertThat(resultVar2.getValue(), is(varValue));
    assertThat(resultVar2.getDataType(), is(STRING));
  }

  @Test
  public void variablesCannotOverrideEventBindings() throws MuleException {
    Event event = spy(testEvent());
    HashSet<String> variables = Sets.newHashSet(PAYLOAD, ATTRIBUTES, ERROR, VARIABLES, FLOW);
    when(event.getVariableNames()).thenReturn(variables);
    TypedValue<String> varValue = new TypedValue<>("", STRING);
    variables.forEach(var -> doReturn(varValue).when(event).getVariable(var));
    FlowConstruct mockFlowConstruct = mock(FlowConstruct.class);
    String flowName = "myFlowName";
    when(mockFlowConstruct.getName()).thenReturn(flowName);

    assertThat(expressionLanguage.evaluate(PAYLOAD, event, BindingContext.builder().build()).getValue(), is(TEST_PAYLOAD));
    assertThat(expressionLanguage.evaluate(ATTRIBUTES, event, BindingContext.builder().build()).getValue(), is(instanceOf(
                                                                                                                          NullAttributes.class)));
    assertThat(expressionLanguage.evaluate(ERROR, event, BindingContext.builder().build()).getValue(), is(nullValue()));
    assertThat(expressionLanguage.evaluate(VARIABLES, event, BindingContext.builder().build()).getValue(),
               is(instanceOf(Map.class)));
    assertThat(expressionLanguage.evaluate("flow.name", event, mockFlowConstruct, BindingContext.builder().build()).getValue(),
               is(flowName));
  }

  @Test
  public void accessRegistryBean() throws MuleException {
    Event event = testEvent();
    muleContext.getRegistry().registerObject("myBean", new MyBean("DataWeave"));
    TypedValue evaluate = expressionLanguage.evaluate("app.registry.myBean.name", event, BindingContext.builder().build());
    assertThat(evaluate.getValue(), is("DataWeave"));
  }

  @Test
  public void accessServerFileSeparator() throws MuleException {
    Event event = testEvent();
    muleContext.getRegistry().registerObject("myBean", new MyBean("DataWeave"));
    TypedValue evaluate = expressionLanguage.evaluate("server.fileSeparator", event, BindingContext.builder().build());
    assertThat(evaluate.getValue(), is(FILE_SEPARATOR));
  }

  @Test
  public void accessMuleVersion() throws MuleException {
    Event event = testEvent();
    muleContext.getRegistry().registerObject("myBean", new MyBean("DataWeave"));
    TypedValue evaluate = expressionLanguage.evaluate("mule.version", event, BindingContext.builder().build());
    assertThat(evaluate.getValue(), is(MuleManifest.getProductVersion()));
  }

  @Test
  public void flowNameBinding() {
    Event event = getEventWithError(empty());
    FlowConstruct mockFlowConstruct = mock(FlowConstruct.class);
    String flowName = "myFlowName";
    when(mockFlowConstruct.getName()).thenReturn(flowName);

    TypedValue result = expressionLanguage.evaluate("flow.name", event, mockFlowConstruct, BindingContext.builder().build());
    assertThat(result.getDataType(), is(STRING));
    assertThat(result.getValue(), is(flowName));
  }

  private Event getEventWithError(Optional<Error> error) {
    Event event = mock(Event.class, RETURNS_DEEP_STUBS);
    doReturn(error).when(event).getError();
    when(event.getMessage().getPayload()).thenReturn(new TypedValue<>(null, OBJECT));
    return event;
  }

  private class SomeAttributes extends BaseAttributes {

  }

  private class MyBean {

    private String name;

    public MyBean(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
