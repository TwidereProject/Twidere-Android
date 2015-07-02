package com.fasterxml.jackson.simple.tree;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class JsonObject extends JsonAbstractValue
{
    private final Map<String, TreeNode> _values;

    public JsonObject()
    {
        this(Collections.<String, TreeNode>emptyMap());
    }

    public JsonObject(Map<String, TreeNode> values)
    {
        this._values = Collections.unmodifiableMap(values);
    }

    @Override
    public JsonToken asToken()
    {
        return JsonToken.START_OBJECT;
    }

    @Override
    public int size()
    {
        return _values.size();
    }

    @Override
    public boolean isContainerNode()
    {
        return true;
    }

    @Override
    public boolean isObject()
    {
        return true;
    }

    @Override
    public Iterator<String> fieldNames()
    {
        return _values.keySet().iterator();
    }

    @Override
    public TreeNode get(String name)
    {
        return _values.containsKey(name) ? _values.get(name) : null;
    }

    @Override
    public TreeNode path(String name)
    {
        return _values.containsKey(name) ? _values.get(name) : MISSING;
    }
}
