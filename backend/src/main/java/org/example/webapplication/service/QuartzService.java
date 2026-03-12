package org.example.webapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.quartz.job.NotifyDriverJob;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class QuartzService {
    private final Scheduler scheduler;
    public void scheduleNotifyDriver (Travel travel, Date fireTime) {

        try {
            JobDataMap map = new JobDataMap();
            map.put("driverName", travel.getUser().getUsername());
            map.put("scheduleName",
                    travel.getSchedule().getStartPlace()
                            + " - "
                            + travel.getSchedule().getEndPlace()
            );
            map.put("startDate", travel.getStartDate().toString());
            JobKey jobKey = JobKey.jobKey(
                    "NOTIFY_TRAVEL_" + travel.getId(),
                    "TRAVEL_NOTIFY"
            );

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            JobDetail jobDetail = JobBuilder.newJob(NotifyDriverJob.class)
                    .withIdentity(jobKey)
                    .usingJobData(map)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("TRIGGER_TRAVEL_" + travel.getId(), "TRAVEL_NOTIFY")
                    .startAt(fireTime)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
