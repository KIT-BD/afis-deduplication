package com.neurotec.samples.server.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NServerManager {

    private static final String NSERVER_PATH = "C:\\Servers\\AFISServer_Asif\\NServer\\NServer.exe";
    private static final String NSERVER_CONFIG = "NServer.conf";
    private static final String NSERVER_DIRECTORY = "C:\\Servers\\AFISServer_Asif\\NServer";
    private static final Logger logger = LoggerFactory.getLogger(NServerManager.class);
    private static final String DB_FILE = "NServer.db";
    private static final String LOG_FILE = "NServer.log";

    private DefaultExecutor executor;
    private ExecuteWatchdog watchdog;

    private void backupAndDeleteFile(String fileName) {
        Path sourcePath = Paths.get(NSERVER_DIRECTORY, fileName);
        if (Files.exists(sourcePath)) {
            try {
                String timestamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());
                String extension = fileName.substring(fileName.lastIndexOf('.'));
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String backupFileName = String.format("%s_%s%s", baseName, timestamp, extension);
                Path destPath = Paths.get(NSERVER_DIRECTORY, backupFileName);

                logger.info("Backing up {} to {}", sourcePath, destPath);
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Backup of {} completed successfully.", fileName);

                logger.info("Deleting original file: {}", sourcePath);
                Files.delete(sourcePath);
                logger.info("Successfully deleted {}", sourcePath);

                cleanupOldBackups(fileName);
            } catch (IOException e) {
                logger.error("Error during backup and delete for file: " + fileName, e);
            }
        } else {
            logger.warn("File {} not found for backup and deletion.", sourcePath);
        }
    }

    private void cleanupOldBackups(String originalFileName) {
        try {
            Path dir = Paths.get(NSERVER_DIRECTORY);
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));

            List<Path> backups = Files.list(dir)
                    .filter(p -> {
                        String fName = p.getFileName().toString();
                        return fName.startsWith(baseName + "_") && fName.endsWith(extension);
                    })
                    .sorted(Comparator.comparing(this::getFileCreationTime).reversed()) // Newest first
                    .collect(Collectors.toList());

            int maxBackups = 3;
            if (backups.size() > maxBackups) {
                List<Path> toDelete = backups.subList(maxBackups, backups.size());
                for (Path oldBackup : toDelete) {
                    Files.delete(oldBackup);
                    logger.info("Deleted old backup: {}", oldBackup);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to clean up old backups for " + originalFileName, e);
        }
    }

    private FileTime getFileCreationTime(Path path) {
        try {
            return (FileTime) Files.getAttribute(path, "creationTime");
        } catch (IOException e) {
            logger.error("Could not get creation time for " + path, e);
            return FileTime.fromMillis(0); // fallback
        }
    }

    public void startNServer() throws IOException {
        backupAndDeleteFile(DB_FILE);
        backupAndDeleteFile(LOG_FILE);

        executor = new DefaultExecutor();
        watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        executor.setWorkingDirectory(new File(NSERVER_DIRECTORY));

        CommandLine cmdLine = new CommandLine(NSERVER_PATH);
        cmdLine.addArgument("-c");
        cmdLine.addArgument(NSERVER_CONFIG);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
        try {
            logger.info("Starting NServer...");
            executor.execute(cmdLine, resultHandler);
            logger.info("NServer started successfully.");
        } catch (IOException e) {
            logger.error("Failed to start NServer", e);
            throw e;
        }
    }

    public void stopNServer() {
        try {
            logger.info("Terminating 'NServer.exe' using: taskkill /F /IM NServer.exe");
            Runtime.getRuntime().exec("taskkill /F /IM NServer.exe");
            logger.info("Taskkill command for 'NServer.exe' issued successfully.");
        } catch (IOException e) {
            logger.error("Failed to issue taskkill command for 'NServer.exe'.", e);
        }
    }
} 