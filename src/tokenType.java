import java.lang.reflect.Type;
import java.util.regex.Pattern;

public enum tokenType {
    KEYWORD("keyword"), SYMBOL("symbol"), IDENTIFIER("identifier"), INT_CONST("integerConstant"), STRING_CONST("stringConstant");

    final String string;

    tokenType(String string){
        this.string = string;
    }

    public static tokenType getTokenType(String token){
        switch(token){
            case "class":
            case "constructor":
            case "function":
            case "method":
            case "field":
            case "static":
            case "var":
            case "int":
            case "char":
            case "boolean":
            case "void":
            case "true":
            case "false":
            case "null":
            case "this":
            case "let":
            case "do":
            case "if":
            case "else":
            case "while":
            case "return":
                return KEYWORD;
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
                return SYMBOL;
        }

        if (token.matches("^[a-zA-Z_][a-zA-Z_\\d]*")){
            return IDENTIFIER;
        }
        else if (token.matches("\\d+")) {
            return INT_CONST;
        }
        else if (token.matches("\".*\"")){
            return STRING_CONST;
        }
        else{
            throw new IllegalStateException("I could not identify this token: " + token);
        }
    }
}
