package com.uhh.code.polymorph.protobuilder;

// This class allows for estimating the worst case usage of any given type Proto3 supports
public class ProtoTypeSizeEstimator
{
        public static int getPrimitiveSizeEstimate(ProtoType protoType, int length)
        {
                length = Math.max(length, 1);

                // This is a estimate function based on the available data type information Google offers
                // If a type is of variable length, the worst case estimate is made (i.e. variable int which casts back to primitive data type int -> assume all bits are set -> largest varint size used)

                // Sizes are inferred from scalar type sizes: https://developers.google.com/protocol-buffers/docs/proto3#scalar
                // And encoding information: https://developers.google.com/protocol-buffers/docs/encoding

                var sizeEstimate = 0;

                switch(protoType)
                {
                        // These always have the same fixed size per definition
                        case TYPE_FIXED32, TYPE_SFIXED32, TYPE_FLOAT -> sizeEstimate = 4 * length;
                        case TYPE_FIXED64, TYPE_SFIXED64, TYPE_DOUBLE -> sizeEstimate = 8 * length;

                        // Bools are encoded as varint but are always either 1 or 0 thus 1 byte
                        case TYPE_BOOL -> sizeEstimate = 1 * length;

                        // Length essentially unbound due to the types nature
                        // But bytes = n byte && according to p
                        case TYPE_BYTES, TYPE_STRING -> sizeEstimate = 1 * length;


                        // encoding information states: "If you use int32 or int64 as the type for a negative number, the resulting varint is always ten bytes long"
                        // These are all encoded using the same VarInt encoding
                        case TYPE_INT32, TYPE_INT64, TYPE_UINT32, TYPE_UINT64 -> sizeEstimate = 10 * length;

                        // These are encoded using ZigZag encoding to allow packing negative numbers more efficiently
                        case TYPE_SINT32, TYPE_SINT64 -> sizeEstimate = 5 * length;
                        default -> throw new IllegalArgumentException(protoType.toString());
                }

                return sizeEstimate;
        }

}