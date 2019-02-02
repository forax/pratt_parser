package com.github.forax.pratt_parser;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

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
    var factory = Lexer.factory("eof", entry("token", "[a-z]+"));
    var lexer = factory.apply("hello bob");
    lexer.consume("token");
    assertEquals("hello", lexer.value());
    lexer.consume("token");
    assertEquals("bob", lexer.value());
    lexer.consume("eof");
    assertNull(lexer.value());
  }
  
  @Test
  void matchEmptyText() {
    var factory = Lexer.factory("eof", entry("token", "[a-z]+"));
    var lexer = factory.apply("");
    lexer.consume("eof");
    assertNull(lexer.value());
  }
  
  @Test
  void matchNoSeparator() {
    var factory = Lexer.factory("$",
        entry("a", "a"), entry("b", "b"));
    var lexer = factory.apply("abaab");
    lexer.consume("a");
    lexer.consume("b");
    lexer.consume("a");
    lexer.consume("a");
    lexer.consume("b");
    lexer.consume("$");
  }
  
  @Test
  void matchNullText() {
    var factory = Lexer.factory("$", entry("token", "[0-9]+"));
    assertThrows(NullPointerException.class, () -> factory.apply(null));
  }
  
  @Test
  void matchFirstRegex() {
    var factory = Lexer.factory("$",
        entry("goto",  "goto"),
        entry("token", "[a-z]+"));
    var lexer = factory.apply("goto");
    lexer.consume("goto");
    assertEquals("goto", lexer.value());
    lexer.consume("$");
  }
  
  @Test
  void matchFirstRegex2() {
    var factory = Lexer.factory("$",
        entry("token", "[a-z]+"),
        entry("goto",  "goto"));
    var lexer = factory.apply("goto");
    lexer.consume("token");
    assertEquals("goto", lexer.value());
    lexer.consume("$");
  }
}
