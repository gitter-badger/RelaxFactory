package rxf.web.inf;

import one.xio.HttpHeaders;
import rxf.core.DateHeaderParser;

import java.nio.channels.SelectionKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ContentRootCacheImpl extends ContentRootImpl {

  public static final long YEAR = TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS);
  public static final Pattern CACHE_PATTERN = Pattern
      .compile(".*(clear.cache.gif|[0-9A-F]{32}[.]cache[.]html)$");

  public void onWrite(SelectionKey key) throws Exception {
    getReq().headerStrings().put(HttpHeaders.Expires.getHeader(),
        DateHeaderParser.RFC1123.getFormat().format(new Date(new Date().getTime() + YEAR)));
    super.onWrite(key);
  }
}
