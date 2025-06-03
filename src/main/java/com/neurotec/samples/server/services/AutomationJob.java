package com.neurotec.samples.server.services;

import com.neurotec.samples.server.MainFrame;
import com.neurotec.samples.server.util.MessageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.swing.*;
import java.awt.*;

public class AutomationJob implements Job {

    private static final Logger log = LogManager.getLogger(AutomationJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                Dimension d = new Dimension(935, 450);
                frame.setSize(d);
                frame.setMinimumSize(d);
                frame.setPreferredSize(d);
                frame.setResizable(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setTitle("Server Sample - Scheduled");
                frame.setLocationRelativeTo(null);
                frame.showMainFrame();
                System.out.println("Automated Enrollment -> Deduplication process initialized:" + new java.util.Date());
            } catch (Exception e) {
                e.printStackTrace();
                MessageUtils.showError(null, e);
            }
        });
    }
}
