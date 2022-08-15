package com.uhh.code.polymorph;

import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// This class parses Protos (.Proto)
public final class Parser {

    // Method to compile the Protobuf binary for a given Proto
    private static String CompileProtobufBinary(String outDir, String protoPath, String absoluteFilename, String relativeFilename)
    {
        // We store the pb in a temp directory (new java feature, hope its good)
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("polymorph_tmp_pb");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If we couldnt create a tmp directory, dont do anything
        if (tmpDir == null)
        {
            return null;
        }

        // store in tmp file directory but with same relative path as input and with .pb as ext
        var pbFilename = tmpDir.getFileName() + relativeFilename + ".pb";

        System.out.println("Proto Pb name: " + pbFilename);

        // args for the compiler
        var args = new String[]{"--include_source_info", "--proto_path", protoPath, "--descriptor_set_out=" + pbFilename, absoluteFilename};

        try {
            // compile it
            Protoc.runProtoc(args);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return pbFilename;
    }

    // Method to parse a individual proto
    public static FileDescriptorSet ParseProto(String outDir, String protoPath, String absoluteFilename, String relativeFilename)
    {
        try {
            // Compile the .proto to a .pb
            var pbFilename = CompileProtobufBinary(outDir, protoPath, absoluteFilename, relativeFilename);
            // acquire a read handle to the new .pb
            var pbFileHandle = new File(pbFilename);
            var pbFileInputStream = new FileInputStream(pbFileHandle);

            // Use Protobufs built in protobuf binary parser to parse the proto
            var parsed = FileDescriptorSet.parseFrom(pbFileInputStream);
            // return the parsed file
            return parsed;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
