package com.uhh.code.polymorph.transformators;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.uhh.code.polymorph.StringHelpers;
import com.uhh.code.polymorph.TransformationResult;
import com.uhh.code.polymorph.Transformator;
import com.uhh.code.polymorph.protobuilder.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Random;

// Transformator for Messages
public class MessageTransformator {
    // Fields for the sub transformators
    private final FieldTransformator fieldTransformator;
    private final DummyDataTransformator dummyDataTransformator;
    private final PermutationsTransformator permutationsTransformator;
    private final MessageEncodingTransformator encodingTransformator;

    private Random rnd;
    private boolean ignoreTransformations;

    public MessageTransformator(Random rnd)
    {
        // get the rnd instance
        this.rnd = rnd;

        //instantiate all the sub transformators
        fieldTransformator = new FieldTransformator(this.rnd);
        fieldTransformator.SetIgnoreTransformations(this.ignoreTransformations);
        permutationsTransformator = new PermutationsTransformator(this.rnd);
        dummyDataTransformator = new DummyDataTransformator(this.rnd);
        encodingTransformator = new MessageEncodingTransformator(this.rnd);
    }

    // Method for generating enum identifier { ident1 = value1 [...]
    private void TransformKeepEnumTypes(TypeSpec.Builder javaMessageBuilder, ProtoMessage protoMessage, String enumName, List<DescriptorProtos.EnumValueDescriptorProto> enumValues)
    {
        var fieldValue = new ProtoFieldValueEnum();

        for (var enumValue : enumValues)
        {
            fieldValue.addNum(enumValue.getName(), enumValue.getNumber());
        }

        protoMessage.addComplexField(fieldValue, enumName);
    }

    // Method for generating java wrappers for enums
    private void TransformKeepEnum(TypeSpec.Builder javaMessageBuilder, String protoName, String fieldName, String customIdentifier)
    {
        var camelCasedName = StringHelpers.UnderscoreCaseToPascalCase(fieldName);
        var pascalCasedName = StringHelpers.UnderscoreCaseToCamelCase(fieldName);

        var className = ClassName.get(customIdentifier, protoName + "." + customIdentifier);


        var wrapperSetSpec = MethodSpec.methodBuilder("set" + camelCasedName)
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(className, pascalCasedName)
                .addStatement("wrappedMessage.set" + camelCasedName + "(" + pascalCasedName + ")")
                .build();

        var wrapperGetSpec = MethodSpec.methodBuilder("get" + camelCasedName)
                .returns(className)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return wrappedMessage.get" + camelCasedName + "()")
                .build();

        javaMessageBuilder.addMethod(wrapperSetSpec);
        javaMessageBuilder.addMethod(wrapperGetSpec);
    }

