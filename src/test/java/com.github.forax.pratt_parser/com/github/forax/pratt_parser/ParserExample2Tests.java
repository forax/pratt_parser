package com.github.forax.pratt_parser;

import static com.github.forax.pratt_parser.ParserExample2Tests.Precedence.P_ADD;
import static com.github.forax.pratt_parser.ParserExample2Tests.Precedence.P_MUL;
import static com.github.forax.pratt_parser.ParserExample2Tests.Precedence.P_NONE;
import static com.github.forax.pratt_parser.ParserExample2Tests.Token.*;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

import com.github.forax.pratt_parser.ParserExample2Tests.Expr.BinOp;
import com.github.forax.pratt_parser.ParserExample2Tests.Expr.Literal;
import com.github.forax.pratt_parser.ParserExample2Tests.Expr.UnOp;

@SuppressWarnings("static-method")
class ParserExample2Tests {
  interface Expr {
    class Literal implements Expr {
      final int value;
      Literal(int value) { this.value = value; }
    }
    class UnOp implements Expr {
      final IntUnaryOperator op; final Expr expr;
      UnOp(IntUnaryOperator op, Expr expr) { this.op = op; this.expr = expr; }
    }
    class BinOp implements Expr {
      final IntBinaryOperator op; final Expr left; final Expr right;
      BinOp(IntBinaryOperator op, Expr left, Expr right) { this.op = op; this.left =  left; this.right = right; }
    }
  }
  
  interface Visitor<R> {
    interface Registry<R> {
      <T> Registry<R> when(Class<T> type, BiFunction<? super Visitor<? extends R>, ? super T, ? extends R> fun);  
    }
    
    R visit(Object o);
    
    static <R> Visitor<R> create(Consumer<? super Registry<R>> consumer) {
      HashMap<Class<?>, Function<Object, ? extends R>> map = new HashMap<>();
      Visitor<R> visitor = object -> map.get(object.getClass()).apply(object);
      consumer.accept(new Registry<>() {
        @Override
        public <T> Registry<R> when(Class<T> type, BiFunction<? super Visitor<? extends R>, ? super T, ? extends R> fun) {
          map.put(type, object -> fun.apply(visitor, type.cast(object)));
          return this;
        }
      });
      return visitor;
    }
  }
  
  enum Token { PLUS, MINUS, STAR, LPAR, RPAR, NUM, EOF }
  enum Precedence { P_NONE, P_ADD, P_MUL }
  
  @Test
  void test() {
    String text = "+ 2 + + 3 * - (- 4)";
    Lexer<Token> lexer = Lexer.factory(EOF,
        entry(PLUS,  "\\+"),
        entry(MINUS, "\\-"),
        entry(STAR,  "\\*"),
        entry(LPAR,  "\\("),
        entry(RPAR,  "\\)"),
        entry(NUM,   "[0-9]+")
      ).apply(text);

    Parser<Expr, Precedence> parser = Parser.create(lexer, 
        Map.of(EOF, P_NONE, RPAR, P_NONE, PLUS, P_ADD, STAR, P_MUL)::get,
        Precedence::compareTo,
        Map.of(
            PLUS,  p -> new UnOp(x -> x, p.parseExpr(P_NONE)),
            MINUS, p -> new UnOp(x -> -x, p.parseExpr(P_NONE)),
            LPAR,  p -> { Expr expr = p.parseExpr(P_NONE); lexer.consume(RPAR); return expr; }, 
            NUM,   p -> new Literal(Integer.parseInt(lexer.value()))),
        Map.of(
            PLUS,  (p, left) -> new BinOp((a, b) -> a + b, left, p.parseExpr(P_ADD)),
            STAR,  (p, left) -> new BinOp((a, b) -> a * b, left, p.parseExpr(P_MUL))));
    Expr expr = parser.parseExpr(P_NONE);
    
    Visitor<Integer> visit = Visitor.create(registry -> registry
       .when(Literal.class, (v, literal) -> literal.value)
       .when(UnOp.class,    (v, unOp)    -> unOp.op.applyAsInt(v.visit(unOp.expr)))
       .when(BinOp.class,   (v, binOp)   -> binOp.op.applyAsInt(v.visit(binOp.left), v.visit(binOp.right)))
       );
    int result = visit.visit(expr);
    
    assertEquals(14, result);
  }
}
