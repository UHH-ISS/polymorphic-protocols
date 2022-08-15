package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

public class FloatTransformator extends BaseTransformator {

    public FloatTransformator(Random rnd, ProtoType protoType) {
        super(rnd, protoType);
    }

    @Override
    public TransformationResult Transform(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder, MethodSpec.Builder wrapperGetSpecBuilder)
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
            case 1 -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Float.parseFloat", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 2 -> result = super.TransformToNegative(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 3 -> result = TransformToSplit(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    public TransformationResult TransformToSplit(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder, MethodSpec.Builder wrapperGetSpecBuilder)
    {
        var isDouble = this.originalProtoType == ProtoType.TYPE_DOUBLE;

        var castToFnStr =  "Float.floatToIntBits";
        var castFromFnStr =  "Float.intBitsToFloat";
        var shifts = "16";
        var bitMask = "0xFFFF";
        var intType = ProtoType.TYPE_INT32;

        if (isDouble)
        {
            castToFnStr = "Double.doubleToLongBits";
            castFromFnStr = "Double.longBitsToDouble";

            shifts = "32";
            bitMask = "0xFFFFFFFFL";
            intType = ProtoType.TYPE_INT64;
        }

        wrapperSetSpecBuilder
                .addStatement("var asInt = " + castToFnStr + "(" + pascalCasedName + ")")
                .addStatement("wrappedMessage.set" + camelCasedName + "P1" + "((int)asInt >> " + shifts + ")")
                .addStatement("wrappedMessage.set" + camelCasedName + "P2" + "((int)asInt & " + bitMask + ")")
                .build();

        wrapperGetSpecBuilder
                .addStatement("var asInt = (wrappedMessage.get" + camelCasedName + "p1() << " + shifts + " | (wrappedMessage.get" + camelCasedName + "p2()) & " + bitMask + ")")
                .addStatement("return " + castFromFnStr + "(asInt)");



        protoMessage.addField(intType, fieldName + "p1", "type before: " + this.originalProtoType + " | representation: split int float bits");
        protoMessage.addField(intType, fieldName + "p2", "type before: " + this.originalProtoType + " | representation: split int float bits");

        return new TransformationResult(intType, 2, 1);
    }

}
