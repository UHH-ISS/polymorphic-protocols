package com.uhh.code.polymorph;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import com.google.protobuf.DescriptorProtos;
import com.github.os72.protocjar.Protoc;
import com.squareup.javapoet.*;
import com.uhh.code.polymorph.protobuilder.ProtoBuilder;
import com.uhh.code.polymorph.transformators.MessageTransformator;

import javax.lang.model.element.Modifier;

// The class for the Transformator which orchestrates all specific Transformations
public class Transformator {

    boolean ignoreTransformations;
    Random rnd;
    MessageTransformator messageTransformator;
    TransformationResult apiTransformationResult;
    String packageName;
    Integer dummyFieldAmount;

    public Transformator(Random rnd) {
        this.rnd = rnd;
        this.messageTransformator = new MessageTransformator(this.rnd);
        this.apiTransformationResult = new TransformationResult(0, 0);
    }

    // method for generating a java wrapper
    public static TypeSpec.Builder CreateGenericJavaWrapper(String javaPackage, DescriptorProtos.DescriptorProto descriptorProto, String protoName, boolean ignoreTransformations, Random rnd)
    {
        // get all the names of the wrappers and the message
        var messageName = descriptorProto.getName();
        var wrappedMessageName =  protoName + "." + messageName;
        var wrappedMessageBuilderName =  protoName + "." + messageName + ".Builder";

        // get the typed class names
        var className = ClassName.get(javaPackage, wrappedMessageName);
        var originalBuilderClassName = ClassName.get(javaPackage, wrappedMessageBuilderName);
        var newClassName = ClassName.get(javaPackage, messageName);

        // create the class for the message
        var messageClass = TypeSpec.classBuilder(messageName)
                .addModifiers(Modifier.PUBLIC);


        // method for creating a new protobuilder
        var wrapperNewBuildSpec = MethodSpec.methodBuilder("newBuilder")
                .returns(newClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("var newMsg = new $T()", newClassName)
                .addStatement("newMsg.wrappedMessage = $T.newBuilder()", className)
                .addStatement("return newMsg")
                .build();

        // method for building a message
        var wrapperBuildSpec = MethodSpec.methodBuilder("build")
                .returns(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("wrappedMessage.build()")
                .addStatement("return this")
                .build();

        // method for clearing a message
        var wrapperClearSpec = MethodSpec.methodBuilder("clear")
                .returns(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("wrappedMessage.clear()")
                .addStatement("return this")
                .build();

        // method for converting a message to a string
        var wrapperToString = MethodSpec.methodBuilder("toString")
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addCode("return wrappedMessage.toString();")
                .build();

        // add a field for the wrappedMessage (identifier this.wrappedMessage)
        messageClass.addField(originalBuilderClassName, "wrappedMessage", Modifier.PUBLIC);

        if (!ignoreTransformations)
        {
            var stringLen = 32;

            if (rnd.nextBoolean())
            {
                stringLen = 24;
            }

            var rndString = new RandomStringGenerator(rnd);

            var encryptionKey = rndString.generateString(stringLen);

            // add fields to allow for potentially hashing data
            messageClass.addField(java.security.MessageDigest.class, "SHA1", Modifier.STATIC);
            messageClass.addField(java.security.MessageDigest.class, "SHA256", Modifier.STATIC);
            messageClass.addField(java.security.MessageDigest.class, "MD5", Modifier.STATIC);

            // add fields to allow for potentially encrypting/decrypt data
            messageClass.addField(javax.crypto.Cipher.class, "AESEncrypt", Modifier.STATIC);
            messageClass.addField(javax.crypto.Cipher.class, "AESDecrypt", Modifier.STATIC).addStaticBlock(
                    CodeBlock.builder().beginControlFlow("try")
                            .addStatement("AESEncrypt = Cipher.getInstance(\"AES/ECB/PKCS5Padding\")")
                            .addStatement("AESDecrypt = javax.crypto.Cipher.getInstance(\"AES/ECB/PKCS5Padding\")")
                            .addStatement("SHA1 = java.security.MessageDigest.getInstance(\"SHA-1\")")
                            .addStatement("SHA1.reset()")
                            .addStatement("SHA256 = java.security.MessageDigest.getInstance(\"SHA-256\")")
                            .addStatement("SHA256.reset()")
                            .addStatement("MD5 = java.security.MessageDigest.getInstance(\"MD5\")")
                            .addStatement("MD5.reset()")
                            .addStatement("AESEncrypt.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(\"" + encryptionKey + "\".getBytes(), \"AES\"))")
                            .addStatement("AESDecrypt.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(\"" + encryptionKey + "\".getBytes(), \"AES\"))")
                            .nextControlFlow("catch ($T e)", GeneralSecurityException.class)
                            .addStatement("e.printStackTrace()")
                            .endControlFlow()
                            .build()
            );
        }

        // add all the methods
        messageClass.addMethod(wrapperBuildSpec);
        messageClass.addMethod(wrapperNewBuildSpec);
        messageClass.addMethod(wrapperClearSpec);
        messageClass.addMethod(wrapperToString);

        return messageClass;
    }

    // method for transforming a proto
    private TransformationResult TransformProto(DescriptorProtos.FileDescriptorProto file, String protoName, HashMap<String, String> javaFiles, HashMap<String, String> protoFiles)
    {
        // create a emptry transformation result
        var protoTransformResult = new TransformationResult(0, 0);

        // create a new protobuilder
        var protoClass = new ProtoBuilder(file.getName());

        if (packageName != null)
        {
            protoClass.setPackageName(packageName);
        }

        // go through messages
        for(var descriptorProto : file.getMessageTypeList())
        {
            String javaPackage = "com.autogeneratedby.polymorph";

            if (this.packageName != null)
            {
                javaPackage = this.packageName.toLowerCase();
            }

            System.out.println(descriptorProto.getName() + " has " + descriptorProto.getFieldList().size() + " fields");

            // create a not yet filled out wrapper for that message
            var javaWrapper = CreateGenericJavaWrapper(javaPackage, descriptorProto, protoName, this.ignoreTransformations, this.rnd);
            // create a not yet filled out wrapper for that message
            var messageProtoClass = protoClass.createProto(descriptorProto.getName());

            // transform the message
            var result = messageTransformator.TransformFields(javaWrapper, messageProtoClass, descriptorProto, protoName);
            System.out.println("result: " + result.newDataTypes.size());

            // add the result of transformation to all results
            protoTransformResult.AddTransformationResult(result);
            System.out.println("protoTransformResult: " + protoTransformResult.newDataTypes.size());

            // build its java contents
            var javaWrapperContents = JavaFile.builder(javaPackage, javaWrapper.build()).build();

            // put it in a java file, messagename.java
            javaFiles.put(descriptorProto.getName() + ".java", javaWrapperContents.toString());
        }

        // also build the new proto
        var protoFile = protoClass.build();

        // put it in a proto file, name same as original
        protoFiles.put(file.getName(), protoFile);

        // return transformation result
        return protoTransformResult;
    }

    // method for generating java wrappers and protos
    public Pair<HashMap<String, String>, HashMap<String, String>> GenerateJavaWrappersAndProtoFiles(String outDir, String path, String absoluteFilename, String relativeFilename)
    {
        try {
            // parse the proto
            var fileDescriptorSet = Parser.ParseProto(outDir, path, absoluteFilename, relativeFilename);

            // maps for filenames and filecontents for java and proto files
            var javaFiles = new HashMap<String, String>();
            var protoFiles = new HashMap<String, String>();

            // go through all of the files within this API-Description
            for(var fileDescriptor : fileDescriptorSet.getFileList())
            {
                // get the name of the proto
                var protoName = fileDescriptor.getName();

                // name is usually protoname.proto, lets look for the dot
                var dotTerminatorIndex = protoName.indexOf(".");

                if (dotTerminatorIndex > 0)
                {
                    // remove the file extension
                    protoName = protoName.substring(0, dotTerminatorIndex);
                }

                // upercase the first character, then we have the actual name of the proto
                protoName = StringHelpers.UppercaseFirstCharacter(protoName);
                System.out.println("Going to transform: " + fileDescriptor.getName());

                // transform the proto
                var result = TransformProto(fileDescriptor, protoName, javaFiles, protoFiles);
                apiTransformationResult.AddTransformationResult(result);
            }

            var allFileSize = 0;
            for(var fileName : javaFiles.keySet())
            {
                var contents = javaFiles.get(fileName);
                allFileSize += contents.length();
            }

            for(var fileName : protoFiles.keySet())
            {
                var contents = protoFiles.get(fileName);
                allFileSize += contents.length();
            }

            apiTransformationResult.programSize += allFileSize;

            // return the result as pair of javafiles and protofiles
            return new Pair<>(javaFiles, protoFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // method for transforming a specific file
    public void TransformFile(String outDir, String path, String absoluteFilename, String relativeFilename, String rootPath)
    {
        // generate all java and proto files
        var javaAndProtoFiles = GenerateJavaWrappersAndProtoFiles(outDir, path, absoluteFilename, relativeFilename);
        var javaFiles = javaAndProtoFiles.first();
        var protoFiles = javaAndProtoFiles.second();

        String packagePath = "";

        if (packageName != null)
        {
            packagePath = packageName.replaceAll("\\.", "/") + "/";
        }

        // create a output directory
        File directory = new File(outDir + packagePath);

        if (!directory.exists())
        {
            directory.mkdirs();
        }

        // for each  javafile
        for (var fileName : javaFiles.keySet())
        {
            System.out.println("JavaFileName: " + fileName);

            // write its contents with into output directory with specified filename
            try {
                var newFile = new FileWriter(outDir + packagePath + "/" + fileName );

                var contents = javaFiles.get(fileName);
                newFile.write(contents);
                newFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // for each protofile
        for (var fileName : protoFiles.keySet())
        {
            // write its contents with into output directory with specified filename
            try {
                System.out.println("ProtoFileName: " + fileName);
                var newFile = new FileWriter(outDir + packagePath + "/" + fileName );

                var contents = protoFiles.get(fileName);
                newFile.write(contents);
                newFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Compile all the protos using protoc
        for (var fileName : protoFiles.keySet())
        {
            String[] args = { "--proto_path", outDir +  packagePath , "--java_out=" + outDir , fileName};
            try {
                Protoc.runProtoc(args);
                var compiledProtoName = StringHelpers.UppercaseFirstCharacter(fileName).replaceFirst(".proto", ".java");

                var compiledProtoPath = Paths.get(outDir + packagePath + compiledProtoName);
                var compiledProtoSize = Files.size(compiledProtoPath);
                apiTransformationResult.programSize += compiledProtoSize;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // if passthrough then also set the fields to reflect that
    public void SetIgnoreTransformations(boolean ignoreTransformations)
    {
        this.ignoreTransformations = ignoreTransformations;
        this.messageTransformator.SetIgnoreTransformations(this.ignoreTransformations);

    }

    // if package name is desired, use it
    public void SetPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    // allow setting fixed dummy field amounts
    public void SetDummyFieldAmount(Integer amount)
    {
        this.dummyFieldAmount = amount;
        this.messageTransformator.SetDummyFieldAmount(amount);
    }
}
