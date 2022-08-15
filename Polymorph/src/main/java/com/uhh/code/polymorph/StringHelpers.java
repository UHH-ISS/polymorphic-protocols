package com.uhh.code.polymorph;

public final class StringHelpers {

    // Method for uppercasing first character of a string
    public static String UppercaseFirstCharacter(String toUppercase)
    {
        // Empty string do nothing
        if (toUppercase.isBlank() || toUppercase.isEmpty())
        {
            return toUppercase;
        }

        // Only length 1 does not need substring
        if (toUppercase.length() == 1)
        {
            return "" + Character.toUpperCase(toUppercase.charAt(0));
        }

        // Uppercase first character and append characters at positions [2, len] unmodified
        return Character.toUpperCase(toUppercase.charAt(0)) + toUppercase.substring(1);
    }

    // Method for lowercasing first character of a string
    public static String LowerCaseFirstCharacter(String toLowerCase)
    {
        // Empty string do nothing
        if (toLowerCase.isBlank() || toLowerCase.isEmpty())
        {
            return toLowerCase;
        }

        // Only length 1 does not need substring
        if (toLowerCase.length() == 1)
        {
            return "" + Character.toLowerCase(toLowerCase.charAt(0));
        }

        // Lowercase first character and append characters at positions [2, len] unmodified
        return Character.toLowerCase(toLowerCase.charAt(0)) + toLowerCase.substring(1);
    }

    // Create the camel cased version of a underscore cased string
    public static String UnderscoreCaseToCamelCase(String underscoreCase)
    {
        // Result
        String camelCase = "";

        // whether the next character should be uppercased
        boolean uppercaseNext = false;

        for(int i = 0;i < underscoreCase.length();i++)
        {
            var ch = underscoreCase.charAt(i);

            // if this is a underscore, next character needs to be uppercased
            if (ch == '_')
            {
                uppercaseNext = true;
                continue;
            }

            if (Character.isDigit(ch))
            {
                uppercaseNext = true;
            }

            // uppercase this character?
            if (uppercaseNext)
            {
                // then procceed to uppercase it
                camelCase = camelCase + Character.toUpperCase(ch);
                uppercaseNext = false;
            }else{
                // leave unchanged
                camelCase = camelCase + ch;
            }
        }


        // reteurn result
        return camelCase;
    }

    public static String UnderscoreCaseToPascalCase(String underscoreCase)
    {
        // convert to camel case
        var pascalCase = UnderscoreCaseToCamelCase(underscoreCase);
        // lowercase the first character
        pascalCase = Character.toUpperCase(pascalCase.charAt(0)) + pascalCase.substring(1);

        return pascalCase;
    }
}
