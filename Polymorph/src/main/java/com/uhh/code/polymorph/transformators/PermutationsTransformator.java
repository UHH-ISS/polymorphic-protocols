package com.uhh.code.polymorph.transformators;
import com.uhh.code.polymorph.TransformationResult;
import com.uhh.code.polymorph.protobuilder.ProtoMessage;

import java.util.ArrayList;
import java.util.Random;

class PermutationsTransformator {
    private Random rnd;
    private boolean ignoreTransformations;

    private PermutationsTransformator()
    {

    }

    public PermutationsTransformator(Random rnd)
    {
        this.rnd = rnd;
    }

    public TransformationResult PerformTransformation(ProtoMessage protoMessage)
    {
        if (this.ignoreTransformations)
        {
            return new TransformationResult(0, 0);
        }

        System.out.println("Assigning new Permutation");

        var highestField = protoMessage.getHighestIndex();
        var indexList = new ArrayList<Integer>();

        for(int i=0;i<highestField;i++)
        {
            indexList.add(i + 1);
        }

        var messageIndexes = protoMessage.getIndexes();

        for(var messageIndex : messageIndexes)
        {
            var indexListSize = indexList.size();
            var index = this.rnd.nextInt(indexListSize);

            var randomIndex = indexList.get(index);
            indexList.remove(index);
            messageIndex.SetIndex(randomIndex);
        }

        return new TransformationResult(0, 1);
    }

    public void SetIgnoreTransformations(boolean ignoreTransformations)
    {
        this.ignoreTransformations = ignoreTransformations;
    }
}
