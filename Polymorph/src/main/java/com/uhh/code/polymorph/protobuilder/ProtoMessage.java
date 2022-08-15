package com.uhh.code.polymorph.protobuilder;

import com.uhh.code.polymorph.Pair;

import java.util.*;

public class ProtoMessage extends ProtoFieldValue {
    private final String name;
    private Map<ProtoIndex, Pair<ProtoIdentifier, ProtoFieldValue>> messageFields = new HashMap<>();
    private int highestIndex = 0;
    private int level = 0;

    public ProtoMessage(String name)
    {
        super(ProtoType.TYPE_MESSAGE);
        this.name = name;
    }

    public ProtoMessage(String name, int level)
    {
        super(ProtoType.TYPE_MESSAGE);
        this.name = name;
        this.level = level;
    }

    public String getName()
    {
        return this.name;
    }

    public int getHighestIndex()
    {
        return this.highestIndex;
    }

    public int getLevel() { return this.level; }

    public ProtoMessage addField(ProtoType type, String name, String comment)
    {
        if (type == ProtoType.TYPE_ENUM || type == ProtoType.TYPE_GROUP || type == ProtoType.TYPE_MESSAGE)
        {
            throw new IllegalArgumentException(type.toString());
        }

        highestIndex++;

        var protoIndex = new ProtoIndex(highestIndex);
        var protoIdentifier = new ProtoIdentifier(name);
        var protoFieldValue = new ProtoFieldValue(type);
        protoFieldValue.setComment(comment);

        var identifierTypePair = new Pair<>(protoIdentifier, protoFieldValue);
        messageFields.put(protoIndex, identifierTypePair);

        return this;
    }

    public ProtoMessage addField(ProtoType type, String name)
    {
        return addField(type, name, null);
    }

    public ProtoMessage addComplexField(ProtoFieldValue fieldValue, String name)
    {
        highestIndex++;

        var protoIndex = new ProtoIndex(highestIndex);
        var protoIdentifier = new ProtoIdentifier(name);

        var identifierTypePair = new Pair<>(protoIdentifier, fieldValue);
        messageFields.put(protoIndex, identifierTypePair);

        return this;
    }

    public Set<ProtoIndex> getIndexes()
    {
        return messageFields.keySet();
    }

    protected String build(int level)
    {
        System.out.println("Building message " + this.getName());
        var msg = new StringBuilder("message " + this.getName() + " {");
        msg.append(" // Level " + level);

        var comment =  this.getComment();

        if (comment != null && !comment.isEmpty())
        {
            msg.append(", ");
            msg.append(comment);
        }

        msg.append("\n");

        for(var protoIndex : messageFields.keySet())
        {
            var identifierTypePair = messageFields.get(protoIndex);
            var protoIdentifier = identifierTypePair.first();
            var protoFieldValue = identifierTypePair.second();

            if (level > 0)
            {
                msg.append("\t".repeat(level));
            }

            msg.append("\t");
            msg.append(protoFieldValue.build(protoIndex.GetIndex(), protoIdentifier.GetIdentifier()));
            msg.append("\n");
        }

        if (level > 0)
        {
            msg.append("\t".repeat(level));
        }

        msg.append("}");

        return msg.toString();
    }

    protected String build()
    {
        return build(this.getLevel());
    }

    @Override
    protected String build(int index, String identifier)
    {
        return build(this.getLevel());
    }
}
