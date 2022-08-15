package com.uhh.code.polymorph.protobuilder;

// This class models the index for fields
// It allows one to retroatively change indexes, which may be used by transformators
// Interface is comparable to allow for usage of i.e. SortedMaps
public class ProtoIndex implements Comparable<ProtoIndex>
{
    private int index;

    ProtoIndex(int index)
    {
        this.index = index;
    }

    public void SetIndex(int index)
    {
        this.index = index;
    }

    public int GetIndex()
    {
        return this.index;
    }

    // If the others index is larger then it should come before us
    @Override
    public int compareTo(ProtoIndex otherIndex) {

        return this.GetIndex() > otherIndex.GetIndex() ? 1 : 0;
    }
}