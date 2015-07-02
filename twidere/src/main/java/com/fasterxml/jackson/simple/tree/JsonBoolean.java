package com.fasterxml.jackson.simple.tree;

import com.fasterxml.jackson.core.JsonToken;

public class JsonBoolean extends JsonAbstractValue
{
    public static JsonBoolean TRUE = new JsonBoolean();
    public static JsonBoolean FALSE = new JsonBoolean();

    private JsonBoolean()
    {

    }

    @Override
    public JsonToken asToken()
    {
        return this == TRUE ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
    }

    @Override
    public boolean isValueNode()
    {
        return true;
    }
}
