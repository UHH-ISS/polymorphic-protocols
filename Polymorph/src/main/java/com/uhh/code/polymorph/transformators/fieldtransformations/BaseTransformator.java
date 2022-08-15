package com.uhh.code.polymorph.transformators.fieldtransformations;

import com.squareup.javapoet.MethodSpec;
import com.uhh.code.polymorph.TypeMaximumValueString;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.protobuilder.ProtoTypeJavaTypeMap;
import com.uhh.code.polymorph.TransformationResult;

import java.util.Random;

// Base interface for transformators
public abstract class BaseTransformator {

    // The prototype and reflectiontype are required for the java codegen and protogen
    ProtoType originalProtoType;
    java.lang.reflect.Type reflectionType;
    // The rnd is required to decide which transformations shall be performed
    Random rnd;
    // Passthrough mode support
    boolean passThrough;

    public BaseTransformator(Random rnd, ProtoType originalProtoType) {
        this.rnd = rnd;
        this.originalProtoType = originalProtoType;
        // We automatically infer the reflectionType based on the protoType
        this.reflectionType = ProtoTypeJavaTypeMap.ReflectionMap.get(originalProtoType);
    }

    public java.lang.reflect.Type getReflectionType()
    {
        return this.reflectionType;
    }

    public abstract TransformationResult Transform(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder);

    protected TransformationResult TransformToIdentical(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        // Incase we desire to only Wrap without modification, we can simply call the original get/set functions
        wrapperSetSpecBuilder
                .addStatement("wrappedMessage.set" + camelCasedName + "(" + pascalCasedName + ")");

        wrapperGetSpecBuilder
                .addStatement("return wrappedMessage.get" + camelCasedName + "()");

        // Add the same field to the proto and indiciate its unmodified
        protoMessage.addField(this.originalProtoType, fieldName, "type unmodified");

        // Return the result of this transformation
        return new TransformationResult(this.originalProtoType, 1, 0);
    }

    protected TransformationResult TransformToString(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, String castToFnStr, MethodSpec.Builder wrapperSetSpecBuilder,  MethodSpec.Builder wrapperGetSpecBuilder)
    {
        // When converting toString, one only has to call String.valueOf
        wrapperSetSpecBuilder
                .addStatement("wrappedMessage.set" + camelCasedName + "(String.valueOf(" + pascalCasedName + "))");

        // When converting back from a string, one needs to call the proper castFn i.e. Integer.parseInt
        wrapperGetSpecBuilder
                .addStatement("return " + castToFnStr + "(wrappedMessage.get" + camelCasedName + "())");

        // add new string to the new proto
        protoMessage.addField(ProtoType.TYPE_STRING, fieldName, "type before: " + this.originalProtoType);

        // estimate the maximum size this type can take as a string
        var typeMaxAsString = TypeMaximumValueString.getMaximumLengthString(this.reflectionType);

        // return the transformationresult, include the maximum size there
        return new TransformationResult(ProtoType.TYPE_STRING, typeMaxAsString.length(), 1);
    }

    protected TransformationResult TransformToNegative(ProtoMessage protoMessage, String fieldName, String camelCasedName, String pascalCasedName, MethodSpec.Builder wrapperSetSpecBuilder, MethodSpec.Builder wrapperGetSpecBuilder)
    {
        // When transforming a numerical to a negative one only needs to prepend a -
        // To reverse this, the same thing ;)
        wrapperSetSpecBuilder.addStatement("wrappedMessage.set" + camelCasedName + "(-" + pascalCasedName + ")");
        wrapperGetSpecBuilder.addStatement("return -(wrappedMessage.get" + camelCasedName + "())");

        // Put the information that we changed the representation into the proto
        protoMessage.addField(this.originalProtoType, fieldName, "type before: " + this.originalProtoType + " | representation: negative number");

        // Return this as transformationresult, length is still 1
        return new TransformationResult(this.originalProtoType, 1,1);
    }

    public void SetPassThrough(boolean passThrough)
    {
        this.passThrough = passThrough;
    }
}
