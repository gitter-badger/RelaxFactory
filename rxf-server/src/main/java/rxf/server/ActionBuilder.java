package rxf.server;

import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

import static rxf.server.driver.CouchMetaDriver.CONTENT_LENGTH;
import static rxf.server.driver.CouchMetaDriver.ETAG;

/**
 * User: jim
 * Date: 5/29/12
 * Time: 1:58 PM
 */
public abstract class ActionBuilder<T> {

  private AtomicReference<Rfc822HeaderState> state = new AtomicReference<Rfc822HeaderState>();
  private SelectionKey key;
  protected static ThreadLocal<ActionBuilder<?>> currentAction = new InheritableThreadLocal<ActionBuilder<?>>();
  private SynchronousQueue[] synchronousQueues;

  public ActionBuilder(SynchronousQueue... synchronousQueues) {
    this.synchronousQueues = synchronousQueues;
    currentAction.set(this);
  }


  private SynchronousQueue[] many(SynchronousQueue... ts) {
    return ts;
  }

  @Override
  public String toString() {
    return "ActionBuilder{" +
        "state=" + state +
        ", key=" + key +
        ", synchronousQueues=" + (synchronousQueues == null ? null : Arrays.asList(synchronousQueues)) +
        '}';
  }

  public Rfc822HeaderState state() {
    Rfc822HeaderState ret = this.state.get();
    if (null == ret) state.set(ret = new Rfc822HeaderState(ETAG, CONTENT_LENGTH));
    return ret;
  }

  public SelectionKey key() {
    return this.key;
  }

  abstract protected TerminalBuilder<T> fire() throws Exception;


  public ActionBuilder<T> state(Rfc822HeaderState state) {
    this.state.set(state);
    return this;
  }


  public ActionBuilder<T> key(SelectionKey key) {
    this.key = key;

    return this;
  }

  public static <T> ActionBuilder<T> get() {
    return (ActionBuilder<T>) currentAction.get();
  }

  public ActionBuilder<T> sync(SynchronousQueue... ts) {
    this.synchronousQueues = ts;
    return this;
  }
}
