package com.github.forax.pratt_parser;

import static java.util.Map.entry;
import static com.github.forax.pratt_parser.LexerExampleTests.Token.*;

import java.util.function.Function;

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
    Function<CharSequence, Lexer<Token>> factory =
        Lexer.factory(EOF, 
          entry(COMMA, ","),
          entry(ID,    "[a-z]+")
        );
    
    Lexer<Token> lexer = factory.apply("hello, boy");
    
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
