package com.salesrep.app.util;

import android.os.SystemClock;
import android.view.View;

public abstract class ButtonMultipleClickListener implements View.OnClickListener {

    protected int defaultInterval;
    private long lastTimeClicked = 0;

    public ButtonMultipleClickListener() {
        this(1000);
    }

    public ButtonMultipleClickListener(int minInterval) {
        this.defaultInterval = minInterval;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            performClick(v);
            return;
        }
        lastTimeClicked = SystemClock.elapsedRealtime();
        performClickAfterInterval(v);
    }

    public abstract void performClick(View v);

    public abstract void performClickAfterInterval(View v);

}
