package com.uhh.code.polymorph.transformators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.uhh.code.polymorph.StringHelpers;
import com.uhh.code.polymorph.TransformationResult;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.transformators.fieldtransformations.*;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.uhh.code.polymorph.protobuilder.ProtoType.*;

class FieldTransformator {

    private Random rnd;
    private Map<String, BaseTransformator> transformationMap = new HashMap<>();
    private boolean ignoreTransformations;
    private Integer dummyFieldAmount;

    private FieldTransformator()
    {
    }

    public FieldTransformator(Random rnd)
    {
        this.rnd = rnd;

        transformationMap.put("TYPE_BOOL", new BoolTransformator(rnd));
        transformationMap.put("TYPE_BYTES", new ByteStringTransformator(rnd));
        transformationMap.put("TYPE_STRING", new StringTransformator(rnd));


        transformationMap.put("TYPE_FLOAT", new FloatTransformator(rnd, TYPE_FLOAT));
        transformationMap.put("TYPE_DOUBLE", new FloatTransformator(rnd, TYPE_DOUBLE));

        transformationMap.put("TYPE_FIXED32", new IntTransformator(rnd, TYPE_FIXED32));
        transformationMap.put("TYPE_FIXED64", new IntTransformator(rnd, TYPE_FIXED64));
        transformationMap.put("TYPE_SFIXED32", new IntTransformator(rnd, TYPE_SFIXED32));
        transformationMap.put("TYPE_SFIXED64", new IntTransformator(rnd, TYPE_SFIXED64));

        transformationMap.put("TYPE_INT32", new IntTransformator(rnd, TYPE_INT32));
        transformationMap.put("TYPE_INT64", new IntTransformator(rnd, TYPE_INT64));
        transformationMap.put("TYPE_UINT32", new IntTransformator(rnd, TYPE_UINT32));
        transformationMap.put("TYPE_UINT64", new IntTransformator(rnd, TYPE_UINT64));
        transformationMap.put("TYPE_SINT32", new IntTransformator(rnd, TYPE_SINT32));
        transformationMap.put("TYPE_SINT64", new IntTransformator(rnd, TYPE_SINT64));
    }

    public TransformationResult PerformTransformation(TypeSpec.Builder javaMessageBuilder, ProtoMessage protoMessage, String fieldType, String fieldName)
    {
        if (fieldType.equals( "TYPE_ENUM"))
        {
            return new TransformationResult(0, 0);
        }

        if (fieldType.equals("TYPE_GROUP") || fieldType.equals("TYPE_MESSAGE"))
        {
            return new TransformationResult(0, 0);
        }

        var transformator = transformationMap.get(fieldType);

        if (transformator == null)
        {
            throw new IllegalArgumentException(fieldType + ":" + fieldName);
        }
        // TODO: adjust the fieldName, I noticed that when names are i.e. Text1p2 protobufs turns it into Text1P2, while Textp2 stays Textp2
        // this is relevant for the splits, quality of life adjustment

        transformator.SetPassThrough(this.ignoreTransformations);

        var camelCasedName = StringHelpers.UnderscoreCaseToPascalCase(fieldName);
        var pascalCasedName = StringHelpers.UnderscoreCaseToCamelCase(fieldName);

        var wrapperSetSpecBuilder = MethodSpec.methodBuilder("set" + camelCasedName)
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(transformator.getReflectionType(), pascalCasedName);

        var wrapperGetSpecBuilder = MethodSpec.methodBuilder("get" + camelCasedName)
                .returns(transformator.getReflectionType())
                .addModifiers(Modifier.PUBLIC);

        transformator.SetPassThrough(this.ignoreTransformations);

        var transformationResult = transformator.Transform(protoMessage, fieldName, camelCasedName, pascalCasedName, wrapperSetSpecBuilder, wrapperGetSpecBuilder);
        transformationResult.previousDataTypes.add(ProtoType.valueOf(fieldType).toString());

        javaMessageBuilder.addMethod(wrapperSetSpecBuilder.build());
        javaMessageBuilder.addMethod(wrapperGetSpecBuilder.build());

        return transformationResult;
    }

    public void SetIgnoreTransformations(boolean ignoreTransformations)
    {
        this.ignoreTransformations = ignoreTransformations;
    }
}
