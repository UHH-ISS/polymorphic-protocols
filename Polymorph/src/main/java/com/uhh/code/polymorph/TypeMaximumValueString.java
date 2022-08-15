package com.uhh.code.polymorph;

import java.lang.reflect.Type;

// Class for calculating the maximum length of a given java type as a string i.e., a 8 bit integer wuold be -> "-127"
public class TypeMaximumValueString {

    // This function serves to deliver a string filled with the maximum length of a given java type using the data types class
    // The maximum length of a given java type = the biggest length in the string format it will have, usually on numbers their negative bound
    // This is useful for checking how large a string filled with data from that type can potentially get
    public static String getMaximumLengthString(Type type)
    {
        var maximumValueString = "";

        // Sadly java switches do not support reflection types, thus we gotta else if
        // For each numerical its the lowest possible number
        if (int.class.equals(type)) {
            maximumValueString = "" + Integer.MIN_VALUE;
        } else if (long.class.equals(type)) {
            maximumValueString = "" + Long.MIN_VALUE;
        } else if (float.class.equals(type)) {
            maximumValueString = "" + Float.MIN_VALUE;
        } else if (double.class.equals(type)) {
            maximumValueString = "" + Double.MIN_VALUE;
        } else if (boolean.class.equals(type)) {
            maximumValueString = "1";
        } else {
            throw new IllegalArgumentException(type.toString());
        }

        return maximumValueString;
    }
}
