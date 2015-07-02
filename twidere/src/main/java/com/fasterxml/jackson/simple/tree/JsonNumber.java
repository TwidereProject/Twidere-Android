package com.fasterxml.jackson.simple.tree;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;

public class JsonNumber extends JsonAbstractValue
{
    private static final Map<Class<? extends Number>, JsonParser.NumberType> NUMBER_TYPES;
    static
    {
        final Map<Class<? extends Number>, JsonParser.NumberType> numberTypes = new HashMap<Class<? extends Number>, JsonParser.NumberType>();

        numberTypes.put(Byte.class, JsonParser.NumberType.INT);
        numberTypes.put(Short.class, JsonParser.NumberType.INT);
        numberTypes.put(Integer.class, JsonParser.NumberType.INT);
        numberTypes.put(Long.class, JsonParser.NumberType.LONG);
        numberTypes.put(BigInteger.class, JsonParser.NumberType.BIG_INTEGER);
        numberTypes.put(Float.class, JsonParser.NumberType.FLOAT);
        numberTypes.put(Double.class, JsonParser.NumberType.DOUBLE);
        numberTypes.put(BigDecimal.class, JsonParser.NumberType.BIG_DECIMAL);

        NUMBER_TYPES = Collections.unmodifiableMap(numberTypes);
    }

    private final Number _value;
    private final JsonParser.NumberType _numberType;

    public JsonNumber(Number value)
    {
        if (!NUMBER_TYPES.containsKey(value.getClass()))
            throw new IllegalArgumentException("Unsupported Number type");
        this._value = value;
        this._numberType = NUMBER_TYPES.get(value.getClass());
    }

    public Number getValue()
    {
        return _value;
    }

    @Override
    public JsonToken asToken() {
        switch (numberType())
        {
            case BIG_DECIMAL:
            case DOUBLE:
            case FLOAT:
                return VALUE_NUMBER_FLOAT;
            default:
                return VALUE_NUMBER_INT;
        }
    }

    @Override
    public boolean isValueNode()
    {
        return true;
    }

    @Override
    public JsonParser.NumberType numberType()
    {
        return _numberType;
    }
}
