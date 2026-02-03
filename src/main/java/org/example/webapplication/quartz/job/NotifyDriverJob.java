package org.example.webapplication.quartz.job;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.travel.TravelRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifyDriverJob implements Job {
    @Autowired
    private TravelRepository travelRepository;

    public NotifyDriverJob() {}

    @Override
    public void execute(JobExecutionContext context) {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String driverName = dataMap.getString("driverName");
        String scheduleName = dataMap.getString("scheduleName");
        String startDate = dataMap.getString("startDate");

        System.out.println(" Notify DRIVER: "
                + driverName
                + " | Schedule: "
                + scheduleName
                + " | Date: "
                + startDate
        );
    }
}
