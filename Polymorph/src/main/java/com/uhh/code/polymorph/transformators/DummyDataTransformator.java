package com.uhh.code.polymorph.transformators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.uhh.code.polymorph.StringHelpers;
import com.uhh.code.polymorph.TransformationResult;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;
import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.protobuilder.ProtoTypeSizeEstimator;

import javax.lang.model.element.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// Transformator for adding Dummy Fields
// might also be possible to turn dummy data into checks, i.e. always assign a certain value, if data isn't set to certain value then trash the entire message
class DummyDataTransformator {
    private Random rnd;
    private boolean ignoreTransformations;
    private Integer fixedDummyFieldAmount;

    private DummyDataTransformator()
    {
    }

    public DummyDataTransformator(Random rnd)
    {
        this.rnd = rnd;
    }

    public TransformationResult PerformTransformation(TypeSpec.Builder javaWrapper, ProtoMessage protoMessage, boolean hashFields)
    {
        MethodSpec.Builder hashInit = null;

        if (hashFields)
        {
            hashInit = MethodSpec.methodBuilder("setFieldHashes")
                    .returns(void.class)
                    .addModifiers(Modifier.PUBLIC);
        }

        if (this.ignoreTransformations)
        {
            if (hashFields)
            {
                javaWrapper.addMethod(hashInit.build());
            }

            return new TransformationResult(0, 0);
        }

        // Decide amount of fields required
        var dummyFieldAmount = Math.max(1, rnd.nextInt(1 + protoMessage.getHighestIndex()));

        if (this.fixedDummyFieldAmount != null)
        {
            dummyFieldAmount = this.fixedDummyFieldAmount;
            System.out.println("Dummy field transformation amount is fixed to: " + this.fixedDummyFieldAmount);
        }

        System.out.println("Creating " + dummyFieldAmount + " dummy fields");



        // Only use primitive types as setting up dummy messages, groups and enums is more complex
        var protoTypes = ProtoType.values();
        var possibleTypes = new ArrayList<ProtoType>();

        for(var protoType : protoTypes)
        {
            if (protoType != ProtoType.TYPE_MESSAGE && protoType != ProtoType.TYPE_GROUP && protoType != ProtoType.TYPE_ENUM)
            {
                possibleTypes.add(protoType);
            }
        }

        var hashFieldNames = new ArrayList<String>();
        var transformationResult = new TransformationResult(0,0);
        var byteOverhead = 0;

        // Create the fields
        for (int i=0;i<dummyFieldAmount;i++)
        {
            // Always pick a random type for a given field
            var randomTypeIndex = this.rnd.nextInt(possibleTypes.size());
            var fieldType = possibleTypes.get(randomTypeIndex);

            // Name is not written over wire, thus used name does not matter
            var name = "dummy" + (i + 1);
            var comment = "dummydata";

            if (hashFields)
            {
                fieldType = ProtoType.TYPE_BYTES;
                name = "hash" + (i + 1);
                comment = "hash";
                hashFieldNames.add(name);
            }

            protoMessage.addField(fieldType, name, comment);
            transformationResult.newDataTypes.add(fieldType.toString());

            // Make sure to calculate the overhead
            // We always have a length of 1 because we never change the value, thus even i.e. strings and bytes are always empty
            byteOverhead = byteOverhead + ProtoTypeSizeEstimator.getPrimitiveSizeEstimate(fieldType, 1);
        }


        if (hashFields)
        {
            var hashAlgos = new ArrayList<String>();
            hashAlgos.add("SHA1");
            hashAlgos.add("SHA256");
            hashAlgos.add("MD5");
            hashInit.beginControlFlow("try");

            for(var hash : hashFieldNames)
            {

                var hashAlgo = hashAlgos.get(this.rnd.nextInt(3));

                hashInit.addStatement(hashAlgo + ".reset()")
                        .addStatement(hashAlgo + ".update(java.util.UUID.randomUUID().toString().getBytes())")
                        .addStatement("wrappedMessage.set" + StringHelpers.UppercaseFirstCharacter(hash) + "(com.google.protobuf.ByteString.copyFrom(" + hashAlgo + ".digest()))" );

            }
            hashInit.nextControlFlow("catch ($T e)", Exception.class)
                    .addStatement("e.printStackTrace()")
                    .endControlFlow();

            javaWrapper.addMethod(hashInit.build());
        }

        transformationResult.size = byteOverhead;
        transformationResult.transformationCount = 1;

        return transformationResult;
    }

    public void SetIgnoreTransformations(boolean ignoreTransformations)
    {
        this.ignoreTransformations = ignoreTransformations;
    }

    public void SetDummyFieldAmount(Integer amount)
    {
        this.fixedDummyFieldAmount = amount;
    }
}
