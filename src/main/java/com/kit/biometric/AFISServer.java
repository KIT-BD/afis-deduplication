package com.kit.biometric;

import com.neurotec.licensing.NLicense;
import com.neurotec.samples.server.services.AutomationJob;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.server.util.PropertyLoader;
import com.neurotec.samples.util.LibraryManager;
import com.neurotec.samples.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingUtilities;


public final class AFISServer {
    static Collection<String> licenses = null;
    private static final Logger log = LogManager.getLogger(AFISServer.class);

    public static void main(String[] args) throws SchedulerException, InterruptedException {
//        Utils.setupLookAndFeel();
//        System.out.println(System.getProperty("user.home"));

        PropertyLoader.fileName = args[0];

        LibraryManager.initLibraryPath();
        licenses = new ArrayList<>();

        licenses.add("Biometrics.FingerClient");

        try {
            if (!NLicense.obtain("/local", 5000, "FingerExtractor")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerExtractor"});
            }


            if (!NLicense.obtain("/local", 5000, "FingerClient")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerClient"});
            }


            if (!NLicense.obtain("/local", 5000, "FingerMatcher")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerMatcher"});
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(-1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    scheduledJob();
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageUtils.showError(null, e);
                }
            }
        });
    }

    private static void scheduledJob() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(AutomationJob.class)
                .withIdentity("guiJob", "group1")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("cronTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 0 * * ?"))
                .build();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        scheduler.scheduleJob(job, trigger);

        scheduler.triggerJob(job.getKey());

        System.out.println("\nScheduled Automation task to run daily at midnight and once on startup.");
    }

}
