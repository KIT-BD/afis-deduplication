package com.neurotec.samples.server.connection;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NBuffer;
import com.neurotec.io.NFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;


public final class DirectoryEnumerator
        implements TemplateLoader {
    private File[] files = null;
    private final File directory;
    private int index = -1;


    private int resultCount;


    public DirectoryEnumerator(String directoryPath) {
        String templateDir = directoryPath;
        if (templateDir == null || templateDir.equals("")) {
            throw new IllegalArgumentException("Specified directory doesn't exist");
        }
        this.directory = new File(templateDir);
        if (!this.directory.exists() || !this.directory.isDirectory()) {
            throw new IllegalArgumentException("Specified directory doesn't exist");
        }
    }


    public synchronized void beginLoad() {
        if (this.index != -1) {
            throw new IllegalStateException();
        }
        listFiles();
        
        // Validate that the starting index is within bounds
        int startIndex = 300;
        if (startIndex >= this.resultCount) {
            System.err.println("Starting index " + startIndex + " is greater than or equal to the total template count of " + this.resultCount);
            this.index = 0; // Fall back to starting from 0
        } else {
            this.index = startIndex;
            System.out.println("Starting template processing from index " + startIndex + " of " + this.resultCount);
        }
    }


    public synchronized void endLoad() {
        this.index = -1;
    }


    public synchronized NSubject[] loadNext(int n) throws IOException {
        if (this.index == -1) {
            throw new IllegalStateException();
        }
        if (this.resultCount == 0 || this.resultCount <= this.index) {
            return new NSubject[0];
        }

        int count = this.resultCount - this.index;
        count = (count > n) ? n : count;
        NSubject[] results = new NSubject[count];
        
        // Log batch start
        System.out.println("\nStarting to load batch of " + count + " templates (from " + (this.index + 1) + " to " + (this.index + count) + " of " + this.resultCount + ")");
        
        for (int i = 0; i < count; i++) {
            File file = this.files[this.index++];
            String id = file.getName();
            NBuffer template = NFile.readAllBytes(file.getAbsolutePath());
            NSubject result = new NSubject();
            result.setTemplateBuffer(template);
            result.setId(id);
            results[i] = result;
            
            // Log progress for each template
            System.out.println("Loaded template: " + this.index + " of " + this.resultCount + 
                              " (" + String.format("%.2f", (this.index * 100.0 / this.resultCount)) + "%)");
        }
        
        // Log batch completion
        System.out.println("Completed loading batch of " + count + " templates. Total loaded: " + this.index + " of " + this.resultCount);
        
        return results;
    }


    public void dispose() {
    }


    public int getTemplateCount() {
        try {
            listFiles();
            if (this.files == null) {
                System.err.println("No files found in directory: " + this.directory.getAbsolutePath());
                return 0;
            }
            System.out.println("Found " + this.resultCount + " files in directory: " + this.directory.getAbsolutePath());
            return this.resultCount;
        } catch (Exception e) {
            System.err.println("Error listing files in directory: " + this.directory.getAbsolutePath());
            e.printStackTrace();
            return 0;
        }
    }


    public String toString() {
        return this.directory.getPath();
    }


    public synchronized void listFiles() {
        if (this.files == null) {
            if (!this.directory.exists()) {
                System.err.println("Directory does not exist: " + this.directory.getAbsolutePath());
                this.files = new File[0];
                this.resultCount = 0;
                return;
            }

            if (!this.directory.isDirectory()) {
                System.err.println("Path is not a directory: " + this.directory.getAbsolutePath());
                this.files = new File[0];
                this.resultCount = 0;
                return;
            }

            System.out.println("Listing files in directory: " + this.directory.getAbsolutePath());
            this.files = this.directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });

            if (this.files == null) {
                System.err.println("Failed to list files, listFiles() returned null. Check permissions for: " + this.directory.getAbsolutePath());
                this.files = new File[0];
                this.resultCount = 0;
            } else {
                this.resultCount = this.files.length;
                System.out.println("Found " + this.resultCount + " files");
            }
        }
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\connection\DirectoryEnumerator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */