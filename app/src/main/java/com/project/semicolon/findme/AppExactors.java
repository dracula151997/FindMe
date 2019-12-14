package com.project.semicolon.findme;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExactors {
    private static final Object LOCK = new Object();
    private static AppExactors instance;
    private final Executor diskIo;
    private final Executor mainThread;

    public AppExactors(Executor diskIo, Executor mainThread) {
        this.diskIo = diskIo;
        this.mainThread = mainThread;
    }

    public static AppExactors getInstance() {
        if (instance == null) {
            instance = new AppExactors(Executors.newSingleThreadExecutor(),
                    new MainThreadExecutors());
        }
        return instance;
    }

    public Executor getDiskIo() {
        return diskIo;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    private static class MainThreadExecutors implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            mainThreadHandler.post(runnable);

        }
    }
}
