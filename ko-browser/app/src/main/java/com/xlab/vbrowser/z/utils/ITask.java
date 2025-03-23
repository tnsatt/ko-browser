package com.xlab.vbrowser.z.utils;

public interface ITask {
    void run();
    void onComplete();
    void onError(Exception e);
}
