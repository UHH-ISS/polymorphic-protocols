package com.uhh.code.polymorph;

// Pair allows us to return two values in Java similar to C#s builtin return (a,b) functionality
public record Pair<A, B>(A first, B second) {}
