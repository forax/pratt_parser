package com.github.forax.pratt_parser;

import static com.github.forax.pratt_parser.LexerExampleTests.Token.COMMA;
import static com.github.forax.pratt_parser.LexerExampleTests.Token.EOF;
import static com.github.forax.pratt_parser.LexerExampleTests.Token.ID;
import static java.util.Map.entry;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class LexerExampleTests {
  enum Token {
    EOF,
    ID,
    COMMA
  }
  
  @Test
  void test() {
    var factory =
        Lexer.factory(EOF, 
          entry(COMMA, ","),
          entry(ID,    "[a-z]+")
        );
    
    var lexer = factory.apply("hello, boy");
    
    /*
    Token token;
    while((token = lexer.consume()) != Token.EOF) {
      System.out.println(token);
    }*/
    
    lexer.consume(ID);
    lexer.consume(COMMA);
    lexer.consume(ID);
    lexer.consume(EOF);
  }
}
