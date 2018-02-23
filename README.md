# pratt_parser

Everything starts when i was discussing with a colleague of mine about how parser are not very easy to update dynamically.
He mentions Pratt parser, a kind of parsers i was not aware of.
A kick Google search on that subject links to an article of Munificient Bob about Pratt Parser
()
I really like the articles from Munificient Bob, it's always well written, it's a pleasure to read.

But, for the code provided, well, hugh, it's embarrassing, let say it's a code written in 2006, a long time for Java,
so here is my version of how to write a Pratt Parser in Java 9.

The idea behind a Pratt parser is that when you want to parse an expression, you have two kind of productions
either a production that starts with a token, a prefix expression, like this
```
  expr := '-' expr
``` 
or a production that starts with an expression and is followed by a token, a suffix expression, like this
```
  expr := expr '*' expr
```

When parsing an expression, you can decompose the parsing in two phases,
1) parse prefix expressions, using a Map that associate a token that starts the prefix expression and a function
   that explicit how to parse that expression (that function is called a Parselet)
2) parse suffix expression, also using a Map that associate a token that starts the suffix expression and
   a Parselet (this parselet has a slighly different signatures because it takes the first expression as parameter). 

It's a little more complex than that because you can have productions like these
```
  expr := expr '+' expr
  expr := expr '*' expr
```
and a text like this
```
  2 + 3 * 5
```
An expression like the above can be parsed either as (2 + 3) * 5 or as 2 + (3 * 5),
the first case is obtained by stopping the recursion and returning the expression, it's called a reduction of the production expr + expr,
the second case is obtained by reading the next token ('*') and the rest of the expression, it's called a shift by '*'.

To decide how to parse such text, the idea is that the parsing of current expression take a number (the precedence) as argument
(the precedence of the expression it tries to parse) and the tokens that can follow an expression (the one that can be shifted)
are also numbered, these two numbers are compared and the greater one decide if the parser should do a reduction or a shift.

For that the parser need to be able to ask to the lexer what is the next token without consume it,
this is done by the method lookhead() of the lexer.

So a Pratt parser is created with the following arguments
```java
 Parser<E,P> create(Lexer<T> lexer,
                    Function<T,P> precedenceFun,
                    Comparator<P> comparator,
                    Map<T,PrefixParselet<E,P>> prefixMap,
                    Map<T,SuffixParselet<E,P>> suffixMap)
```

- precedenceFun is the function that returns the precedence for a token that can appear after an expression,
- comparator is able to compare two precedences
- prefixMap associate the prefix parselet to the first token of the prefix expression it recognizes
- suffixMap associate the suffix parselet to the token of the suffix expression it recognizes.

and the parsing algorithm is this one: 
```java
public E parseExpr(P precedence) {
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
```
and that all, amazing isn't it !

Here is an example of how to configure and use the Pratt parser to parse expression with
numbers, unary '+', unary '-', binary '+', binary '*' and parenthesis. 

```java
    enum Token { PLUS, MINUS, STAR, LPAR, RPAR, NUM, EOF }
    enum Precedence { P_NONE, P_ADD, P_MUL }
    ...
    
    String text = ...
    Lexer<Token> lexer = Lexer.factory(EOF,
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
    
    int result = parser.parseExpr(P_NONE);
    System.out.println(result);
```

There is also a version
- without enums (ParserExampleTests) or
- that create an AST an evaluate it using a visitor (ParserExample2Tests).

