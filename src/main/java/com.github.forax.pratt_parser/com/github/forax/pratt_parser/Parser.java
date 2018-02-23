package com.github.forax.pratt_parser;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Parse the tokens from a lexer to create expressions using Pratt's algorithm.
 *
 * @param <E> type of the expressions.
 * @param <P> type of the precedences.
 */
public interface Parser<E, P> {
  /**
   * An object that knows how to parse a prefix expression.
   *
   * @param <E> type of the expressions.
   * @param <P> type of the precedences.
   */
  interface PrefixParselet<E, P> {
    /**
     * Parse a prefix expression.
     * 
     * @param parser the parser to ask to parse the expression recursively.
     * @return return the value of the parsed expression.
     */
    E parse(Parser<E,P> parser);
  }
  
  /**
   * An object that knows how to parse a suffix expression.
   *
   * @param <E> type of the expressions.
   * @param <P> type of the precedences.
   */
  interface SuffixParselet<E, P> {
    /**
     * Parse a suffix expression.
     * 
     * @param parser the parser to ask to parse the expression recursively.
     * @param left left part of the expression.
     * @return return the value of the parsed expression.
     */
    E parse(Parser<E,P> parser, E left);
  }
  
  /**
   * Parse an expression by calling the parselets ({@link PrefixParselet}, {@link SuffixParselet})
   * registered by the current parser.
   * 
   * @param precedence the precedence of the current expression.
   * @return the value of the parsed expression.
   */
  E parseExpr(P precedence);
  
  /**
   * Creates a Parser from the function that associate a precedence to a token and
   * two {@link java.util.Map}s that associate a token to a parselet.
   * 
   * @param <E> type of the expressions.
   * @param <T> type of the tokens.
   * @param <P> type of the precedences.
   * @param lexer a lexer.
   * @param precedenceFun a function that returns the precedence of a token when used in suffix position.
   * @param comparator a comparator of precedence.
   * @param prefixMap a map that associate a token in prefix position and the code to execute to parse
   *                  the corresponding prefix expression.
   * @param suffixMap a map that associate a token in suffix position and the code to execute to parse
   *                  the corresponding suffix expression.
   * @return a new parser.
   */
  static <E, T, P>
    Parser<E,P> create(Lexer<? extends T> lexer,
                       Function<? super T,?  extends P> precedenceFun, Comparator<? super P> comparator,
                       Map<T,PrefixParselet<E,P>> prefixMap,
                       Map<T,SuffixParselet<E,P>> suffixMap) {
    Objects.requireNonNull(lexer);
    Objects.requireNonNull(precedenceFun);
    Objects.requireNonNull(comparator);
    Objects.requireNonNull(prefixMap);
    Objects.requireNonNull(suffixMap);
    return new Parser<>() {
      public E parseExpr(P precedence) {
        Objects.requireNonNull(precedence);
        
        T token = lexer.consume();
        
        PrefixParselet<E,P> prefix = prefixMap.get(token);
        if (prefix == null) {
          throw new IllegalStateException("Could not parse token " +  token + " of value " + lexer.value());
        }
        E left = prefix.parse(this);

        while (comparator.compare(precedence, precedenceFun.apply(lexer.lookhead())) < 0) {
          token = lexer.consume();
          SuffixParselet<E,P> suffix = suffixMap.get(token);
          left = suffix.parse(this, left);
        }
        return left;
      }
    };
  }
}
