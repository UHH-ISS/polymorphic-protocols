package com.uhh.code.polymorph.protobuilder;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;

import static com.uhh.code.polymorph.protobuilder.ProtoType.*;

// This class maps the proto representations to primitive datatypes
public final class ProtoTypeJavaTypeMap
{
        static final public Map<ProtoType, java.lang.reflect.Type> ReflectionMap = new HashMap<>()
        {
                {
                        put(TYPE_BOOL, boolean.class);
                        put(TYPE_FLOAT, float.class);
                        put(TYPE_DOUBLE, double.class);
                        put(TYPE_BYTES, ByteString.class);
                        put(TYPE_FIXED32, int.class);
                        put(TYPE_FIXED64, long.class);
                        put(TYPE_INT32, int.class);
                        put(TYPE_INT64, long.class);
                        put(TYPE_SFIXED32, int.class);
                        put(TYPE_SFIXED64, long.class);
                        put(TYPE_SINT32, int.class);
                        put(TYPE_SINT64, long.class);
                        put(TYPE_STRING, String.class);
                        put(TYPE_UINT32, int.class);
                        put(TYPE_UINT64, long.class);
                }
        };
}