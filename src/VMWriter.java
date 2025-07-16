import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    public enum segment{
        CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP;
    }

    public enum command{
        ADD('+'), SUB('-'), NEG('-'), EQ('='), GT('>'), LT('<'), AND('&'), OR('|'), NOT('~');

        final char string;

        command(char string){
            this.string = string;
        }

        public static command getCommand(char s) throws IOException {
            for (command command : command.values()){
                if (s == command.string){
                    return command;
                }
            }
            throw new IOException("I can't evaluate this command");
        }
    }

    FileWriter out;

    VMWriter(FileWriter out){
        this.out = out;
    }


    /**
     * Writes a VM push command
     * @param segment
     * @param index
     */
    public void writePush(segment segment, int index) throws IOException {
        out.append("push " + segment.toString().toLowerCase() + " " + index + '\n');
    }

    /**
     * Writes a VM pop command.
     * @param segment
     */
    public void writePop(segment segment, int index) throws IOException {
        out.append("pop " + segment.toString().toLowerCase() + " " + index + '\n');
    }

    /**
     * Writes a VM arithmetic-logical command.
     */
    public void writeArithmetic(command command) throws IOException {
        out.append(command.toString().toLowerCase() + '\n');
    }

    public void writeLabel(String label) throws IOException {
        out.append("label " + label + '\n');
    }

    public void writeGoto(String label) throws IOException {
        out.append("goto " + label + '\n');
    }

    public void writeIf(String label) throws IOException {
        out.append("if-goto " + label + '\n');
    }

    public void writeCall(String name, int nArgs) throws IOException {
        out.append("call " + name + " " + nArgs + '\n');
    }

    public void writeFunction(String name, int nVars) throws IOException {
        out.append("function " + name + " " + nVars + '\n');
    }

    public void writeReturn() throws IOException {
        out.append("return\n");
    }

    public void close() throws IOException {
        out.close();
    }


}
