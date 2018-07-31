import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

resolver.
    dependencies(
        "org.junit.jupiter.api:5.2.0",
        "org.junit.platform.commons:1.2.0",
        "org.apiguardian.api:1.0.0",
        "org.opentest4j:1.0.0"
    )

docer.
    quiet(true).
    link(uri("https://docs.oracle.com/javase/9/docs/api/"))

packager.
    modules("com.github.forax.pratt_parser@1.0")   
    
run(resolver, compiler, tester, docer, packager)

/exit
