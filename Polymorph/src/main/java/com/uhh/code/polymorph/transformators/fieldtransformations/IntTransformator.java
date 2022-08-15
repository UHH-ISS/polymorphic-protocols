package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

public class IntTransformator extends BaseTransformator
{
    public IntTransformator(Random rnd, ProtoType originalProtoType) {
        super(rnd, originalProtoType);
    }

    @Override
    public TransformationResult Transform(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        TransformationResult result = null;

        var chosenTransformation = this.rnd.nextInt(4);

        if (this.passThrough)
        {
            chosenTransformation = 0;
        }

        switch (chosenTransformation)
        {
            case 0 -> result = super.TransformToIdentical(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 1 -> result = TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 2 -> result = super.TransformToNegative(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 3 -> result = TransformToSplit(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    public TransformationResult TransformToString(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        TransformationResult result = null;

        switch(this.originalProtoType)
        {
            case TYPE_UINT32, TYPE_FIXED32 -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Integer.parseUnsignedInt", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case TYPE_UINT64, TYPE_FIXED64 -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Long.parseUnsignedLong", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case TYPE_INT32, TYPE_SFIXED32, TYPE_SINT32  -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Integer.parseInt", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case TYPE_INT64, TYPE_SFIXED64, TYPE_SINT64 -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Long.parseLong", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    public TransformationResult TransformToSplit(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        var is64Bits = false;

        switch(this.originalProtoType)
        {
            case TYPE_UINT64, TYPE_FIXED64, TYPE_SFIXED64, TYPE_INT64, TYPE_SINT64 -> is64Bits = true;
        }

        var shifts = "32";
        var bitMask = "0xFFFFFFFFL";

        if (!is64Bits)
        {
            shifts = "16";
            bitMask = "0xFFFF";
        }

        wrapperSetSpecBuilder.addStatement("wrappedMessage.set" + camelCasedName + "P1" + "((int)" + pascalCasedName + " >> " + shifts + ")")
                .addStatement("wrappedMessage.set" + camelCasedName + "P2" + "((int)" + pascalCasedName + " & " + bitMask + ")")
                .build();

        wrapperGetSpecBuilder.addStatement("return (wrappedMessage.get" + camelCasedName + "P1() << " + shifts + " | (wrappedMessage.get" + camelCasedName + "P2()) & " + bitMask + ")");

        protoMessage.addField(this.originalProtoType, fieldName + "p1", "type before: " + this.originalProtoType + " | p1");
        protoMessage.addField(this.originalProtoType, fieldName + "p2", "type before: " + this.originalProtoType + " | p2");

        return new TransformationResult(this.originalProtoType, 2, 1);
    }
}


