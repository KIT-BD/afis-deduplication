package com.neurotec.samples.server.connection;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NBuffer;
import com.neurotec.io.NFile;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;





public final class DirectoryEnumerator
  implements TemplateLoader
{
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
    this.index = 0;
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
    for (int i = 0; i < count; i++) {
      File file = this.files[this.index++];
      String id = file.getName();
      NBuffer template = NFile.readAllBytes(file.getAbsolutePath());
      NSubject result = new NSubject();
      result.setTemplateBuffer(template);
      result.setId(id);
      results[i] = result;
    }
    return results;
  }


  public void dispose() {}


  public int getTemplateCount() {
    listFiles();
    return this.resultCount;
  }


  public String toString() {
    return this.directory.getPath();
  }





  public synchronized void listFiles() {
    if (this.files == null) {
      this.files = this.directory.listFiles(new FileFilter()
          {
            public boolean accept(File pathname) {
              return pathname.isFile();
            }
          });
      this.resultCount = this.files.length;
    }
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\connection\DirectoryEnumerator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */