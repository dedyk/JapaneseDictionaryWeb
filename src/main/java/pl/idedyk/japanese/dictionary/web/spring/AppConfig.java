package pl.idedyk.japanese.dictionary.web.spring;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        
    	ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    	
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
    
    @Bean(name="japaneseDictionaryCacheControl")
    public CacheControl createCacheControl() {
    	// CacheControl cacheControl = CacheControl.maxAge(Duration.ofSeconds(604801)); // tydzien i jedna sekunda
    	CacheControl cacheControl = CacheControl.maxAge(Duration.ofSeconds(3600)); // godzina i jedna sekunda
    	
    	cacheControl.mustRevalidate();
    	
    	return cacheControl;

//		to bylo kiedys w pliku japanese-dictionary-web-servlet.xml
//		<property name="cacheSeconds" value="604801" /> <!-- tydzien i jedna sekunda -->
//		<property name="useExpiresHeader" value="true" />
//		<property name="useCacheControlHeader" value="true" />
//		<property name="useCacheControlNoStore" value="false" />
//		<property name="alwaysMustRevalidate" value="true" />
//<!--  				<property name="cacheMappings">
//			<props>
//				< ! - - cache for one month - - >
//				<prop key="/js/**">2592000</prop>
//			</props>
//		</property>
//-->

    }
}
