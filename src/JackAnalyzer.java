import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

public class JackAnalyzer {
    public static void main(String... args) throws Exception {
        if (args.length != 2){
            throw new IllegalArgumentException("Usage: java JACKCompiler [Input file] [Output file]");
        }
        if (args[0].endsWith(".jack")){
            handleSingleFile(args[0], args[1]);
        }
        else{
            handleDirectory(args[0], args[1]);
        }
    }

    private static void handleSingleFile(String fileName, String fileNameOut) throws Exception {
        JackTokenizer tokenizer = new JackTokenizer(new File(fileName));
        FileWriter out = new FileWriter(fileNameOut);
        CompilationEngine engine = new CompilationEngine(tokenizer, out);
        tokenizer.advance();
        //REMOVE WHEN MORE THAN ONE CLASS, ITS SO THAT IT DOESN'T IMMEDIATELY CRASH
        //while (tokenizer.hasMoreTokens()){
            engine.compileClass();
        //}
        out.close();
    }

    private static void handleDirectory(String directoryName, String outputDirectory) throws Exception {
        DirectoryStream<Path> stream = getDirStream(directoryName);
        //Path output = FileSystems.getDefault().getPath(outputDirectory);
        CompilationEngine.startLabelIndex();
        for (Path path : stream){
            File file = path.toFile();
            if (!file.toString().endsWith(".jack")){
                continue;
            }
            JackTokenizer tokenizer = new JackTokenizer(file);
            System.out.println(file.getName());

            Path output = FileSystems.getDefault().getPath(outputDirectory+"/"+file.getName().substring(0,file.getName().length()-5) + ".vm");

            if (!Files.exists(output)) {
                Files.createFile(output);
            }

            FileWriter outputFile = new FileWriter(String.valueOf(output));


            CompilationEngine engine = new CompilationEngine(tokenizer, outputFile);
            tokenizer.advance();
            //REMOVE WHEN MORE THAN ONE CLASS, ITS SO THAT IT DOESN'T IMMEDIATELY CRASH
            //while (tokenizer.hasMoreTokens()){
            engine.compileClass();
            //}
            outputFile.close();



        }

    }
    private static DirectoryStream<Path> getDirStream(String directoryName){
        Path dir = FileSystems.getDefault().getPath(directoryName);
        try {
            return Files.newDirectoryStream(dir);
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
            throw new IllegalArgumentException("error, directory exception or smth");
        }
    }
}
