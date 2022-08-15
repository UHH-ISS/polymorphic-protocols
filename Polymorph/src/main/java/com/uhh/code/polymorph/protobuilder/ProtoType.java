package com.uhh.code.polymorph.protobuilder;

// This enumeration lists all Types which Protobuf supports
public enum ProtoType
{
        TYPE_BOOL,
        TYPE_BYTES,
        TYPE_DOUBLE,
        TYPE_ENUM,
        TYPE_FIXED32,
        TYPE_FIXED64,
        TYPE_FLOAT,
        TYPE_GROUP,
        TYPE_INT32,
        TYPE_INT64,
        TYPE_MESSAGE,
        TYPE_SFIXED32,
        TYPE_SFIXED64,
        TYPE_SINT32,
        TYPE_SINT64,
        TYPE_STRING,
        TYPE_UINT32,
        TYPE_UINT64;

        // Tostring casts it to its proto representation
        @Override
        public String toString()
        {
                switch(this)
                {
                        case TYPE_BOOL:
                                return "bool";
                        case TYPE_BYTES:
                                return "bytes";
                        case TYPE_DOUBLE:
                                return "double";
                        case TYPE_ENUM:
                                return "enum";
                        case TYPE_FIXED32:
                                return "fixed32";
                        case TYPE_FIXED64:
                                return "fixed64";
                        case TYPE_FLOAT:
                                return "float";
                        case TYPE_GROUP:
                                return "group check whether this type is even used";
                        case TYPE_INT32:
                                return "int32";
                        case TYPE_INT64:
                                return "int64";
                        case TYPE_MESSAGE:
                                return "message";
                        case TYPE_SFIXED32:
                                return "sfixed32";
                        case TYPE_SFIXED64:
                                return "sfixed64";
                        case TYPE_SINT32:
                                return "sint32";
                        case TYPE_SINT64:
                                return "sint64";
                        case TYPE_STRING:
                                return "string";
                        case TYPE_UINT32:
                                return "uint32";
                        case TYPE_UINT64:
                                return "uint64";
                        default:
                                throw new IllegalArgumentException();
                }
        }
}