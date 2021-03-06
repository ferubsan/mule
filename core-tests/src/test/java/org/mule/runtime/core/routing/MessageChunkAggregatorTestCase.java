/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.routing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mule.runtime.api.message.Message.of;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.EventContext;
import org.mule.runtime.core.api.MuleSession;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.message.GroupCorrelation;
import org.mule.runtime.core.session.DefaultMuleSession;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.testmodels.fruit.Apple;

import org.junit.Test;

public class MessageChunkAggregatorTestCase extends AbstractMuleContextTestCase {

  public MessageChunkAggregatorTestCase() {
    setStartContext(true);
  }

  @Test
  public void testMessageProcessor() throws Exception {
    MuleSession session = new DefaultMuleSession();
    Flow flow = getTestFlowWithComponent("test", Apple.class);
    assertNotNull(flow);

    MessageChunkAggregator router = new MessageChunkAggregator();
    router.setMuleContext(muleContext);
    router.setFlowConstruct(flow);
    router.initialise();

    Message message1 = of("test event A");
    Message message2 = of("test event B");
    Message message3 = of("test event C");

    EventContext context = DefaultEventContext.create(flow, TEST_CONNECTOR, "foo");

    Event event1 = Event.builder(context).message(message1).groupCorrelation(new GroupCorrelation(3, null)).flow(flow)
        .session(session).build();
    Event event2 = Event.builder(context).message(message2).flow(flow).session(session).build();
    Event event3 = Event.builder(context).message(message3).flow(flow).session(session).build();

    assertNull(router.process(event1));
    assertNull(router.process(event2));
    Event resultEvent = router.process(event3);
    assertNotNull(resultEvent);
    Message resultMessage = resultEvent.getMessage();
    assertNotNull(resultMessage);
    String payload = getPayloadAsString(resultMessage);

    assertTrue(payload.contains("test event A"));
    assertTrue(payload.contains("test event B"));
    assertTrue(payload.contains("test event C"));
    assertTrue(payload.matches("test event [A,B,C]test event [A,B,C]test event [A,B,C]"));
  }
}
