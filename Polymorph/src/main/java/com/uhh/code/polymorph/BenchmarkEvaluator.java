package com.uhh.code.polymorph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BenchmarkEvaluator {

    // Function for writing files for numerical categories
    public static void EvaluateNumericalCategoryWriteLog(String outputDirName, String category, long min, long max, long avg, long median, long n) {
        // Name always depends on category name
        var outputFilename = outputDirName + "/log_" + category + ".txt";

        File newFile = new File(outputFilename);

        if (newFile.exists()) {
            newFile.delete();
        }

        // Create the directories required
        newFile.getParentFile().mkdirs();

        try {
            // Attempt to create file
            if (!newFile.createNewFile()) {
                System.out.println("Could not create output file!");
                return;
            }

            // Get a handle to write to it and write the min, max, avg, median and sample size to the file
            var writer = new FileWriter(outputFilename, true);

            writer.write("Min: " + min);
            writer.write('\n');
            writer.write("Max: " + max);
            writer.write('\n');
            writer.write("Avg: " + avg);
            writer.write('\n');
            writer.write("Median: " + median);
            writer.write('\n');
            writer.write("Sample Size: " + n);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function for evaluating a numerical category
    // It automatically calculates the min, max, avg, median and sample size for the input collection
    public static void EvaluateNumericalCategory(String outputDirName, String category, List<Long> data)
    {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long avg = 0;
        long n = 0;
        long median = 0;

        for (var value : data)
        {
            // Calculate sample size
            n += 1;
            // Calculate min
            min = Math.min(min, value);
            // Calculate max using Math.max
            max = Math.max(max, value);
            // Calculate average iteratively
            avg += (value - avg) / n;
        }

        // Sort it so we can get the median
        Collections.sort(data);
        // Get median
        median = data.get(data.size() / 2);
        // Write all this information to the log
        EvaluateNumericalCategoryWriteLog(outputDirName, category, min, max, avg, median, n);
    }

    // Function to write array categories
    // For array categories we simply write down the values of the array
    public static void EvaluateArrayCategory(String outputDirName, String category, List<Long> data)
    {
        // Name depends on category name
        var outputFilename = outputDirName + "/log_" + category + ".txt";

        File newFile = new File(outputFilename);

        if (newFile.exists())
        {
            newFile.delete();
        }

        // try to create required dirs
        newFile.getParentFile().mkdirs();

        try {
            // attempt to create file
            if (!newFile.createNewFile())
            {
                System.out.println("Could not create output file!");
                return;
            }
            // get a handle to write to it
            var writer = new FileWriter(outputFilename, true);
            // write the arrays contents into the file
            writer.write(data.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This function parses logFiles to acquire the specific categories
    public static void EvaluateLog(String log,
                                   List<Long> elapsedTimes,
                                   List<Long> usedSeeds,
                                   List<Long> usedMemories,
                                   List<Long> dataSizes,
                                   List<Long> transformationsApplied,
                                   List<Long> editDistance,
                                   List<Long> programSize)
    {
        // Split by each newline
        var lines = log.split("\n");

        for(var line : lines)
        {
            // The : seperates the name from the value
            var nameValue = line.split(":");
            // as Data, nameValue = category: value
            // therefore, value starts with ' ' and we need to cut this off using substring
            var name = nameValue[0];
            var value = nameValue[1].substring(1);

            // parse the value as long to ensure we dont truncate it
            var numericalValue = Long.valueOf(value);

            // We need to add it to the right category
            switch(name)
            {
                case "Elapsed Time (ns)" -> elapsedTimes.add(numericalValue);
                case "Used Seed (PRNG)" -> usedSeeds.add(numericalValue);
                case "Used Memory (B)" -> usedMemories.add(numericalValue);
                case "Size (B)" -> dataSizes.add(numericalValue);
                case "Transformations Applied (Count)" -> transformationsApplied.add(numericalValue);
                case "Data-Types Edit Distance (Count)" -> editDistance.add(numericalValue);
                case "Program Size (B)" -> programSize.add(numericalValue);
            }
        }
    }

    // Base function for evalutation of logs
    public static void EvaluateBenchmark(String outputDirName, List<String> allContents)
    {
        // Create the collections for the informations contained within those logs
        var elapsedTimes = new ArrayList<Long>();
        var usedSeeds = new ArrayList<Long>();
        var usedMemories = new ArrayList<Long>();
        var dataSizes = new ArrayList<Long>();
        var transformationsApplied = new ArrayList<Long>();
        var editDistance = new ArrayList<Long>();
        var programSize = new ArrayList<Long>();

        // parse all logs
        for(var content : allContents)
        {
            EvaluateLog(content, elapsedTimes, usedSeeds, usedMemories, dataSizes, transformationsApplied, editDistance, programSize);
        }

        // calculate and store the statistical values for each category
        EvaluateNumericalCategory(outputDirName, "elapsedTimes", elapsedTimes);
        EvaluateArrayCategory(outputDirName, "usedSeeds", usedSeeds);
        EvaluateNumericalCategory(outputDirName, "usedMemories", usedMemories);
        EvaluateNumericalCategory(outputDirName, "dataSizes", dataSizes);
        EvaluateNumericalCategory(outputDirName, "transformationsApplied", transformationsApplied);
        EvaluateNumericalCategory(outputDirName, "editDistance", editDistance);
        EvaluateNumericalCategory(outputDirName, "programSize", programSize);
    }

}