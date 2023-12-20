package com.salesrep.app.htmlLoader;

import android.graphics.Bitmap;

public interface BitmapCallback {
    void finished(Bitmap bitmap);

    void error(Throwable error);
}
