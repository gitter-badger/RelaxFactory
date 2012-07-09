package rxf.server.guice;

import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.*;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.shared.*;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;
import junit.framework.Assert;
import org.junit.Test;
import rxf.server.GwtRequestFactoryVisitor;

/**
 * This test may have difficulty running if the RF annotation processor isn't triggered before it.
 *
 * @author colin
 */
public class RFServiceLayerModuleTest {
  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {

      install(new RFServiceLayerModule());
      bind(ServiceLayerDecorator.class).to(InjectingServiceLayerDecorator.class);
    }
  }

  public static class SampleService {
    public String trim(String str) {
      return str.trim();
    }
  }

  @Service(value = SampleService.class, locator = InjectingServiceLocator.class)
  public interface SampleRequest extends RequestContext {
    Request<String> trim(String str);
  }

  public interface SampleFactory extends RequestFactory {
    SampleRequest req();
  }

  @Test
  public void testBasicSetup() {
    //build injector, but don't make use of it directly, only via the request processor
    Injector i = Guice.createInjector(new TestModule());
    SampleFactory f = RequestFactorySource.create(SampleFactory.class);
//    final String[] holder = new String[1];
    final AtomicReference<String> holder = new AtomicReference<String>();
    f.initialize(new SimpleEventBus(), new RequestTransport() {
      public void send(String arg0, TransportReceiver arg1) {
        // the SIMPLE_REQUEST_PROCESSOR field should have been repopulated by guice
        // and any dependencies satisfied
        String resp = GwtRequestFactoryVisitor.SIMPLE_REQUEST_PROCESSOR.process(arg0);
        arg1.onTransportSuccess(resp);
      }
    });
    f.req().trim("   word   ").fire(new Receiver<String>() {
      @Override
      public void onSuccess(String arg0) {
        holder.set(arg0);
      }
    });
    Assert.assertEquals("word", holder.get());
  }
}
