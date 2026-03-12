package org.example.webapplication.quartz.scheduler;

import lombok.AllArgsConstructor;
import org.example.webapplication.quartz.AutowiringSpringBeanJobFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@AllArgsConstructor
public class SchedulerConfig {
    private final ApplicationContext applicationContext;

    @Bean
    public Scheduler scheduler() throws SchedulerException {

        SchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        AutowiringSpringBeanJobFactory jobFactory =
                new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        scheduler.setJobFactory(jobFactory);
        scheduler.start();

        return scheduler;
    }



}
