package wo.app.woserver.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wo.app.woserver.WoServer;

@Configuration
@EnableConfigurationProperties(WoServerProperties.class)
public class WoServerAutoConfiguration {
    @Bean
    public WoServer woServer(WoServerProperties woServerProperties, ApplicationContext applicationContext){
        return new WoServer(woServerProperties, applicationContext);
    }
}
