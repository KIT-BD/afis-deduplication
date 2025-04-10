package com.neurotec.samples.server.connection;

import com.neurotec.biometrics.NSubject;

public interface TemplateLoader {
  void beginLoad() throws Exception;
  
  void endLoad() throws Exception;
  
  NSubject[] loadNext(int paramInt) throws Exception;
  
  int getTemplateCount() throws Exception;
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\connection\TemplateLoader.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */