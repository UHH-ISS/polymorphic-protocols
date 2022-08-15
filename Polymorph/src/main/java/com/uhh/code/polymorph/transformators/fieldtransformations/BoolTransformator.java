package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

public class BoolTransformator extends BaseTransformator {

    public BoolTransformator(Random rnd) {
        super(rnd, ProtoType.TYPE_BOOL);
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
            case 1 -> result = super.TransformToString(protoMessage, fieldName, camelCasedName, pascalCasedName, "Boolean.parseBoolean", wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 2 -> result = TransformToInt32(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 3 -> result = TransformToFloats(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    private TransformationResult TransformToInt32(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        var enabledNumber = 1 + rnd.nextInt(1000);

        wrapperSetSpecBuilder.addStatement("if ($N) {", pascalCasedName)
                .addStatement("wrappedMessage.set" + camelCasedName + "(" + enabledNumber + ")")
                .addStatement("}else{")
                .addStatement("wrappedMessage.set" + camelCasedName + "(0)")
                .addStatement("}")
                .build();
        wrapperGetSpecBuilder.addStatement("return wrappedMessage.get" + camelCasedName + "() == " + enabledNumber + " ? true : false");

        protoMessage.addField(ProtoType.TYPE_FIXED32, fieldName, "type before: bool");

        return new TransformationResult(ProtoType.TYPE_INT32, 1, 1);
    }

    private TransformationResult TransformToFloats(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        var enabledFloat = rnd.nextInt(300);

        wrapperSetSpecBuilder.beginControlFlow("if ($N)", pascalCasedName)
                .addStatement("wrappedMessage.set" + camelCasedName + "p1" + "(" + enabledFloat + ".f)")
                .addStatement("wrappedMessage.set" + camelCasedName + "p2" + "(-" + enabledFloat + ".f)")
                .nextControlFlow("else")
                .addStatement("wrappedMessage.set" + camelCasedName + "p1" + "(0)")
                .addStatement("wrappedMessage.set" + camelCasedName + "p2" + "(0)")
                .endControlFlow()
                .build();

        wrapperGetSpecBuilder.addStatement("return (wrappedMessage.get" + camelCasedName + "p1() + wrappedMessage.get" + camelCasedName + "p2()) == " + enabledFloat + " ? true : false");

        protoMessage.addField(ProtoType.TYPE_FLOAT, fieldName + "p1", "type before: bool | p1");
        protoMessage.addField(ProtoType.TYPE_FLOAT, fieldName + "p2", "type before: bool | p2");

        return new TransformationResult(ProtoType.TYPE_FLOAT, 2, 1);
    }
}
