package com.uhh.code.polymorph;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    // parse all dirs
    public static void HandleFileRecursively(File inputDir, String outputDir, String rootPath, Transformator transformator)
    {
        // list all files within the dir
        var fileListing = inputDir.listFiles();

        if (fileListing == null)
        {
            // if none, terminate
            return;
        }

        for(File file : fileListing)
        {
            // if its a subdirectory, we have to recurse it as well
            if(file.isDirectory())
            {
                var dirAbsPath = file.getPath();

                var dirFileSepIndex = dirAbsPath.lastIndexOf(File.separator);

                if (dirFileSepIndex > 0)
                {
                    var dirAppendix = dirAbsPath.substring(dirFileSepIndex + 1);
                    outputDir = outputDir + dirAppendix + "/";
                }

                HandleFileRecursively(file, outputDir, rootPath, transformator);

            }
            else
            {
                // if its a file, we can give it to the polymorph
                HandleFile(file, outputDir, rootPath, transformator);
            }
        }
    }

    // handle a specific file
    public static void HandleFile(File inputFile, String outputDir, String rootPath, Transformator transformator)
    {
        // get its name
        var inputFilename = inputFile.getName();

        // if the file cant be read, tell the user
        if (!inputFile.canRead())
        {
            System.out.println("Can not read file: " + inputFilename);
            return;
        }

        // get the abspath and path, this is relevant for the protoc compiler later on
        var absoluteFilename = inputFile.getAbsolutePath() ;
        var path = absoluteFilename.substring(0, absoluteFilename.lastIndexOf(File.separator));

        // pass the information to the transformator
        transformator.TransformFile(outputDir, path, absoluteFilename, inputFilename, rootPath);

    }

    // entrypoint for java functions
    public static void main(String[] args) {
        // get access to the java runtime
        var runtime = Runtime.getRuntime();

        System.out.println("Welcome to the Polymorph!");

        var inputDirName =  "protoin/";
        var outputDirName = "protoout/";

        // open the input directory
        var inputDir = new File(inputDirName);

        if (inputDir == null)
        {
            System.out.println("Could not get a handle to the input directory!");
            return;
        }

        // the default parameters
        long inputSeed = 0;
        boolean passThrough = false;
        boolean testBench = false;
        String packageName = null;

        // parse the parameters
        if (args.length > 0) {
            var arg0 = args[0];

            if (!arg0.isBlank() && !arg0.isEmpty())
            {
                if (arg0.equals("passthrough")) {
                    passThrough = true;
                } else {
                    if (arg0.equals("testbench")) {
                        testBench = true;
                    } else {
                        inputSeed = Integer.valueOf(args[0]);
                    }
                }
            }
        }

        // testbench mode
        if (testBench)
        {
            // testbench has different arguments, has iterations as arg
            var args1 = args.length > 1 ? args[1] : null;
            var args2 = args.length > 2 ? args[2] : null;

            Integer iterations = args1 != null ? Integer.valueOf(args1) : 50;
            boolean passThroughBench = args2 != null && args2.equals("passthrough");

            System.out.println("Test Bench set to " + iterations + " iterations");

            for(int i=0;i<iterations;i++)
            {
                System.out.println("Iteration #" + (i + 1));

                // prepare args for a new main call
                // set a new output directory
                var newArgsList = new ArrayList <String>();

                if (passThroughBench)
                {
                    newArgsList.add("passthrough"); // pick a seed by yourself
                }else{
                    newArgsList.add(""); // pick a seed by yourself
                }

                newArgsList.add(outputDirName +  (i + 1) + "/"); // output to a own iteration folder

                // convert to array as that is what main accepts
                var newArgs = new String[newArgsList.size()];
                newArgsList.toArray(newArgs);

                // gc to make the bench more fair
                runtime.gc();
                try {
                    // to interfere with the JIT
                    Thread.sleep(50); // let java VM cool down for 50ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // again gc just to be sure
                runtime.gc();

                // call main again with new args to run new iteration
                main(newArgs);
            }

            // array for all logfiles
            var allContents = new ArrayList<String>();

            // for every iteration we did
            for (int i=0;i<iterations;i++)
            {
                // open its logfiles
                var logFile = new File(outputDirName + (i  + 1) + "/log.txt");
                try {
                    // read the content
                    var logFileInput = new FileInputStream(logFile);
                    var allBytes = logFileInput.readAllBytes();
                    var contents = new String(allBytes);
                    // add content to array for all logfiles
                    allContents.add(contents);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // use benchmarkevaluator to evalulate the benchmark
            BenchmarkEvaluator.EvaluateBenchmark(outputDirName, allContents);
            return;
        }

        // if a output directory is set, accept it
        if (args.length > 1 && !args[1].isEmpty() && !args[1].isBlank())
        {
            // set to output dir
            outputDirName = args[1];
            System.out.println("Output directory set to " + outputDirName);
        }

        // if a packagename is set, use it
        if (args.length > 2 && !args[2].isEmpty() && !args[2].isBlank())
        {
            packageName = args[2];
            System.out.println("Package name set to " + packageName);

        }

        packageName = "com.autogeneratedby.polymorph";


        // if a input seed is set, notify that it will be used
        if (inputSeed != 0) {
            System.out.println("Input seed: " + inputSeed);
        }else{
            // randomly select a input seed
            System.out.println("No input seed, seeding using a random one");
            inputSeed = new Random().nextLong();
            System.out.println("Randomly selected seed: " + inputSeed);
        }

        // output the input/output dir
        System.out.println("Input Source Location:");
        System.out.println(inputDirName);
        System.out.println("Output Source Location:");
        System.out.println(outputDirName);

        // gc to make the runtime logs more accurate
        runtime.gc();

        // get first timepoint to so we can later calculate timeelapsed
        long startTime = System.nanoTime();

        // initiate polymorph and PRNG
        Transformator transformator = new Transformator(new Random(inputSeed));

        // if this run is passthrough, tell transformator to only wrap API
        if (passThrough)
        {
            transformator.SetIgnoreTransformations(true);
            System.out.println("Passthrough mode enabled, input API will only be wrapped");
        }

        //if there is a package name, tell it to the transformator
        if (packageName != null)
        {
            transformator.SetPackageName(packageName);
        }

        // disable dummy fields for now to get more reproduceable results
        transformator.SetDummyFieldAmount(0);

        // check whether input dir contains any files
        var inputDirFiles = inputDir.listFiles();

        if (inputDirFiles == null)
        {
            // if not, notify user its empty
            System.out.println("No files within input directory!");
            return;
        }

        // parse all files in input directory
        var inputDirPathAbs = inputDir.getAbsolutePath();
        var rootPath = inputDirPathAbs.substring(0,inputDirPathAbs.lastIndexOf(File.separator));

        HandleFileRecursively(inputDir, outputDirName, rootPath, transformator);

        // polymorph is done, fetch endtime and bytesused
        var endTime = System.nanoTime();
        var bytesUsed = runtime.totalMemory() - runtime.freeMemory();

        // open the dir for the logs
        var outputDir = new File(outputDirName);

        if (outputDir == null)
        {
            System.out.println("Could not get a handle to the output directory!");
            return;
        }

        // outputfilename depends on outputdir
        var outputFilename = outputDirName + "/log.txt";

        // if the file exists, delete it
        File newFile = new File(outputFilename);

        if (newFile.exists())
        {
            newFile.delete();
        }

        // create parental dirs
        newFile.getParentFile().mkdirs();

        try {
            // create a new file for the log
            if (!newFile.createNewFile())
            {
                System.out.println("Could not create output file!");
                return;
            }

            var writer = new FileWriter(outputFilename, true);
            long timeElapsed = endTime - startTime;

            // write log information inside
            writer.write("Elapsed Time (ns): " + timeElapsed);
            writer.write('\n');
            writer.write("Used Seed (PRNG): " + inputSeed);
            writer.write('\n');
            writer.write("Used Memory (B): " + bytesUsed );
            writer.write('\n');
            writer.write("Size (B): " + transformator.apiTransformationResult.size);
            writer.write('\n');
            writer.write("Transformations Applied (Count): " + transformator.apiTransformationResult.transformationCount);
            writer.write('\n');
            writer.write("Data-Types Edit Distance (Count): " + transformator.apiTransformationResult.CalculateDataTypeDistance());
            writer.write('\n');
            writer.write("Program Size (B): " + transformator.apiTransformationResult.programSize);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
