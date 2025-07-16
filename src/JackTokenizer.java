import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JackTokenizer {
    Scanner in;
    String currToken;
    Queue<String> left;

    JackTokenizer(File inputFile) throws FileNotFoundException {
        this.in = new Scanner(inputFile);
        this.left = new LinkedList<>();
    }


    /**
     * Are there more tokens in the input?
     * @return the boolean whether this is true
     */
    public boolean hasMoreTokens(){
        return in.hasNextLine() || !left.isEmpty();
    }

    /**
     * Gets the next token from the input,
     * and makes it the current token.
     * <p>
     * This method should be called only if
     * hasMoreTokens is true.
     * <p>
     * initially there is no current token.
     */
    public void advance(){
        if (!left.isEmpty()){
            currToken = left.remove();
            return;
        }
        String next;
        while (true){
            next = in.nextLine().strip();
            if (next.startsWith("//") || next.isEmpty()){
                continue;
            }
            break;
        }
        if (next.startsWith("/**")) {
            while (!next.endsWith("*/")){
                next = in.nextLine().strip();
            }
            next = in.nextLine().strip();
        }

        if (next.contains("//")){
            next = next.substring(0, next.indexOf("//")-1);
        }

        int begin = 0;
        int end;
        for (int i = 0; i < next.length(); i++){
            String c = String.valueOf(next.charAt(i));

            if (c.equals(" ")){
                if (begin != i) {
                    left.add(next.substring(begin, i));
                }
                begin = i + 1;
                continue;
            }

            if (c.equals("\"")){
                i++;
                String s = String.valueOf(next.charAt(i));
                while (!s.equals("\"")){
                    s = String.valueOf(next.charAt(i));
                    i++;
                }
                left.add(next.substring(begin, i));
                //begin = i + 1;
                begin = i;
            }

            if (isSymbol(c) && begin != i){
                left.add(next.substring(begin, i));
                left.add(c);
                begin = i + 1;
            }
            else if (isSymbol(c)){
                left.add(c);
                begin = i + 1;
            }
        }
        if (begin != next.length()) {
            left.add(next.substring(begin));
        }

        currToken = left.remove();

    }

    private boolean isSymbol(String c){
        switch (c){
            case "{":
            case "}":
            case "(":
            case ")":
            case "[":
            case "]":
            case ".":
            case ",":
            case ";":
            case "+":
            case "-":
            case "*":
            case "/":
            case "&":
            case "|":
            case ">":
            case "<":
            case "=":
            case "~":
                return true;
        }
        return false;
    }




    /**
     * Returns the type of the current token, as a constant
     */
    public tokenType tokenType(){
        return tokenType.getTokenType(currToken);
    }

    /**
     * Returns the keyword which is the current token, as a constant
     * This method should be called only if tokenType is KEYWORD
     * @return the constant keyWord
     */
    public keyWord keyWord(){
        return keyWord.getKeyWord(currToken);
    }

    /**
     * Returns the character which is the current token.
     * Should only be called if tokenType is SYMBOL
     * @return the char
     */
    public char symbol(){
        return currToken.charAt(0);
    }

    /**
     * Returns the character which is the current token.
     * Should only be called if tokenType is IDENTIFIER
     * @return the String
     */
    public String identifier(){
        return currToken;
    }
    /**
     * Returns the character which is the current token.
     * Should only be called if tokenType is INT_CONST
     * @return the int
     */
    public int intVal(){
        return Integer.parseInt(currToken);
    }

    /**
     * Returns the character which is the current token.
     * Should only be called if tokenType is STRING_CONST
     * @return the String
     */
    public String stringVal(){
        return currToken.substring(1, currToken.length()-1);
    }
}
