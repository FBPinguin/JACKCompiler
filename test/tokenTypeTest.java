import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

class tokenTypeTest {

    @Test
    void getTokenType() {
        assertEquals(tokenType.IDENTIFIER, tokenType.getTokenType("Hello"));
        assertEquals(tokenType.KEYWORD, tokenType.getTokenType("class"));
        assertEquals(tokenType.KEYWORD, tokenType.getTokenType("null"));
        assertEquals(tokenType.KEYWORD, tokenType.getTokenType("false"));
        assertEquals(tokenType.INT_CONST, tokenType.getTokenType("124323409"));
        assertEquals(tokenType.STRING_CONST, tokenType.getTokenType("\"123457890\""));
        assertEquals(tokenType.SYMBOL, tokenType.getTokenType("."));
    }
}