package com.fasterxml.jackson.simple.tree;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;

public class JsonArray extends JsonAbstractValue
{
    private final List<TreeNode> values;

    public JsonArray()
    {
        this(Collections.<TreeNode>emptyList());
    }

    public JsonArray(List<TreeNode> values)
    {
        this.values = Collections.unmodifiableList(values);
    }

    @Override
    public JsonToken asToken()
    {
        return START_ARRAY;
    }

    @Override
    public int size()
    {
        return values.size();
    }

    @Override
    public boolean isContainerNode()
    {
        return true;
    }

    @Override
    public boolean isArray()
    {
        return true;
    }

    @Override
    public TreeNode get(int i)
    {
        return 0 <= i && i < values.size() ? values.get(i) : null;
    }

    @Override
    public TreeNode path(int i)
    {
        return 0 <= i && i < values.size() ? values.get(i) : MISSING;
    }
}
