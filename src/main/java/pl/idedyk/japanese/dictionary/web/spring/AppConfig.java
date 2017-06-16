package pl.idedyk.japanese.dictionary.web.spring;

import org.springframework.context.annotation.Configuration;
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

}
