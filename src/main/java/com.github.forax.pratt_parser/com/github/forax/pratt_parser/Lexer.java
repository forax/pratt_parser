package com.github.forax.pratt_parser;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple lexer with a 1 token look-head.
 * 
 * @param <T> type of the tokens.
 */
public interface Lexer<T> {
  /**
   * Returns the last matched token, previously returned by a call to {@link #consume()} or {@link #lookhead()}.
   * @return the last matched token, {@code null} otherwise.
   */
  T token();
  
  /**
   * Return the value of the last matched token.
   * 
   * @return the value of the last matched token, {@code null} otherwise.
   * @see #token()
   */
  String value();
  
  /**
   * Parse the next token from the text and return it.
   * @return the next token from the text.
   */
  T consume();
  
  /**
   * Returns the token that will be returned by {@link #consume()} without consume it.
   * @return the token that will be returned by {@link #consume()} without consume it.
   */
  T lookhead();
  
  /**
   * Consume the next token and check that it matches the token pass as argument.
   * @param token the token to match.
   * @throws IllegalStateException if the next token doesn't match the token pass as argument.
   */
  default void consume(T token) {
    T current = consume();
    if (!token.equals(current)) {
      throw new IllegalStateException("parsing error " + current + " but should be " + token);
    }
  }
  
  /**
   * Returns a lexer factory that takes a text an returns a lexer that will split the text into tokens
   * following the pairs of token/regex.
   * 
   * @param <T> type of the tokens.
   * @param eof the symbol to return at the end of the text.
   * @param regexes pairs of token/regex, during the parsing, if a regex matches, corresponding token will be
   *        returned by {@link Lexer#consume()}.
   * @return a lexer factory that takes a text an returns a lexer configured by the pairs token/regex.
   * @throws NullPointerException if the array of {@code regexes} is null.
   * @throws IllegalArgumentException if the array of {@code regexes} is empty.
   */
  @SafeVarargs
  static <T> Function<CharSequence, Lexer<T>> factory(T eof, Entry<T, String>... regexes) {
    Objects.requireNonNull(eof);
    if (regexes.length == 0) {
      throw new IllegalArgumentException("no token/regex pair specified");
    }
    Pattern pattern = Pattern.compile(Arrays.stream(regexes).map(e -> '(' + e.getValue() + ')').collect(joining("|")));
    return input -> {
      Matcher matcher = pattern.matcher(input);
      return new Lexer<>() {
        private T token;
        private String value;
        private boolean lookhead;
        
        @Override
        public T token() { return token; }
        @Override
        public String value() { return value; }

        @Override
        public T lookhead() {
          if(lookhead) {
            return token;
          }
          lookhead = true;
          return next();
        }
        
        @Override
        public T consume() {
          if(lookhead) {
            lookhead = false;
            return token;
          }
          return next();
        }
        
        private T next() {
          if (!matcher.find()) {
            value = null;
            return token = eof;
          }

          for(int i = 0; i < matcher.groupCount(); i++) {
            String value = matcher.group(i + 1);
            if (value != null) {
              this.value = value;
              return this.token = regexes[i].getKey();
            }
          }
          throw new RuntimeException("no match at " + matcher.start());
        }
      };
    };
  }
}
