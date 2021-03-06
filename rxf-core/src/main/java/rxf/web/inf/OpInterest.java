package rxf.web.inf;

import java.lang.annotation.*;
import java.nio.channels.SelectionKey;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by jim on 7/24/14.
 * <p/>
 * for Tx preloaded reads, @PreRead is redundant,
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface OpInterest {
  int value() default SelectionKey.OP_READ;
}
