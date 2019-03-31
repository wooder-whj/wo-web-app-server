package wo.app.woserver.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import wo.app.woserver.annotation.*;
import wo.app.woserver.annotation.HttpMethod;
import wo.app.woserver.bean.ReflectiveBean;
import wo.app.woserver.util.UriUtils;

import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WoDynamicRequestHandler implements RequestHandler{
    private final Logger logger= LoggerFactory.getLogger(WoDynamicRequestHandler.class);
    private final FullHttpRequest request;
    private final ApplicationContext applicationContext;

    public WoDynamicRequestHandler(FullHttpRequest request, ApplicationContext applicationContext) {
        this.request = request;
        this.applicationContext = applicationContext;
    }

    @Override
    public HttpResponse handleRequest() throws Exception {
        String response=null;
        ReflectiveBean routine = routine();
        if(routine.getMethod().getReturnType().isAssignableFrom(List.class)){
            response=JSONArray.toJSONString(routine.getMethod().invoke(routine.getAnInstance(), routine.getArgs()));
        }else{
            response=JSON.toJSONString(routine.getMethod().invoke(routine.getAnInstance(), routine.getArgs()));
        }
        byte[]bytes=response.getBytes(Charset.forName("UTF-8"));
        HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes));
        String contentType = request.headers().get(HttpHeaderNames.ACCEPT);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE,contentType==null
                                    ? "*/*;charset=UTF-8"
                                    : contentType.replace(",",";" )+";charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH,bytes.length);
        return httpResponse;
    }

    private ReflectiveBean routine() throws Exception {
        ReflectiveBean bean=new ReflectiveBean();
        for(String name:applicationContext.getBeanNamesForAnnotation(Request.class)){
            Class<?> clazz = Class.forName(applicationContext.getBean(name).getClass().getTypeName(),
                    true,
                    applicationContext.getClassLoader());
            for(Method method:clazz.getDeclaredMethods()){
                QueryMapping queryMapping = method.getDeclaredAnnotation(QueryMapping.class);
                if(queryMapping!=null){
                    if(UriUtils.isUriMatchedQueryMapping(UriUtils.getContextPath(request.uri()),queryMapping.value())
                            && request.method().name().equals(queryMapping.method())){
                        bean.setAnInstance(applicationContext.getBean(name));
                        bean.setMethod(method);
                        if(HttpMethod.GET.equals(queryMapping.method())){
                            Object[] args=new Object[method.getParameters().length];
                            Map<String,String> paramMap=new HashMap<>();
                            getArgsForParam(method,args,paramMap);
                            getArgsForVariable(method,args,paramMap);
                            bean.setArgs(args);
                        }else{// POST
                            String strContentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE).trim();
                            if(strContentType.contains("x-www-form-urlencoded")){
                                bean.setArgs(getFormParams(method));
                            }else if(strContentType.contains("application/json")){
                                bean.setArgs(postArgsByJson(method));
                            }
                        }
                    }
                }
            }
        }
        return bean;
    }

    private void getArgsForVariable(Method method, Object[] args,Map<String,String>paramMap) {
        Parameter[]parameters=method.getParameters();
        UriUtils.getQueryParameters(request.uri(), method.getDeclaredAnnotation(QueryMapping.class).value(), paramMap);
        for(int i=0;i<parameters.length;i++){
            QueryVariable queryVariable = parameters[i].getDeclaredAnnotation(QueryVariable.class);
            if(queryVariable!=null){
                args[i]=paramMap.get(queryVariable.value());
            }
        }
    }

    private void getArgsForParam(Method method,Object[]args,Map<String,String>paramMap){
        Parameter[]parameters=method.getParameters();
        UriUtils.getParametersForQueryParam(request.uri(),paramMap);
        paramMap.entrySet().forEach(entry->{
            String key=entry.getKey().trim();
            String value=entry.getValue().trim();
            for(int i=0;i<parameters.length;i++){
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                if(queryParam!=null&&(queryParam.value().equals(key))){
                    args[i]=value;
                }
            }
        });
    }

    private Object[] postArgsByJson(Method method){
        Parameter[] parameters = method.getParameters();
        Object[] args=new Object[parameters.length];
        String content = new String(ByteBufUtil.getBytes(request.content()), StandardCharsets.UTF_8);
        for(int i=0;i<parameters.length;i++){
            QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
            if(queryParam!=null){
                if(parameters[i].getType().isAssignableFrom(List.class)){
                    args[i] = JSONArray.parseArray(content);
                }else {
                    JSONObject json = JSONObject.parseObject(content);
                    args[i]=json.getObject(parameters[i].getName(), Object.class);
                }
            }
        }
        return args;
    }

    /*
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     */
    private Object[] getFormParams(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args=new Object[parameters.length];
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
        try{
            List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
            List<MemoryAttribute> attributes=new ArrayList<>();
            for (InterfaceHttpData data : postData) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    MemoryAttribute attribute = (MemoryAttribute) data;
                    attributes.add(attribute);
                }
            }
            for(int i=0;i<parameters.length;i++){
                QueryParam annotation = parameters[i].getDeclaredAnnotation(QueryParam.class);
                if(annotation!=null){
                    for (MemoryAttribute attribute : attributes) {
                        if(annotation.value().equals(attribute.getName())){
                            if(parameters[i].getType().isAssignableFrom(List.class)){
                                ArrayList list = JSONArray.parseObject(attribute.getValue(), ArrayList.class);
                                args[i]=list;
                                break;
                            }
                            Constructor<?> constructor = parameters[i].getType().getDeclaredConstructor(String.class);
                            args[i]=constructor.newInstance(attribute.getValue());
                            break;
                        }
                    }
                }else{
                    Field[] fields = parameters[i].getType().getDeclaredFields();
                    Object newInstance = parameters[i].getType().newInstance();
                    for(Field field:fields){
                        String fieldName=field.getName();
                        for (MemoryAttribute attribute : attributes) {
                            if(fieldName.equals(attribute.getName())){
                                field.setAccessible(true);
                                if(field.getType().isAssignableFrom(List.class)){
                                    ArrayList list = JSONArray.parseObject(attribute.getValue(), ArrayList.class);
                                    field.set(newInstance, list);
                                    break;
                                }
                                Constructor<?> constructor = field.getType().getDeclaredConstructor(String.class);
                                Object fieldInstance = constructor.newInstance(attribute.getValue());
                                field.set(newInstance,fieldInstance);
                                break;
                            }
                        }
                    }
                    args[i]=newInstance;
                }
            }

        } catch (Exception e) {
          logger.error(e.getMessage(),e );
        } finally {
            if(decoder!=null){
                decoder.destroy();
            }
        }
        return args;
    }
}
