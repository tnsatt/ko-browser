package com.xlab.vbrowser.z.utils;

import android.os.AsyncTask;

import com.xlab.vbrowser.utils.IBackgroundTask;

public class ATask extends AsyncTask<Void, Void, Void> {
    private final ITask task;
    private Exception error;

    public ATask(ITask task) {
        this.task = task;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        try{
            this.task.run();
        }catch (Exception e){
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(error==null) {
            this.task.onComplete();
        }else{
            this.task.onError(error);
        }
    }
}
