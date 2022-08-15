package com.uhh.code.polymorph;

import com.uhh.code.polymorph.protobuilder.ProtoType;
import com.uhh.code.polymorph.protobuilder.ProtoTypeSizeEstimator;

import javax.xml.crypto.dsig.Transform;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// This function represents the transformation result of any given transformation
public class TransformationResult {
    // allow instantiating using just a size and a count
    public TransformationResult(int size, int transformationCount)
    {
        this.size = size;
        this.transformationCount = transformationCount;
    }

    // allow instantiating with the type which was transformed to
    public TransformationResult(ProtoType protoType, int length, int transformationCount)
    {
        // automatically calculate its estimated worst case size
        this.size = ProtoTypeSizeEstimator.getPrimitiveSizeEstimate(protoType, length);
        this.transformationCount = transformationCount;
        // add it to the new types
        this.newDataTypes.add(protoType.toString());
    }


    // Method for adding other transformation results
    public void AddTransformationResult(TransformationResult otherResult)
    {
        // add the size and transformationCount to ours
        this.programSize += otherResult.programSize;
        this.size += otherResult.size;
        this.transformationCount += otherResult.transformationCount;
        // to our collections add theirs
        this.previousDataTypes.addAll(otherResult.previousDataTypes);
        this.newDataTypes.addAll(otherResult.newDataTypes);
    }

    // calculate the distance for a specific datatype
    public int CalculateDataTypeDistanceForType(String type)
    {
        // how many occurences did we have previously
        var previousOccurences = 0;
        // how many occurences do we have now
        var newOccurences = 0;

        // count previous occurences
        for(var previousType : this.previousDataTypes)
        {
            if (previousType == type)
            {
                previousOccurences += 1;
            }
        }

        // count new occurences
        for(var newType : this.newDataTypes)
        {
            if (newType == type)
            {
                newOccurences += 1;
            }
        }

        // return the difference between the previous and the new occurences
        return Math.abs(previousOccurences - newOccurences);
    }

    // method for calculating the edit distance between datatypes
    public int CalculateDataTypeDistance()
    {
        // create a array with all types
        var allTypes = new ArrayList<String>();
        allTypes.addAll(this.previousDataTypes);
        allTypes.addAll(this.newDataTypes);

        // create a empty array
        var types = new ArrayList<String>();

        // add every possible type which was in the alltypes array only once
        // (remove duplicates)
        for(var type : allTypes)
        {
            if (!types.contains(type))
            {
                types.add(type);
            }
        }

        // default distance 0
        var dist = 0;

        // for every distance between types, add it to the total distance
        for(var type : types)
        {
            dist = dist + this.CalculateDataTypeDistanceForType(type);
        }

        // return total edit distance
        return dist;
    }


    public int programSize;
    public int size;
    public int transformationCount;
    public List<String> previousDataTypes = new ArrayList<>();
    public List<String> newDataTypes = new ArrayList<>();
}
