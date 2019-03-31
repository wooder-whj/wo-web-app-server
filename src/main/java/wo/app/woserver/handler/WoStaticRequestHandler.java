package wo.app.woserver.handler;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import wo.app.woserver.WoServer;
import wo.app.woserver.util.UriUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class WoStaticRequestHandler implements RequestHandler {
    private final Logger logger= LoggerFactory.getLogger(WoStaticRequestHandler.class);
    private final FullHttpRequest request;
    private final ApplicationContext applicationContext;

    public WoStaticRequestHandler(FullHttpRequest request, ApplicationContext applicationContext) {
        this.request = request;
        this.applicationContext = applicationContext;
    }

    @Override
    public HttpResponse handleRequest() throws Exception {
        byte[] bytes = readStaticResource();
        HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE,"*/*;charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        return httpResponse;
    }

    private byte[] readStaticResource() throws IOException {
        RandomAccessFile accessFile=null;
        FileChannel channel=null;
        byte[] bytes=null;
        List<Byte> contentBytes=new ArrayList();
        try{
            ApplicationArguments arguments = applicationContext.getBean(ApplicationArguments.class);
            String contextPath=UriUtils.getContextPath(request.uri());
            if(arguments.containsOption(WoServer.CONTEXT_PATH)){
                accessFile=new RandomAccessFile( arguments.getOptionValues(WoServer.CONTEXT_PATH).get(0) + contextPath, "r");
            }else {
                accessFile=new RandomAccessFile( UriUtils.getClassPath() + contextPath.substring(1), "r");
            }
//            String urlPath=ResourceUtils.URL_PROTOCOL_JAR.equals(UriUtils.getURLProtocol(WoStaticRequestHandler.class))
//                            ?UriUtils.getClassPath().replace(ResourceUtils.FILE_URL_PREFIX,ResourceUtils.JAR_URL_PREFIX )
//                            :UriUtils.getClassPath();
            channel = accessFile.getChannel();
            ByteBuffer buffer=ByteBuffer.allocate(2048);
            while((channel.read(buffer))>0){
                buffer.flip();
                bytes=new byte[buffer.limit()];
                buffer.get(bytes);
                for(byte b:bytes){
                    contentBytes.add(b);
                }
                buffer.clear();
            }
            bytes=new byte[contentBytes.size()];
            for(int i=0;i<contentBytes.size();i++){
                bytes[i]=(contentBytes.get(i)).byteValue();
            }
        }finally {
            try{
                if(channel!=null){
                    channel.close();
                }
                if(accessFile!=null){
                    accessFile.close();
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e );
            }
        }
        return bytes;
    }
}
