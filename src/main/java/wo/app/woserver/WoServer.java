package wo.app.woserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import wo.app.woserver.configuration.WoServerProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WoServer implements ApplicationRunner{
    private final Logger logger= LoggerFactory.getLogger(WoServer.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final WoServerProperties woServerProperties;
    private final ApplicationContext applicationContext;

    public static final String OPTION_PORT="port";
    public static final String CONTEXT_PATH ="context.path";
    public static final String PATH_DELIMITER=System.getProperty("os.name").contains("Windows")?"\\":"/";
    public static final String DEFAULT_CONTEXT_DIR="/woWebApp/classes/";

    public WoServer(WoServerProperties woServerProperties, ApplicationContext applicationContext){
        this.woServerProperties = woServerProperties;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            Integer port=woServerProperties.getPort()==null?8080:woServerProperties.getPort();
            if(args.containsOption(OPTION_PORT)){
                port=Integer.valueOf(args.getOptionValues(OPTION_PORT).get(0));
            }
            new ServerBootstrap().group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65535));
                            // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML5文件
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new WoServerChannelHandler());
                        }
                    })
                    .bind(port)
                    .sync()
                    .channel()
                    .closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private class WoServerChannelHandler extends ChannelDuplexHandler{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof FullHttpRequest){
                FullHttpRequest request=(FullHttpRequest)msg;
                executor.submit(new WoDispatcher(ctx, request, applicationContext));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error(cause.getMessage(),cause );
            ctx.close();
        }
    }
}
