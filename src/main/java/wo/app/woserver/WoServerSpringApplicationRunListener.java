package wo.app.woserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ResourceUtils;
import wo.app.woserver.util.UriUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

public class WoServerSpringApplicationRunListener implements SpringApplicationRunListener {
    private final Logger logger= LoggerFactory.getLogger(WoServerSpringApplicationRunListener.class);
    private final String[] args;

    public WoServerSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.args = args;
    }
    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        ApplicationArguments arguments = context.getBean(ApplicationArguments.class);
        if(!arguments.containsOption(WoServer.CONTEXT_PATH)){
            return;
        }
        AnnotationConfigApplicationContext aContext=(AnnotationConfigApplicationContext) context;
        List<String> optionValues = arguments.getOptionValues(WoServer.CONTEXT_PATH);
        try {
            URL[]urls=new URL[optionValues.size()];
            for(int i=0;i<urls.length;i++){
                urls[i]=new URL(ResourceUtils.FILE_URL_PREFIX+"/" +optionValues.get(i).replace("\\","/" ));
            }
            URLClassLoader urlClassLoader = new URLClassLoader(urls, aContext.getClassLoader());
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            aContext.setClassLoader(urlClassLoader);
            ClassPathBeanDefinitionScanner definitionScanner = new ClassPathBeanDefinitionScanner(aContext);
            for(String value:optionValues){
                Set<String> packagePaths = UriUtils.getScanPackagePaths(value);
                for(String packagePath:packagePaths){
                    definitionScanner.scan(packagePath.replace(value, "")
                            .replace("/", ".")
                            .replace("\\", "."));
                }
            }
            aContext.getBeanFactory().setBeanClassLoader(urlClassLoader);
        } catch (Exception e) {
            logger.error(e.getMessage(),e );
        }

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
}
