package wo.app.woserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wo.app.woserver.annotation.RequestType;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import static jdk.nashorn.internal.objects.NativeArray.lastIndexOf;

public class UriUtils {
    private static final Logger logger= LoggerFactory.getLogger(UriUtils.class);
    public static String decode(String uri){
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e );
        }
        return uri;
    }
    public static String getContextPath(String uri){
        String returnUri=uri.contains("?")?uri.substring(0,uri.lastIndexOf("?")):uri;
        try {
            return URLDecoder.decode(returnUri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e );
            return returnUri;
        }
    }

    public static String getFileExtension(String uri){
        String contextPath=getContextPath(uri);
        String fileName=contextPath.substring(contextPath.lastIndexOf("/"));
        return fileName.substring(fileName.lastIndexOf("."));
    }
    public static RequestType parseRequestType(String uri){
        uri=decode(uri);
        if(uri.contains("?")){
            uri=uri.substring(0,uri.lastIndexOf("?") );
        }
        if(uri.substring(uri.lastIndexOf("/")).contains(".")){
            return RequestType.Static;
        }
        return RequestType.Dynamic;
    }
    public static Map<String,String> getParametersForQueryParam(String uri,Map<String,String>map){
        if(map==null){
            return map;
        }
        uri= UriUtils.decode(uri);
        if(!uri.contains("?")){
            return map;
        }
        String[] params = uri.split("\\?")[1].split("&");
        for(String param:params){
            if(param.contains("=")){
                String[] kv = param.split("=");
                if(kv.length==2){
                    map.put(param.split("=")[0], param.split("=")[1]);
                }
            }
        }
        return map;
    }

    public static Map<String,String> getQueryParameters(String uri,String uriWithVariable,Map<String,String>map){
        if(map==null){
            return map;
        }
        String[] paths = decode(uri).split("/");
        String[] variables = uriWithVariable.split("/");
        for(int i=0;i<variables.length;i++){
            if(variables[i].contains("{")&&variables[i].contains("}")){
                map.put(variables[i].replace("{","" ).replace("}","" ), paths[i]);
            }
        }
        return map;
    }

    public static boolean isQueryVariable(String uri){
        return  uri.contains("{") && uri.contains("}");
    }

    public static boolean isUriMatchedQueryMapping(String uriContextPath,String queryMapping){
        if(!isQueryVariable(queryMapping)){
            return uriContextPath.equals(queryMapping);
        }
        String[] paths = decode(uriContextPath).split("/");
        String[] variables = queryMapping.split("/");
        if(paths.length != variables.length){
            return uriContextPath.equals(queryMapping);
        }
        for(int i=0;i<variables.length;i++){
            if(variables[i].contains("{") && variables[i].contains("}")){
                variables[i]=paths[i];
            }
        }
        StringBuffer psb=new StringBuffer();
        for(String path:paths){
            psb.append(path);
        }
        StringBuffer vsb=new StringBuffer();
        for(String variable:variables){
            vsb.append(variable);
        }
        return psb.toString().equals(vsb.toString());
    }

    public static boolean isTextFile(String uri){
        String[]textExtension={".html",".js",".txt",".json",".doc",".docx",".css"};
        return Arrays.asList(textExtension).contains(getFileExtension(uri));
    }

    public static String getClassPath(){
        String packagePath=UriUtils.class.getPackage().getName().replace(".", "/");
        String packageAbsolutePath = Thread.currentThread().getContextClassLoader().getResource(packagePath).getPath();
        String classPath=packageAbsolutePath.substring(0,packageAbsolutePath.indexOf(packagePath));
        return classPath;
    }

    public static String getDefaultContextPath(String fileUrlPrefix,String webDir){
        String classPath = getClassPath();
        classPath = classPath.substring(0, classPath.substring(0, classPath.lastIndexOf("/")).lastIndexOf("/"));
        classPath = classPath.substring(0, classPath.substring(0, classPath.lastIndexOf("/")).lastIndexOf("/"));
        return classPath.replace(fileUrlPrefix+(System.getProperty("os.name").contains("Windows")?"/":""), "")+webDir;
    }

    public static String getURLProtocol(Class<?> clazz){
        String packagePath=clazz.getPackage().getName().replace(".", "/");
        URL packageURL = Thread.currentThread().getContextClassLoader().getResource(packagePath);
        return packageURL.getProtocol();
    }

    public static Set<String> getScanPackagePaths(String filePath){
        Set<String>packagePaths=new HashSet<>();
        File file = new File(filePath);
        if(file.isFile()){
            if(file.getName().contains(".class")){
                packagePaths.add(file.getParent());
                return packagePaths;
            }else{
                return packagePaths;
            }
        }else{
            scanDir(file,packagePaths);
            return packagePaths;
        }
    }

    private static void scanDir(File file,Set<String>packagePaths){
        File[] files = file.listFiles();
        if(files==null || files.length==0){
            return;
        }
        for(File fe:files){
            if(fe.isFile()){
                if(fe.getPath().contains(".class")){
                    boolean isAdd=true;
                    List<String> removePaths=new ArrayList<>();
                    for(String path:packagePaths){
                        if(file.getPath().contains(path)){
                            isAdd=false;
                            break;
                        }
                        if(path.contains(file.getPath())){
                            removePaths.add(path);
                        }
                    }
                    if(isAdd){
                        packagePaths.add(file.getPath());
                    }
                    for(String removePath:removePaths){
                        packagePaths.remove(removePath);
                    }
                    return;
                }
            }else{
                scanDir(fe,packagePaths);
            }
        }
    }
}
