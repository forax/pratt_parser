package com.github.forax.pratt_parser;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ParserTests {
  @Test
  void commaSeperatedList() {
    Lexer<String> lexer = Lexer.factory("$",
        entry(",",   ","),
        entry("id", "[a-z]+")
      ).apply("a, b, c");

    Parser<List<String>, Integer> parser = Parser.create(lexer, 
        Map.of("$", 0, ",", 1)::get,
        Integer::compareTo,
        Map.of(
            "id", p -> new ArrayList<>(List.of(lexer.value()))),
        Map.of(
            ",", (p, left) -> { left.addAll(p.parseExpr(1)); return left; })
        );

    List<String> result = parser.parseExpr(0);
    assertEquals(List.of("a", "b", "c"), result);
  }
  
  @Test
  void nullArguments() {
    Lexer<String> lexer = Lexer.factory("$", Map.entry("a", "a")).apply("a");
    assertThrows(NullPointerException.class, () -> Parser.create(null , __ -> 0, Integer::compareTo, Map.of(), Map.of()));
    assertThrows(NullPointerException.class, () -> Parser.create(lexer, null   , Integer::compareTo, Map.of(), Map.of()));
    assertThrows(NullPointerException.class, () -> Parser.create(lexer, __ -> 0, null              , Map.of(), Map.of()));
    assertThrows(NullPointerException.class, () -> Parser.create(lexer, __ -> 0, Integer::compareTo, null    , Map.of()));
    assertThrows(NullPointerException.class, () -> Parser.create(lexer, __ -> 0, Integer::compareTo, Map.of(), null    ));
  }
  
  @Test
  void unknownToken() {
    Lexer<String> lexer = Lexer.factory("$", entry("a", "a")).apply("a");
    Parser<Integer, Integer> parser = Parser.create(lexer, 
        __ -> 0,
        Integer::compareTo,
        Map.of(
            "b",   p -> fail("should not reach here")),
        Map.of()
        );
    assertThrows(IllegalStateException.class, () -> parser.parseExpr(0));
  }
  
  @Test
  void noPrefix() {
    Lexer<String> lexer = Lexer.factory("$", entry("a", "a")).apply("a");
    Parser<Integer, Integer> parser = Parser.create(lexer, 
        __ -> 0,
        Integer::compareTo,
        Map.of(),  // empty !
        Map.of()
        );
    assertThrows(IllegalStateException.class, () -> parser.parseExpr(0));
  }
  
  @Test
  void noSuffix() {
    Lexer<String> lexer = Lexer.factory("$",
        entry("-",   "\\-"),
        entry("num", "[0-9]+")
      ).apply("--3");

    Parser<Integer, Integer> parser = Parser.create(lexer, 
        Map.of("$", 0)::get,
        Integer::compareTo,
        Map.of(
            "-",   p -> - p.parseExpr(0),
            "num", p -> Integer.parseInt(lexer.value())),
        Map.of()
        );

    int result = parser.parseExpr(0);
    assertEquals(3, result);
  }
}
