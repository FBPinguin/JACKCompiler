public enum keyWord {
    CLASS("class"), METHOD("method"), FUNCTION("function"), CONSTRUCTOR("constructor"), INT("int"),
    BOOLEAN("boolean"), CHAR("char"), VOID("void"), VAR("var"), STATIC("static"), FIELD("field"), LET("let"),
    DO("do"), IF("if"), ELSE("else"), WHILE("while"), RETURN("return"), TRUE("true"), FALSE("false"), NULL("null"), THIS("this");

    final String string;

    keyWord(String string){
        this.string = string;
    }


    public static keyWord getKeyWord(String s){
        switch(s){
            case "class":
                return CLASS;
            case "method":
                return METHOD;
            case "function":
                return FUNCTION;
            case "constructor":
                return CONSTRUCTOR;
            case "int":
                return INT;
            case "boolean":
                return BOOLEAN;
            case "char":
                return CHAR;
            case "void":
                return VOID;
            case "var":
                return VAR;
            case "static":
                return STATIC;
            case "field":
                return FIELD;
            case "let":
                return LET;
            case "do":
                return DO;
            case "if":
                return IF;
            case "else":
                return ELSE;
            case "while":
                return WHILE;
            case "return":
                return RETURN;
            case "true":
                return TRUE;
            case "false":
                return FALSE;
            case "null":
                return NULL;
            case "this":
                return THIS;
        }
        throw new IllegalStateException("I can't convert this into a keyWord: " + s);
    }
}