    // method for transforming messages i.e. message ident { int32 test = [...]
    private void TransformKeepMessage(TypeSpec.Builder javaMessageBuilder, String protoName, String fieldName, String customIdentifier)
    {
        // get camel and pascal cased fieldname
        var camelCasedName = StringHelpers.UnderscoreCaseToPascalCase(fieldName);
        var pascalCasedName = StringHelpers.UnderscoreCaseToCamelCase(fieldName);

        // get typed name
        var className = ClassName.get(customIdentifier, customIdentifier);


        // create the set for the wrapped mesasge
        var wrapperSetSpec = MethodSpec.methodBuilder("set" + camelCasedName)
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(className, pascalCasedName)
                .addStatement("wrappedMessage.set" + camelCasedName + "(" + pascalCasedName + ".wrappedMessage)")
                .build();

        // create the get for the wrapped message
        var wrapperGetSpec = MethodSpec.methodBuilder("get" + camelCasedName)
                .returns(className)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("var protoMsg = wrappedMessage.get" + camelCasedName + "()")
                .beginControlFlow("if (protoMsg == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("var mergeMsg =  $N.newBuilder()", protoName + "." + customIdentifier)
                .addStatement("mergeMsg.mergeFrom((com.google.protobuf.Message)protoMsg)")
                .addStatement("var newMsg = new $N()", customIdentifier)
                .addStatement("newMsg.wrappedMessage = mergeMsg")
                .addStatement("return newMsg")
                .build();

        // add the get and the setspec
        javaMessageBuilder.addMethod(wrapperSetSpec);
        javaMessageBuilder.addMethod(wrapperGetSpec);
    }

    // method for transforming a messages fields
    public TransformationResult TransformFields(TypeSpec.Builder javaMessageBuilder, ProtoMessage protoMessage, DescriptorProto proto, String protoName)
    {
        // instantiate a new transformation result
        var fieldTransformatorResult = new TransformationResult(0, 0);

        // preserve the enum ident { definitions
        System.out.println("Preserving Enum Types");

        // get all enums from the enumtypelist
        for (var field : proto.getEnumTypeList())
        {
            // get name and values
            var enumName = field.getName();
            var enumValues = field.getValueList();
            // transform based on that information
            TransformKeepEnumTypes(javaMessageBuilder, protoMessage, enumName, enumValues);
        }

        System.out.println("Iterating through Nested Types");
        // get all messages / nested datatypes from getnestedtypelist
        for (var fieldProto : proto.getNestedTypeList())
        {
            // get the name of the field
            var fieldName = fieldProto.getName();

            // create a new proto message for it
            var newProtoMessage = new ProtoMessage(fieldName, protoMessage.getLevel() + 1);
            // create new wrappers for the new message
            var innerWrapper = Transformator.CreateGenericJavaWrapper(proto.getName(), fieldProto, protoName + "." + proto.getName(), this.ignoreTransformations, this.rnd);
            // it has to be static as java demands it for the subclasses, else syntax errors
            innerWrapper.addModifiers(Modifier.STATIC);

            // transform the submessage
            var transformResult = TransformFields(innerWrapper, newProtoMessage, fieldProto, protoName + "." + proto.getName());
            // add the transformationresult to ours
            fieldTransformatorResult.AddTransformationResult(transformResult);

            // add it to our message
            protoMessage.addComplexField(newProtoMessage, fieldName);
            javaMessageBuilder.addType(innerWrapper.build());
        }

        System.out.println("Transforming Field Types");

        // transform normal fields (primitive types or just reusing a message or enum type)
        for(var field : proto.getFieldList())
        {
            // get type and field name
            var fieldTypeName = field.getType().name();
            var fieldName = field.getName();
            System.out.println("Transforming field " + fieldName + " type: " + fieldTypeName);


            var isEnum = fieldTypeName.equals("TYPE_ENUM");
            var isMessage = fieldTypeName.equals("TYPE_MESSAGE") || fieldTypeName.equals("TYPE_GROUP");

            // if its a primitive datatype, we can transform it using the fieldtransformator
            if (!isEnum && !isMessage)
            {
                var result = fieldTransformator.PerformTransformation(javaMessageBuilder, protoMessage, fieldTypeName, fieldName);
                fieldTransformatorResult.AddTransformationResult(result);
                continue;
            }

            // if its not a primitive datatype, get the custom identifier
            var customIdentifier = field.getTypeName().substring(1);
            // create it as a custom fieldvalue
            var fieldValue = new ProtoFieldValue(customIdentifier, isEnum ? ProtoType.TYPE_ENUM : ProtoType.TYPE_MESSAGE);

            // add it to the proto
            protoMessage.addComplexField(fieldValue, fieldName);

            // add it to the java representation
            if (isEnum)
            {
                TransformKeepEnum(javaMessageBuilder, protoName, fieldName, customIdentifier);
            }else{
                TransformKeepMessage(javaMessageBuilder, protoName, fieldName, customIdentifier);
            }
        }


        // perform dummydata transformation
        var dummyTransformatorResult = dummyDataTransformator.PerformTransformation(javaMessageBuilder, protoMessage, false);
        // add dummydata which gets set to hashes
        var dummyHashTransformatorResult = dummyDataTransformator.PerformTransformation(javaMessageBuilder, protoMessage, true);
        // perform encoding transformation
        var encodingTransformatorResult = encodingTransformator.PerformTransformation(javaMessageBuilder, protoMessage, protoName);
        // also change permutations
        var permutationTransformatorResult = permutationsTransformator.PerformTransformation(protoMessage);

        fieldTransformatorResult.AddTransformationResult(dummyTransformatorResult);
        fieldTransformatorResult.AddTransformationResult(dummyHashTransformatorResult);
        fieldTransformatorResult.AddTransformationResult(permutationTransformatorResult);
        fieldTransformatorResult.AddTransformationResult(encodingTransformatorResult);

        return fieldTransformatorResult;
    }

    public void SetIgnoreTransformations(boolean ignoreTransformations)
    {
        this.ignoreTransformations = true;

        this.fieldTransformator.SetIgnoreTransformations(ignoreTransformations);
        this.dummyDataTransformator.SetIgnoreTransformations(ignoreTransformations);
        this.permutationsTransformator.SetIgnoreTransformations(ignoreTransformations);
        this.encodingTransformator.SetIgnoreTransformations(ignoreTransformations);
    }

    public void SetDummyFieldAmount(Integer amount)
    {
        this.dummyDataTransformator.SetDummyFieldAmount(amount);
    }
}
