package com.github.forax.pratt_parser;

import static com.github.forax.pratt_parser.Main.Precedence.P_ADD;
import static com.github.forax.pratt_parser.Main.Precedence.P_MUL;
import static com.github.forax.pratt_parser.Main.Precedence.P_NONE;
import static com.github.forax.pratt_parser.Main.Token.*;
import static java.util.Map.entry;

import java.util.Map;

public class Main {
  enum Token { PLUS, MINUS, STAR, LPAR, RPAR, NUM, EOF }
  enum Precedence { P_NONE, P_ADD, P_MUL }
  
  public static void main(String[] args) {
    var text = "+ 2 + + 3 * - (- 4)";
    var lexer = Lexer.factory(EOF,
        entry(PLUS,  "\\+"),
        entry(MINUS, "\\-"),
        entry(STAR,  "\\*"),
        entry(LPAR,  "\\("),
        entry(RPAR,  "\\)"),
        entry(NUM,   "[0-9]+")
      ).apply(text);
    
    Parser<Integer, Precedence> parser = Parser.create(lexer, 
        Map.of(EOF, P_NONE, RPAR, P_NONE, PLUS, P_ADD, STAR, P_MUL)::get,
        Precedence::compareTo,
        Map.of(
            PLUS,  p -> p.parseExpr(P_NONE),
            MINUS, p -> - p.parseExpr(P_NONE),
            LPAR,  p -> { int v = p.parseExpr(P_NONE); lexer.consume(RPAR); return v; }, 
            NUM,   p -> Integer.parseInt(lexer.value())),
        Map.of(
            PLUS,  (p, left) -> left + p.parseExpr(P_ADD),
            STAR,  (p, left) -> left * p.parseExpr(P_MUL)));
    
    var result = parser.parseExpr(P_NONE);
    System.out.println(result);
  }
}
