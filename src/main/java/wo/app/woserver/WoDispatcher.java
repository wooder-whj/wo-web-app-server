package wo.app.woserver;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import wo.app.woserver.annotation.RequestType;
import wo.app.woserver.handler.WoDynamicRequestHandler;
import wo.app.woserver.handler.WoErrorRequestHandler;
import wo.app.woserver.handler.WoStaticRequestHandler;
import wo.app.woserver.util.UriUtils;

import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class WoDispatcher implements Callable {
    private final Logger logger= LoggerFactory.getLogger(WoDispatcher.class);
    private final ChannelHandlerContext context;
    private final FullHttpRequest request;
    private final ApplicationContext applicationContext;

    public WoDispatcher(ChannelHandlerContext context, FullHttpRequest request, ApplicationContext applicationContext){
        this.context = context;
        this.request = request;
        this.applicationContext = applicationContext;
    }
    @Override
    public Future call() throws Exception {
        ChannelFuture future=null;
        try{
            if (RequestType.Dynamic.equals(UriUtils.parseRequestType(request.uri()))) {
                WoDynamicRequestHandler handler = new WoDynamicRequestHandler(request, applicationContext);
                future = context.writeAndFlush(handler.handleRequest());
            } else {
                WoStaticRequestHandler requestHandler = new WoStaticRequestHandler(request, applicationContext);
                future = context.writeAndFlush(requestHandler.handleRequest());
            }
        }catch (Exception e){
            if(context.channel().closeFuture().cancel(true)){
                context.writeAndFlush(new WoErrorRequestHandler(e, request).handleRequest());
                context.channel().close();
                future = context.close();
            }
            logger.error(e.getMessage(),e );
            throw e;
        }finally {
            if(request!=null){
                request.release();
            }
            if(future!=null){
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
        return future;
    }
}
