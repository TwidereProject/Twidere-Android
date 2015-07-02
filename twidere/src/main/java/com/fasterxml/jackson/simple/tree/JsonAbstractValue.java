package com.fasterxml.jackson.simple.tree;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;

import java.util.Iterator;

abstract class JsonAbstractValue implements TreeNode
{
    @Override
    public JsonParser.NumberType numberType() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isValueNode() {
        return false;
    }

    @Override
    public boolean isContainerNode() {
        return false;
    }

    @Override
    public boolean isMissingNode() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public TreeNode get(String s) {
        return null;
    }

    @Override
    public TreeNode get(int i) {
        return null;
    }

    @Override
    public TreeNode path(String s) {
        return MISSING;
    }

    @Override
    public TreeNode path(int i) {
        return MISSING;
    }

    @Override
    public Iterator<String> fieldNames() {
        return null;
    }

    @Override
    public TreeNode at(JsonPointer jsonPointer) {
        return null;
    }

    @Override
    public TreeNode at(String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public JsonParser traverse() {
        return null;
    }

    @Override
    public JsonParser traverse(ObjectCodec objectCodec) {
        return null;
    }

    private static class JsonMissing extends JsonAbstractValue
    {
        @Override
        public JsonToken asToken()
        {
            return null;
        }

        @Override
        public boolean isMissingNode()
        {
            return true;
        }
    }

    protected static final TreeNode MISSING = new JsonMissing();
}
