package com.uhh.code.polymorph.protobuilder;

// Class for ProtoMessages Fields
public class ProtoFieldValue {
    // Users may define a custom type
    private final String customType;
    // The actual type of the field
    private final ProtoType type;
    // Incase user decides to add a comment
    private String comment;

    ProtoFieldValue(ProtoType type)
    {
        // Assign the type information
        this.type = type;
        this.customType = null;
    }

    public ProtoFieldValue(String customType, ProtoType type)
    {
        // Assign the type information
        this.type = type;
        this.customType = customType;
    }

    public ProtoType getType()
    {
        return this.type;
    }

    public String getComment() { return this.comment; }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    protected String build(int index, String identifier)
    {
        var build = new StringBuilder();

        // If the user defined a type, that type takes priority
        if (this.customType != null)
        {
            build.append(this.customType);
        }else{
            build.append(getType().toString());
        }

        // add the identifier and index
        build.append(" ");
        build.append(identifier);
        build.append(" = ");
        build.append(index);
        build.append(";");

        // Incase the user defined a comment, add it
        if (this.comment != null && !this.comment.isEmpty() && !this.comment.isBlank())
        {
            build.append(" /* ");
            build.append(this.comment);
            build.append(" */");
        }

        return build.toString();
    }
}
