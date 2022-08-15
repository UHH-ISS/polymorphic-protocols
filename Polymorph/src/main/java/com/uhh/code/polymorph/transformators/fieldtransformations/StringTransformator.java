package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

public class StringTransformator extends BaseTransformator {

    public StringTransformator(Random rnd) {
        super(rnd, ProtoType.TYPE_STRING);
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
            case 1 -> result = TransformToReverse(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 2 -> result = TransformToUTF32Bytes(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 3 -> result = TransformToSplit(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    public TransformationResult TransformToReverse(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        wrapperSetSpecBuilder.addStatement("wrappedMessage.set" + camelCasedName + "(new StringBuilder(" + pascalCasedName + ").reverse().toString())")
                .build();

        wrapperGetSpecBuilder.addStatement("return new StringBuilder(wrappedMessage.get" + camelCasedName + "()).reverse().toString()");

        protoMessage.addField(this.originalProtoType, fieldName, "type before: " + this.originalProtoType + " | representation: reversed string");

        return new TransformationResult(this.originalProtoType, 1,1);
    }

    public TransformationResult TransformToUTF32Bytes(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        wrapperSetSpecBuilder.beginControlFlow("try")
                .addStatement("wrappedMessage.set" + camelCasedName + "(com.google.protobuf.ByteString.copyFrom(" + pascalCasedName + ".getBytes(\"UTF-32\")))")
                .nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("e.printStackTrace()")
                .endControlFlow();

        wrapperGetSpecBuilder.beginControlFlow("try")
                .addStatement("return new String(wrappedMessage.get" + camelCasedName + "().toByteArray(), \"UTF-32\")")
                .nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("e.printStackTrace()")
                .endControlFlow()
                .addStatement("return null");

        // msgA = new authReply()
        // msgA.setBananaCount(3);
        // code -> funzt

        // msgA

        protoMessage.addField(ProtoType.TYPE_BYTES, fieldName, "type before: " + this.originalProtoType + " | representation: utf32 bytes");

        return new TransformationResult(ProtoType.TYPE_BYTES, 2, 1);
    }

    public TransformationResult TransformToSplit(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        wrapperSetSpecBuilder.addStatement("wrappedMessage.set" + camelCasedName + "p1(" + pascalCasedName + ".substring(" + pascalCasedName + ".length() / 2))")
                .addStatement("wrappedMessage.set" + camelCasedName + "p2(" + pascalCasedName + ".substring(0, " + pascalCasedName + ".length() / 2))")
                .build();

        wrapperGetSpecBuilder.addStatement("return wrappedMessage.get" + camelCasedName + "p2() + wrappedMessage.get" + camelCasedName + "p1()");

        protoMessage.addField(this.originalProtoType, fieldName + "p1", "type before: " + this.originalProtoType + " | representation: split string");
        protoMessage.addField(this.originalProtoType, fieldName + "p2", "type before: " + this.originalProtoType + " | representation: split string");

        return new TransformationResult(this.originalProtoType, 2, 1);
    }

}
