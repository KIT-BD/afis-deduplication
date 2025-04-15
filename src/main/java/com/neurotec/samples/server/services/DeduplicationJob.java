package com.neurotec.samples.server.services;

import com.neurotec.samples.server.MainFrame;
import com.neurotec.samples.server.controls.BasePanel;
import com.neurotec.samples.server.util.MessageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.swing.*;
import java.awt.*;

public class DeduplicationJob implements Job {

    private static final Logger log = LogManager.getLogger(DeduplicationJob.class);

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
                System.out.println("Deduplication started at:" + new java.util.Date());
            } catch (Exception e) {
                e.printStackTrace();
                MessageUtils.showError(null, e);
            }
        });
    }
}
