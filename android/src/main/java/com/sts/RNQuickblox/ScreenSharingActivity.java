package com.sts.RNQuickblox;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.BaseActivityEventListener;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;

/**
 * Created by monkey on 12/27/17.
 */

public class ScreenSharingActivity extends BaseActivityEventListener {

    private QuickbloxHandler quickbloxHandler;

    public ScreenSharingActivity() {
        this.quickbloxHandler = QuickbloxHandler.getInstance();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data);
            }
        }
    }

    private void startScreenSharing(Intent data){//pass data from permission request
        quickbloxHandler.getSession().getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
    }
}
