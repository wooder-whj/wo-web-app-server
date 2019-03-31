package wo.app.woserver.handler;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wo.app.woserver.annotation.RequestType;
import wo.app.woserver.util.UriUtils;

public class WoErrorRequestHandler implements RequestHandler {
    private final Logger logger= LoggerFactory.getLogger(WoErrorRequestHandler.class);
    private final FullHttpRequest request;
    private final Exception exception;

    public WoErrorRequestHandler(Exception exception, FullHttpRequest request) {
        this.request = request;
        this.exception = exception;
    }

    @Override
    public HttpResponse handleRequest(){
        HttpResponse httpResponse =null;
        if (RequestType.Dynamic.equals(UriUtils.parseRequestType(request.uri()))) {
            httpResponse =new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR
                    , Unpooled.copiedBuffer("Internal error 500".getBytes()));
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8")
                    .set(HttpHeaderNames.CONTENT_LENGTH,"Internal error 500".getBytes().length);
        }else{
            httpResponse =new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND
                    , Unpooled.copiedBuffer("404 File Not Found!".getBytes()));
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8")
                    .set(HttpHeaderNames.CONTENT_LENGTH,"404 File Not Found!".getBytes().length);
        }
        return httpResponse;
    }
}
