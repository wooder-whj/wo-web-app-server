package wo.app.woserver.handler;

import io.netty.handler.codec.http.HttpResponse;

public interface RequestHandler {
    HttpResponse handleRequest() throws Exception;
}
