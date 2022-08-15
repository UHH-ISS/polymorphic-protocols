package com.uhh.code.polymorph;

import java.util.Random;

// This class generates random strings using the RNG
public class RandomStringGenerator
{
    Random rnd;
    // All possible characters
    static final String Characters = "abcdefghijklmnopqrstuvwxyz";

    // Instantiate the random generator
    public RandomStringGenerator(Random rnd)
    {
        this.rnd = rnd;
    }

    // Method for generating fixed len strings
    public String generateString(int len)
    {
        // create a string with the specified length
        var stringBuilder = new StringBuilder(len);

        for (int i=0;i<len;i++)
        {
            // pick a random character
            var randomCharacterIndex = this.rnd.nextInt(Characters.length());
            var randomCharacter = Characters.charAt(randomCharacterIndex);

            stringBuilder.append(randomCharacter);
        }

        return stringBuilder.toString();
    }

}
