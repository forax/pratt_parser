package com.github.forax.pratt_parser;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ParserExampleTests {
  @Test
  void test() {
    String text = "+ 2 + + 3 * - (- 4)";
    Lexer<String> lexer = Lexer.factory("$",
        entry("+",   "\\+"),
        entry("-",   "\\-"),
        entry("*",   "\\*"),
        entry("(",   "\\("),
        entry(")",   "\\)"),
        entry("num", "[0-9]+")
      ).apply(text);

    Parser<Integer, Integer> parser = Parser.create(lexer, 
        Map.of("$", 0, ")", 0, "+", 1, "*", 2)::get,
        Integer::compareTo,
        Map.of(
            "+",   p -> p.parseExpr(0),
            "-",   p -> - p.parseExpr(0),
            "(",   p -> { int v = p.parseExpr(0); lexer.consume(")"); return v; }, 
            "num", p -> Integer.parseInt(lexer.value())),
        Map.of(
            "+",   (p, left) -> left + p.parseExpr(1),
            "*",   (p, left) -> left * p.parseExpr(2)));

    int result = parser.parseExpr(0);
    assertEquals(14, result);
  }
}
