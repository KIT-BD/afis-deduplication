package com.neurotec.samples.server.connection;

import com.neurotec.biometrics.NSubject;

public interface TemplateLoader {
    void beginLoad() throws Exception;

    void endLoad() throws Exception;

    NSubject[] loadNext(int paramInt) throws Exception;

    int getTemplateCount() throws Exception;
}