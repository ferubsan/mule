/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.expression;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;
import org.mule.api.expression.RequiredValueException;
import org.mule.config.i18n.CoreMessages;
import org.mule.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * If the message payload is a map this extractor will look up the property value in
 * the map
 */
public class MapPayloadExpressionEvaluator implements ExpressionEvaluator
{
    public static final String NAME = "map-payload";

    public Object evaluate(String expression, MuleMessage message)
    {
        Object payload = message.getPayload();

        if (!(payload instanceof Map))
        {
            return null;
        }
        
        if (expression.indexOf(",") > -1)
        {

            String[] strings = StringUtils.splitAndTrim(expression, ",");
            Map<String, Object> result = new HashMap<String, Object>(strings.length);

            for (String s : strings)
            {
                Object val = getValue(s, (Map) payload);
                if (val != null)
                {
                    if (s.endsWith("*"))
                    {
                        s = s.substring(0, s.length() - 1);
                    }
                    result.put(s, val);
                }
            }
            return result;
        }
        else
        {
            return getValue(expression, (Map)payload);
        }
    }

    protected Object getValue(String key, Map map)
    {
        boolean required;
        if (key.endsWith("*"))
        {
            key = key.substring(0, key.length() - 1);
            required = false;
        }
        else
        {
            required = true;
        }
        Object val = map.get(key);
        if (val != null)
        {
            return val;
        }
        else if (required)
        {
            throw new RequiredValueException(CoreMessages.expressionEvaluatorReturnedNull(NAME, key));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name)
    {
        throw new UnsupportedOperationException("setName");
    }

}
