package rxf.couch.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import one.xio.AsioVisitor;
import one.xio.AsyncSingletonServer;
import org.junit.*;
import rxf.couch.CouchService;
import rxf.couch.gen.CouchDriver;
import rxf.rpc.RelaxFactoryServerImpl;
import rxf.rpc.RpcHelper;
import rxf.shared.CouchTx;
import rxf.web.inf.ProtocolMethodDispatch;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class CouchServiceProviderTest {
  private static ScheduledExecutorService exec;

  @BeforeClass
  public static void setUp() throws Exception {
    RpcHelper.setDEBUG_SENDJSON(true);
    AsyncSingletonServer.killswitch.set(false);
    exec = Executors.newScheduledThreadPool(2);
    exec.submit(new Runnable() {
      public void run() {
        AsioVisitor topLevel = new ProtocolMethodDispatch();
        try {
          RelaxFactoryServerImpl.init(topLevel/* , 1000 */);

        } catch (Exception e) {
          Assert.fail();
        }
      }
    });
    new Timer().schedule(new TimerTask() {
      public void run() {
        Assert.fail();
      }
    }, 5000);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    try {
      AsyncSingletonServer.killswitch.set(true);
      AsioVisitor.Helper.getSelector().close();
      exec.shutdown();
    } catch (Exception ignore) {
    }
  }

  Injector i;
  private String db;

  @Before
  public void before() {
    i = Guice.createInjector(new TestModule());
    db =
        i.getInstance(Key.get(String.class, Names.named(CouchModuleBuilder.NAMESPACE)))
            + GuiceTest.class.getSimpleName().toLowerCase();
    new CouchDriver.DbCreate().db(db).to().fire().tx();

  }

  @After
  public void after() {
    new CouchDriver.DbDelete().db(db).to().fire().tx();
  }

  public static class TestModule extends AbstractModule {

    protected void configure() {
      install(new CouchModuleBuilder("test" + System.currentTimeMillis() + "_").withEntity(
          GuiceTest.class).withService(SimpleCouchService.class).build());
    }
  }

  public interface SimpleCouchService extends CouchService<GuiceTest> {

  }

  public static class GuiceTest {
    String _id;
    String name;
  }

  @Test
  public void testMakeSureServiceWorks() {
    SimpleCouchService service = i.getInstance(SimpleCouchService.class);
    assertNotNull(service);

    GuiceTest instance = new GuiceTest();
    instance.name = "blah";
    CouchTx tx = service.persist(instance);
    assertNotNull(tx);

    GuiceTest retrieve = service.find(tx.id());
    assertNotNull(retrieve);
    assertEquals(tx.id(), retrieve._id);
    assertEquals("blah", retrieve.name);
  }
}
