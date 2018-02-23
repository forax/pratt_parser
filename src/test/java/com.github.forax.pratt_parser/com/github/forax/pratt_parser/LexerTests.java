package com.github.forax.pratt_parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class LexerTests {
  @Test
  void testWrongConfigurations() {
    assertThrows(NullPointerException.class, () -> Lexer.factory(null, Map.entry("token", "[a-z]+")));
    assertThrows(NullPointerException.class, () -> Lexer.factory("eof", (Map.Entry<String, String>[])null));
    assertThrows(IllegalArgumentException.class, () -> Lexer.factory("eof"));
  }
  
  @Test
  void matchOnlyOneKingOfToken() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("eof", Map.entry("token", "[a-z]+"));
    Lexer<String> lexer = factory.apply("hello bob");
    lexer.consume("token");
    assertEquals("hello", lexer.value());
    lexer.consume("token");
    assertEquals("bob", lexer.value());
    lexer.consume("eof");
    assertNull(lexer.value());
  }
  
  @Test
  void matchEmptyText() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("eof", Map.entry("token", "[a-z]+"));
    Lexer<String> lexer = factory.apply("");
    lexer.consume("eof");
    assertNull(lexer.value());
  }
  
  @Test
  void matchNoSeparator() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("$",
        Map.entry("a", "a"), Map.entry("b", "b"));
    Lexer<String> lexer = factory.apply("abaab");
    lexer.consume("a");
    lexer.consume("b");
    lexer.consume("a");
    lexer.consume("a");
    lexer.consume("b");
    lexer.consume("$");
  }
  
  @Test
  void matchNullText() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("$", Map.entry("token", "[0-9]+"));
    assertThrows(NullPointerException.class, () -> factory.apply(null));
  }
  
  @Test
  void matchFirstRegex() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("$",
        Map.entry("goto",  "goto"),
        Map.entry("token", "[a-z]+"));
    Lexer<String> lexer = factory.apply("goto");
    lexer.consume("goto");
    assertEquals("goto", lexer.value());
    lexer.consume("$");
  }
  
  @Test
  void matchFirstRegex2() {
    Function<CharSequence, Lexer<String>> factory = Lexer.factory("$",
        Map.entry("token", "[a-z]+"),
        Map.entry("goto",  "goto"));
    Lexer<String> lexer = factory.apply("goto");
    lexer.consume("token");
    assertEquals("goto", lexer.value());
    lexer.consume("$");
  }
}
