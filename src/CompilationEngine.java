import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class CompilationEngine {

    JackTokenizer tokenizer;
    VMWriter out;
    SymbolTable symbolTable;
    String className;
    static int labelIndex;

    CompilationEngine(JackTokenizer tokenizer, FileWriter out) {
        this.tokenizer = tokenizer;
        this.out = new VMWriter(out);
    }

    public static void startLabelIndex(){
        labelIndex = 0;
    }

    public void compileClass() throws Exception {
        this.symbolTable = new SymbolTable();



        eat(keyWord.CLASS);

        this.className = tokenizer.identifier();
        eat(tokenType.IDENTIFIER);

        eat(tokenType.SYMBOL, '{');

        compileClassVarDec();

        compileSubroutineDec();

        eat(false, tokenType.SYMBOL, '}');






    }

    public void compileClassVarDec() throws Exception {
//        if (!isKeyWords(keyWord.STATIC, keyWord.FIELD)){
//            return;
//        }



        while (isType(tokenType.KEYWORD) && isKeyWords(keyWord.STATIC, keyWord.FIELD)){

            SymbolTable.kind kind = SymbolTable.kind.convertStaticField(tokenizer.keyWord());
            eatKeyWords(keyWord.STATIC, keyWord.FIELD);


            String type = tokenizer.identifier();
            eatKeyWords(true, keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN);

            String identifier = tokenizer.identifier();
            symbolTable.define(identifier, type, kind);
            eatNewId();



            while (isType(tokenType.SYMBOL, ',')){

                eat(tokenType.SYMBOL, ',');

                identifier = tokenizer.identifier();
                symbolTable.define(identifier, type, kind);
                eatNewId();



            }

            eat(tokenType.SYMBOL, ';');



        }





    }


    public void compileSubroutineDec() throws Exception {
        if (isType(tokenType.KEYWORD) && !isKeyWords(keyWord.CONSTRUCTOR, keyWord.FUNCTION, keyWord.METHOD)){
            return;
        }


        while (isType(tokenType.KEYWORD) && isKeyWords(keyWord.CONSTRUCTOR, keyWord.FUNCTION, keyWord.METHOD)){
            symbolTable.startSubroutine();

            if (tokenizer.keyWord() != keyWord.CONSTRUCTOR) {
                symbolTable.define("this", className, SymbolTable.kind.ARG);
            }


            keyWord keyword = tokenizer.keyWord();

            eatKeyWords(keyWord.CONSTRUCTOR, keyWord.FUNCTION, keyWord.METHOD);


            eatKeyWords(true, keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN, keyWord.VOID);

            String identifier = tokenizer.identifier();
            eat(tokenType.IDENTIFIER);

            eat(tokenType.SYMBOL, '(');

            int nArgs = compileParameterList();

            eat(tokenType.SYMBOL,')');

            switch (keyword){
                case CONSTRUCTOR:
                    writeConstructor(identifier, nArgs);
                    break;
                case METHOD:
                    //TODO: RETURN THE AMOUNT OF LOCAL VARIABLES,DOESNT NEED nARGS
                    writeMethod(identifier, 10);//nArgs);
                    break;
                case FUNCTION:
                    //TODO: RETURN THE AMOUNT OF LOCAL VARIABLES, DOESN'T NEED nARGS
                    out.writeFunction(className + "." + identifier, 10);//symbolTable.varCount(SymbolTable.kind.ARG));
                    break;


            }

            compileSubroutineBody();





        }


    }

    private void writeConstructor(String identifier, int nArgs) throws IOException {
        out.writeFunction(className + "." + identifier, nArgs);//symbolTable.varCount(SymbolTable.kind.ARG));
        out.writePush(VMWriter.segment.CONSTANT, symbolTable.varCount(SymbolTable.kind.FIELD));
        out.writeCall("Memory.alloc", 1);
        out.writePop(VMWriter.segment.POINTER, 0);
    }

    private void writeMethod(String identifier, int nArgs) throws IOException {
        out.writeFunction(className + "." + identifier, nArgs);//symbolTable.varCount(SymbolTable.kind.ARG));
        out.writePush(VMWriter.segment.ARGUMENT, 0);
        out.writePop(VMWriter.segment.POINTER, 0);
    }

    public int compileParameterList() throws Exception {
        if ((!isType(tokenType.KEYWORD) || !isKeyWords(keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN)) && !isType(tokenType.IDENTIFIER)){


            return 0;
        }

        int nArgs = 0;





        String type = tokenizer.identifier();
        eatKeyWords(true, keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN);

        String identifier = tokenizer.identifier();
        eat(tokenType.IDENTIFIER);

        symbolTable.define(identifier, type, SymbolTable.kind.ARG);

        nArgs++;

        while (isType(tokenType.SYMBOL, ',')){
            eat(tokenType.SYMBOL, ',');
            nArgs++;

            eatKeyWords(true, keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN);

            identifier = tokenizer.identifier();
            symbolTable.define(identifier, type, SymbolTable.kind.ARG);
            eat(tokenType.IDENTIFIER);


        }
        return nArgs;
    }

    public void compileSubroutineBody() throws Exception {



        eat(tokenType.SYMBOL, '{');

        compileVarDec();

        compileStatements();

        eat(tokenType.SYMBOL, '}');



    }

    public void compileVarDec() throws Exception {
        if (!isKeyWords(keyWord.VAR)){
            return;
        }



        while (isType(tokenType.KEYWORD) && isKeyWords(keyWord.VAR)){


            eat(keyWord.VAR);


            String type = tokenizer.identifier();
            eatKeyWords(true, keyWord.INT, keyWord.CHAR, keyWord.BOOLEAN);

            String identifier = tokenizer.identifier();
            symbolTable.define(identifier, type, SymbolTable.kind.VAR);
            eatNewId();



            while (isType(tokenType.SYMBOL, ',')){
                eat(tokenType.SYMBOL, ',');

                identifier = tokenizer.identifier();
                symbolTable.define(identifier, type, SymbolTable.kind.VAR);
                eatNewId();


            }

            eat(tokenType.SYMBOL, ';');


        }





    }

    public void compileStatements() throws Exception {

        while (isType(tokenType.KEYWORD) && isKeyWords(keyWord.LET, keyWord.IF, keyWord.WHILE, keyWord.DO, keyWord.RETURN)){
            switch (tokenizer.keyWord()){
                case LET:
                    compileLet();
                    continue;
                case IF:
                    compileIf();
                    continue;
                case WHILE:
                    compileWhile();
                    continue;
                case DO:
                    compileDo();
                    continue;
                case RETURN:
                    compileReturn();
                    continue;
            }
        }


    }

    public void compileLet() throws Exception {

        boolean accesArray = false;

        eat(keyWord.LET);
        String identifier = tokenizer.identifier();
        eat(tokenType.IDENTIFIER);

        if (isType(tokenType.SYMBOL, '[')){
            out.writePush(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));

            eat(tokenType.SYMBOL, '[');

            compileExpression();

            eat(tokenType.SYMBOL, ']');

            out.writeArithmetic(VMWriter.command.ADD);

            accesArray = true;
        }

        eat(tokenType.SYMBOL, '=');

        compileExpression();

        if (accesArray){
            out.writePop(VMWriter.segment.TEMP, 0);
            out.writePop(VMWriter.segment.POINTER, 1);
            out.writePush(VMWriter.segment.TEMP, 0);
            out.writePop(VMWriter.segment.THAT, 0);
        }
        else {
            out.writePop(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));
        }

        eat(tokenType.SYMBOL, ';');


    }

    public void compileIf() throws Exception {
        int label1 = labelIndex;
        int label2 = labelIndex + 1;
        labelIndex += 2;

        eat(keyWord.IF);

        eat(tokenType.SYMBOL, '(');

        compileExpression();

        eat(tokenType.SYMBOL, ')');

        out.writeArithmetic(VMWriter.command.NOT);

        out.writeIf("L" + label1);

        eat(tokenType.SYMBOL, '{');

        compileStatements();

        eat(tokenType.SYMBOL, '}');

        out.writeGoto("L" + label2);

        out.writeLabel("L" + label1);

        if (isType(tokenType.KEYWORD)  && isKeyWords(keyWord.ELSE)){
            eat(keyWord.ELSE);

            eat(tokenType.SYMBOL, '{');

            compileStatements();

            eat(tokenType.SYMBOL, '}');
        }

        out.writeLabel("L" + label2);



    }

    public void compileWhile() throws Exception {
        int label1 = labelIndex;
        int label2 = labelIndex + 1;
        labelIndex += 2;
        out.writeLabel("L" + label1);
        eat(keyWord.WHILE);

        eat(tokenType.SYMBOL, '(');

        compileExpression();

        eat(tokenType.SYMBOL, ')');

        out.writeArithmetic(VMWriter.command.NOT);

        out.writeIf("L" + label2);

        eat(tokenType.SYMBOL, '{');

        compileStatements();

        eat(tokenType.SYMBOL, '}');

        out.writeGoto("L" + label1);

        out.writeLabel("L" + label2);


    }

    public void compileDo() throws Exception {


        eat(keyWord.DO);

        // subroutine call
        String identifier = tokenizer.identifier();
        eat(tokenType.IDENTIFIER);

        if (isType(tokenType.SYMBOL, '(')){
            eat(tokenType.SYMBOL, '(');

            int nArgs = compileExpressionList();

            eat(tokenType.SYMBOL, ')');

            out.writePush(VMWriter.segment.POINTER, 0);
            nArgs++;

            out.writeCall(className + "." + identifier, nArgs);
        }
        else if (isType(tokenType.SYMBOL, '.')){
            eat(tokenType.SYMBOL, '.');
            String methodName = tokenizer.identifier();
            eat(tokenType.IDENTIFIER);

            int nArgs = 0;

            if (!methodName.equals("new") && Objects.equals(identifier, className)){
                out.writePush(VMWriter.segment.POINTER, 0);
                nArgs += 1;
            }
            else if (symbolTable.KindOf(identifier) != SymbolTable.kind.NONE){
                out.writePush(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));
                nArgs += 1;
            }

            eat(tokenType.SYMBOL, '(');

            nArgs += compileExpressionList();

            eat(tokenType.SYMBOL, ')');


            if (symbolTable.KindOf(identifier) != SymbolTable.kind.NONE){
                out.writeCall(symbolTable.typeOf(identifier) + "." + methodName, nArgs);
            }
            else {
                out.writeCall(identifier + "." + methodName, nArgs);
            }
        }
        else{
            throw new IOException("Could not find subroutineCall, found: " + tokenizer.tokenType());
        }

        out.writePop(VMWriter.segment.TEMP, 0);

        eat(tokenType.SYMBOL, ';');



    }

    public void compileReturn() throws Exception {


        eat(keyWord.RETURN);

        if (isType(tokenType.SYMBOL) && isType(tokenType.SYMBOL, ';')){
            out.writePush(VMWriter.segment.CONSTANT, 0);
        }

        compileExpression();

        eat(tokenType.SYMBOL, ';');

        out.writeReturn();


    }

    public void compileExpression() throws Exception {
        if (isType(tokenType.SYMBOL) && isType(tokenType.SYMBOL, ';')){
            return;
        }



        compileTerm();

        while (isType(tokenType.SYMBOL, '+', '-', '*', '/', '&', '|', '<', '>', '=', '~')){
            char symbol = tokenizer.symbol();
            eatSymbols('+', '-', '*', '/', '&', '|', '<', '>', '=', '~');

            compileTerm();

            if (symbol == '/') {
                out.writeCall("Math.divide", 2);
                continue;
            }
            else if (symbol == '*') {
                out.writeCall("Math.multiply", 2);
                continue;
            }

            out.writeArithmetic(VMWriter.command.getCommand(symbol));
        }


    }

    public void compileTerm() throws Exception {

        switch (tokenizer.tokenType()){
            case KEYWORD:
                if (isKeyWords(keyWord.TRUE, keyWord.FALSE, keyWord.NULL, keyWord.THIS)){
                    switch (tokenizer.keyWord()){
                        case TRUE:
                            out.writePush(VMWriter.segment.CONSTANT, 0);
                            out.writeArithmetic(VMWriter.command.NOT);
                            break;
                        case FALSE:
                            out.writePush(VMWriter.segment.CONSTANT, 0);
                            break;
                        case NULL:
                            //FIXME: I DON'T KNOW IF THIS IS RIGHT
                            out.writePush(VMWriter.segment.CONSTANT, 0);
                            break;
                        case THIS:
                            out.writePush(VMWriter.segment.POINTER, 0);
                            break;
                    }
                    eatKeyWords(keyWord.TRUE, keyWord.FALSE, keyWord.NULL, keyWord.THIS);

                    return;
                }
                throw new IOException("this is not right");
            case SYMBOL:
                if (isType(tokenType.SYMBOL, '(')){
                    eat(tokenType.SYMBOL, '(');
                    compileExpression();
                    eat(tokenType.SYMBOL, ')');

                }
                else if (isType(tokenType.SYMBOL, '{')){
                    compileSubroutineBody();

                }
                else if (isType(tokenType.SYMBOL, '-', '~')){
                    char symbol = tokenizer.symbol();
                    eatSymbols('-', '~');
                    compileTerm();

                    switch (symbol){
                        case '-':
                            out.writeArithmetic(VMWriter.command.NEG);
                        case '~':
                            out.writeArithmetic(VMWriter.command.NOT);
                    }

                }

                return;
            case IDENTIFIER:
                String identifier = tokenizer.identifier();
                eat(tokenType.IDENTIFIER);
                if (isType(tokenType.SYMBOL, '[')){
                    out.writePush(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));
                    eat(tokenType.SYMBOL, '[');
                    compileExpression();
                    eat(tokenType.SYMBOL, ']');
                    out.writeArithmetic(VMWriter.command.ADD);
                    out.writePop(VMWriter.segment.POINTER, 1);
                    out.writePush(VMWriter.segment.THAT, 0);
                    out.writePop(VMWriter.segment.TEMP, 0);
                    out.writePush(VMWriter.segment.TEMP, 0);

                    //TODO
                    return;
                }
                else if (isType(tokenType.SYMBOL, '(')){
                    eat(tokenType.SYMBOL, '(');

                    int nArgs = compileExpressionList();

                    eat(tokenType.SYMBOL, ')');

                    out.writeCall(identifier, nArgs);
                    return;
                }
                else if (isType(tokenType.SYMBOL, '.')){


                    eat(tokenType.SYMBOL, '.');

                    String methodName = tokenizer.identifier();
                    eat(tokenType.IDENTIFIER);

                    int nArgs = 0;

                    if (!methodName.equals("new") && Objects.equals(identifier, className)){
                        out.writePush(VMWriter.segment.POINTER, 0);
                        nArgs += 1;
                    }
                    else if (symbolTable.KindOf(identifier) != SymbolTable.kind.NONE){
                        out.writePush(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));
                        nArgs += 1;
                    }

                    eat(tokenType.SYMBOL, '(');

                    nArgs += compileExpressionList();

                    eat(tokenType.SYMBOL, ')');


                    if (symbolTable.KindOf(identifier) != SymbolTable.kind.NONE){
                        out.writeCall(symbolTable.typeOf(identifier) + "." + methodName, nArgs);
                    }
                    else {
                        out.writeCall(identifier + "." + methodName, nArgs);
                    }
                    return;
                }
                out.writePush(symbolTable.KindOf(identifier).segment, symbolTable.indexOf(identifier));
                return;
            case INT_CONST:
                out.writePush(VMWriter.segment.CONSTANT, tokenizer.intVal());
                eat(tokenType.INT_CONST);
                return;
            case STRING_CONST:
                out.writePush(VMWriter.segment.CONSTANT, tokenizer.stringVal().length());
                out.writeCall("String.new", 1);
                for (char c : tokenizer.stringVal().toCharArray()){
                    out.writePush(VMWriter.segment.CONSTANT, (int) c);
                    out.writeCall("String.appendChar", 2);
                }
                eat(tokenType.STRING_CONST);

                return;
        }


    }

    public int compileExpressionList() throws Exception {
        if (!isType(tokenType.IDENTIFIER, tokenType.INT_CONST, tokenType.STRING_CONST, tokenType.SYMBOL, tokenType.KEYWORD)){


            return 0;
        }
        int nArgs = 0;


        while (isType(tokenType.IDENTIFIER, tokenType.INT_CONST, tokenType.STRING_CONST, tokenType.KEYWORD) || isType(tokenType.SYMBOL, '(')){
            compileExpression();
            nArgs++;

            while (isType(tokenType.SYMBOL, ',')){
                eat(tokenType.SYMBOL, ',');
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;



    }


    private void eat(keyWord keyword) throws IOException {
        if (tokenizer.keyWord() != keyword){
            throw new IllegalStateException("KeyWord does not match!\n" + "Expected: " + tokenizer.keyWord()  + "\n" + "Got: " + keyword + "\n");
        }


        tokenizer.advance();
    }

    private void eatNewId() throws Exception {
        if (tokenizer.tokenType() != tokenType.IDENTIFIER){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.tokenType() + "\n" + "Got: " + tokenType.IDENTIFIER + "\n");
        }
        String identifier = tokenizer.identifier();
        String kind = getKind();
        if (Objects.equals(kind, "STATIC") || Objects.equals(kind, "FIELD") || Objects.equals(kind, "ARG") || Objects.equals(kind, "VAR")){
            int index = symbolTable.indexOf(identifier);

        }
        else {

        }

        tokenizer.advance();
    }

    private void eat(tokenType type) throws IOException {
        if (tokenizer.tokenType() != type){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.tokenType() + "\n" + "Got: " + type + "\n");
        }

        if (tokenizer.tokenType() == tokenType.STRING_CONST){

        }
        else if (tokenizer.tokenType() == tokenType.IDENTIFIER){
            String identifier = tokenizer.identifier();
            String kind = getKind();


        }
        else {

        }

        tokenizer.advance();
    }

    private String getKind() throws IOException {
        String identifier = tokenizer.identifier();
        switch (symbolTable.KindOf(identifier)){
            case STATIC:
            case FIELD:
            case ARG:
            case VAR:
                return symbolTable.KindOf(identifier).toString();
            case NONE:
                if (identifier.equals(className) || identifier.equals("Array") || identifier.equals("String")){
                    return "Class";
                }
                else {
                    return "subroutine";
                }
            default:
                throw new IOException("Huh, this is not a kind :(");
        }
    }

    private void eat(tokenType type, char symbol) throws IOException {
        if (tokenizer.tokenType() != type){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.tokenType() + "\n" + "Got: " + type + "\n");
        }

        if (tokenizer.symbol() != symbol){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.symbol() + "\n" + "Got: " + symbol + "\n");
        }


        tokenizer.advance();
    }

    private void eat(boolean advance, tokenType type, char symbol) throws IOException {
        if (tokenizer.tokenType() != type){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.tokenType() + "\n" + "Got: " + type + "\n");
        }

        if (tokenizer.symbol() != symbol){
            throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.symbol() + "\n" + "Got: " + symbol + "\n");
        }


        if (advance) {
            tokenizer.advance();
        }
    }

    private void eatSymbols(char... symbols) throws IOException {
        for (char symbol : symbols){
            if (tokenizer.tokenType() == tokenType.SYMBOL && tokenizer.symbol() == symbol){
                eat(tokenType.SYMBOL, symbol);
                return;
            }
        }
        throw new IllegalStateException("Symbol not found!");
    }

    private boolean isKeyWords(keyWord... keyWords){
        for (keyWord keyword : keyWords){
            if (tokenizer.keyWord() == keyword){
                return true;
            }
        }
        return false;
    }

    private boolean isType(tokenType type){
        return tokenizer.tokenType() == type;
    }

    private boolean isType(tokenType type, char symbol){
        return tokenizer.tokenType() == type && tokenizer.symbol() == symbol;
    }

    private boolean isType(tokenType type, char... symbols){
        for (char symbol : symbols){
            if (tokenizer.tokenType() == type && tokenizer.symbol() == symbol){
                return true;
            }
        }
        return false;
    }

    private boolean isType(tokenType... types){
        for (tokenType type : types){
            if (tokenizer.tokenType() == type){
                return true;
            }
        }
        return false;
    }

    /**
     * given all the keywords see eat the correct one, or an identifier if countIdentifier is set to true.
     *
     * @param keyWords the keywords that when found want to eat
     * @throws IOException when none is found.
     */
    private void eatKeyWords(boolean countIdentifier, keyWord... keyWords) throws IOException {
        if (tokenizer.tokenType() == tokenType.IDENTIFIER && countIdentifier){
            eat(tokenType.IDENTIFIER);
            return;
        }
        for (keyWord keyword : keyWords){
            if (tokenizer.keyWord() == keyword){
                eat(keyword);
                return;
            }
        }
        throw new IllegalStateException("Token type does not match!\n" + "Expected: " + tokenizer.tokenType() + "\n" + "But didn't find it :(\n");
    }
    /**
     * given all the keywords see eat the correct one.
     * @param keyWords the keywords that when found want to eat
     * @throws IOException when none is found.
     */
    private void eatKeyWords(keyWord... keyWords) throws IOException {
        eatKeyWords(false, keyWords);
    }







}
