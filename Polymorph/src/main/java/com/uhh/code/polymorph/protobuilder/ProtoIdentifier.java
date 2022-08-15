package com.uhh.code.polymorph.protobuilder;

// This class shall allow modification of Identifiers incase we desire transforming them later on
// As Protobuf does not send them over wire, this would only have a visual effect
public class ProtoIdentifier
{
    private String identifier;

    ProtoIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public void SetIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String GetIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String toString()
    {
        return this.GetIdentifier();
    }
}