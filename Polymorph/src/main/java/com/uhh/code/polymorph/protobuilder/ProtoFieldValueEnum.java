package com.uhh.code.polymorph.protobuilder;

import java.util.SortedMap;
import java.util.TreeMap;

// Class for enumerations
// This class models full enum i.e. enum SomeEnum { field1 = 1 [...]
public class ProtoFieldValueEnum extends ProtoFieldValue {

    public ProtoFieldValueEnum()
    {
        // Register as a enum
        super(ProtoType.TYPE_ENUM);
    }

    // Map for the enumerations numbers and values
    SortedMap<Integer, String> NumberIdentifier = new TreeMap<>();

    // Add new number
    public void addNum(String identifier, Integer number)
    {
        NumberIdentifier.put(number, identifier);
    }

    @Override
    protected String build(int index, String identifier)
    {
        var build = new StringBuilder();

        // Add to string enum identifier {
        build.append(getType().toString());
        build.append(" ");
        build.append(identifier);
        build.append(" {\n");

        // For each mapping add \t\t identifier = numberBelongingToThatIdentifier;\n
        for(var number : NumberIdentifier.keySet())
        {
            // The identifier mapped to that number
            var numbersIdentifier = NumberIdentifier.get(number);

            build.append("\t\t");
            build.append(numbersIdentifier);
            build.append(" = ");
            build.append(number);
            build.append(";");
            build.append("\n");
        }

        // Close enum with }
        build.append("\t}");

        return build.toString();
    }
}
