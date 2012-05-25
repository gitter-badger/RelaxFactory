package rxf.server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import one.xio.MimeType;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, LOCAL_VARIABLE, TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PACKAGE, PARAMETER})
public @interface DbKeys {
  enum etype {

    opaque, db, docId, rev, designDocId, view, validjson, mimetype {{
      clazz = MimeType.class;
    }}, blob {{
      clazz = ByteBuffer.class;
    }};

    <T> boolean validate(T... data) {
      return true;
    }

    Class clazz = String.class;
  }


  etype[] value();


  @Retention(RetentionPolicy.RUNTIME)
  @Target({FIELD, LOCAL_VARIABLE, TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PACKAGE, PARAMETER})
  @interface DbResultUnit {
    Class value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({FIELD, LOCAL_VARIABLE, TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PACKAGE, PARAMETER})
  @interface DbInputUnit {
    Class value();
  }

  public static abstract class ReturnAction<T> {

    static ThreadLocal<ReturnAction> currentKeys = new ThreadLocal<ReturnAction>();

  }

  abstract class ActionBuilder<T> {

    Rfc822HeaderState state;
    SelectionKey key;
    static ThreadLocal currentAction = new ThreadLocal();

    ActionBuilder() {
      currentAction.set(this);
    }

    public Rfc822HeaderState state() {
      return this.state;
    }

    public ActionBuilder<T> state(Rfc822HeaderState state) {
      this.state = state;
      return (ActionBuilder<T>) this;
    }

    public SelectionKey key() {
      return this.key;
    }

    public ActionBuilder<T> key(SelectionKey key) {
      this.key = key;

      return (ActionBuilder<T>) this;
    }

    public abstract ResultAction<T> fire();
  }

  abstract class DbKeysBuilder<T> {
    public static ThreadLocal<DbKeysBuilder> currentKeys = new ThreadLocal<DbKeysBuilder>();

    public abstract ActionBuilder<T> to();

    protected DbKeysBuilder() {
      currentKeys.set(this);
    }

    java.util.EnumMap<etype, Object> parms = new java.util.EnumMap<etype, Object>(etype.class);

    public ThreadLocal<? extends DbKeysBuilder> getCurrentKeys() {
      return this.currentKeys;
    }


    public java.util.EnumMap<etype, Object> getParms() {
      return this.parms;
    }

    public DbKeysBuilder setParms(java.util.EnumMap<etype, Object> parms) {
      this.parms = parms;
      return this;
    }
  }

//  class test {
//    public static void main(String... a) {
//      SelectionKey x = null;
//
//      getAsyncIteratorBuilder.$().opaque("").to().key(x).state(new Rfc822HeaderState()).fire();
//    }
//  }
}