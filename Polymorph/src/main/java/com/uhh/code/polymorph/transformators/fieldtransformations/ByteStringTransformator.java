package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

public class ByteStringTransformator extends BaseTransformator {

    public ByteStringTransformator(Random rnd) {
        super(rnd, ProtoType.TYPE_BYTES);
    }

    @Override
    public TransformationResult Transform(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        TransformationResult result = null;

        var chosenTransformation = this.rnd.nextInt(3);

        if (this.passThrough)
        {
            chosenTransformation = 0;
        }


        switch (chosenTransformation)
        {
            case 0 -> result = super.TransformToIdentical(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 1 -> result = TransformToPrepend(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
            case 2 -> result = TransformToSplit(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        }

        return result;
    }

    public TransformationResult TransformToPrepend(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        wrapperSetSpecBuilder.addStatement("var prependOut = ByteString.newOutput()")
                .addStatement("prependOut.write(0xFF)")
                .addStatement("var prepend = prependOut.toByteString()")
                .addStatement("wrappedMessage.set" + camelCasedName + "(prepend.concat(" + pascalCasedName + "))")
                .build();

        wrapperGetSpecBuilder.addStatement("return wrappedMessage.get" + camelCasedName + "().substring(1)");

        protoMessage.addField(this.originalProtoType, fieldName, "type before: " + this.originalProtoType + " | representation: prepended string");

        return new TransformationResult(ProtoType.TYPE_BYTES, 1, 1);
    }

    public TransformationResult TransformToSplit(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        wrapperSetSpecBuilder.addStatement("wrappedMessage.set" + camelCasedName + "P1(" + pascalCasedName + ".substring(" + pascalCasedName + ".size() / 2))")
                .addStatement("wrappedMessage.set" + camelCasedName + "P2(" + pascalCasedName + ".substring(0, " + pascalCasedName + ".size() / 2))")
                .build();

        wrapperGetSpecBuilder.addStatement("return (wrappedMessage.get" + camelCasedName + "P2().concat(wrappedMessage.get" + camelCasedName + "P1()))");

        protoMessage.addField(this.originalProtoType, fieldName + "p1", "type before: " + this.originalProtoType + " | representation: split bytes");
        protoMessage.addField(this.originalProtoType, fieldName + "p2", "type before: " + this.originalProtoType + " | representation: split bytes");

        return new TransformationResult(ProtoType.TYPE_BYTES, 2, 1);
    }
}
