package com.sts.RNQuickblox;

import android.telecom.Call;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
//import com.facebook.react.bridge.ReactMethod;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
//import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
//import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dat Tran on 3/22/17.
 */

public class QuickbloxHandler implements QBRTCClientVideoTracksCallbacks<QBRTCSession>, QBRTCSessionConnectionCallbacks {

    private static final String TAG = QuickbloxHandler.class.getSimpleName();

    private static QuickbloxHandler instance;
    private ReactApplicationContext reactApplicationContext;

    public Integer getCaller() {
        return caller;
    }

    private Integer caller;

    public QuickbloxLocalVideoViewManager getLocalViewManager() {
        return localViewManager;
    }

    public void setLocalViewManager(QuickbloxLocalVideoViewManager localViewManager) {
        this.localViewManager = localViewManager;
    }

    private QuickbloxLocalVideoViewManager localViewManager;

    public QuickbloxRemoteVideoViewManager getRemoteVideoViewManager() {
        return remoteVideoViewManager;
    }

    public void setRemoteVideoViewManager(QuickbloxRemoteVideoViewManager remoteVideoViewManager) {
        this.remoteVideoViewManager = remoteVideoViewManager;
    }

    private QuickbloxRemoteVideoViewManager remoteVideoViewManager;


    public RNQuickbloxModule getQuickbloxClient() {
        return quickbloxClient;
    }

    public void setQuickbloxClient(RNQuickbloxModule quickbloxClient, ReactApplicationContext rctCtx) {
        this.quickbloxClient = quickbloxClient;
        reactApplicationContext = rctCtx;
//        this.quickbloxClient.setupQuickblox(APP_ID, AUTH_KEY, AUTH_SECRET, ACCOUNT_KEY);
    }

    private RNQuickbloxModule quickbloxClient;


    public QBRTCSession getSession() {
        return session;
    }

    /**
     * Set current session (video/call)
     * @param session: QBRTCSession
     */
    public void setSession(QBRTCSession session) {
        if (session != null) {
            this.session = session;
            this.session.addSessionCallbacksListener(this);
            this.session.addVideoTrackCallbacksListener(this);
        } else {
            this.session.removeVideoTrackCallbacksListener(this);
            this.session.removeSessionCallbacksListener(this);
            this.session = session;
        }
    }

    private QBRTCSession session;

    public QBUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(QBUser currentUser) {
        this.currentUser = currentUser;
    }

    private QBUser currentUser;

    public QuickbloxHandler() {
    }

    public void init() {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(reactApplicationContext).addSignaling(qbSignaling);
                        }
                    }
                });

        QBRTCClient.getInstance(reactApplicationContext).addSessionCallbacksListener(new QBRTCClientSessionCallbacks() {
            /**
             * Called each time when new session request is received.
             */
            @Override
            public void onReceiveNewSession(QBRTCSession qbrtcSession) {

                if (session != null && qbrtcSession.getSessionID().equals(session.getSessionID())) {
                    session.rejectCall(new HashMap<String, String>() {{
                        put("key", "value");
                    }});
                    return;
                }
//                Map<String,String> userInfo = qbrtcSession.getUserInfo();
                setSession(qbrtcSession);

                /**
                 * Get CalledId insteadOf get from getUserInfo.get("userId") because userId not in getUserInfo() results
                 */
                Log.d("Monkeyyy", session.getUserInfo().toString());
//                Log.d("Monkeyyy", session.toString());
//                Integer userId = qbrtcSession.getCallerID();
                quickbloxClient.receiveCallSession(session, Integer.valueOf(session.getUserInfo().get("userId")));
//                quickbloxClient.receiveCallSession(session, userId);
            }

            /**
             * Called in case when user didn't answer in timer expiration period
             */
            @Override
            public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
                quickbloxClient.sessionDidClose(qbrtcSession);
                session = null;
            }

            /**
             * Called in case when opponent has rejected you call
             */
            @Override
            public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
                quickbloxClient.userRejectCall(integer);
            }

            /**
             * Called in case when opponent has accepted you call
             */
            @Override
            public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
                quickbloxClient.userAcceptCall(integer);
            }

            /**
             * Called in case when user didn't make any actions on received session
             */
            @Override
            public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

            }

            /**
             * Called in case when opponent hung up
             */
            @Override
            public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
                QuickbloxHandler.this.release();
                quickbloxClient.userHungUp(integer);
            }
            /**
             * Called in case when session will close
             */
            @Override
            public void onSessionStartClose(QBRTCSession qbrtcSession) {

            }

            /**
             * Called when session is closed.
             */
            @Override
            public void onSessionClosed(QBRTCSession qbrtcSession) {
                quickbloxClient.sessionDidClose(qbrtcSession);
                session = null;
            }
        });

        QBRTCClient.getInstance(reactApplicationContext).prepareToProcessCalls();
    }

    public static QuickbloxHandler getInstance() {
        if (instance == null)
            instance = new QuickbloxHandler();

        return instance;
    }

    public void release() {
        if (QuickbloxHandler.this.localViewManager != null)
            QuickbloxHandler.this.localViewManager.release();
        if (QuickbloxHandler.this.remoteVideoViewManager != null)
            QuickbloxHandler.this.remoteVideoViewManager.release();
    }

    public void startCall(List<Integer> userIDs, Integer callRequestId, String realName, String avatar) {
        Log.d(TAG, "start call user: " + userIDs.toString() + " " + realName);
        //Initiate opponents list


        /**
         * @QBConferenceType: For audio/video call.
         */
        QBRTCSession session = QBRTCClient.getInstance(reactApplicationContext).
                createNewSessionWithOpponents(userIDs, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);

        this.setSession(session);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("callRequestId", callRequestId.toString());
        userInfo.put("sessionId", session.getSessionID());
        userInfo.put("realName", realName);
        userInfo.put("avatar", avatar);
        userInfo.put("userId", this.currentUser.getId().toString());

//        Log.d("UserInfo", this.currentUser.getId().toString());

        this.session.startCall(userInfo);
    }


    //<editor-fold desc="QBRTCSessionStateCallback">
    @Override
    public void onStateChanged(QBRTCSession session, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    /**
     * Called in case when connection with the opponent is established
     */
    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    /**
     * Called in case when the opponent is disconnected
     */
    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }
    /**
     * Called in case when connection is closed
     */
    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }
    //</editor-fold>

    //<editor-fold desc="QBRTCClientVideoTracksCallbacks">
    /**
     * Called when local video track was received
     */
    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive");
        if (localViewManager != null)
            localViewManager.renderVideoTrack(qbrtcVideoTrack);
    }
    /**
     * Called when remote video track was received
     */
    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        Log.d(TAG, "onRemoteVideoTrackReceive");
        if (remoteVideoViewManager != null)
            remoteVideoViewManager.renderVideoTrack(qbrtcVideoTrack);
        caller = integer;
    }
    //</editor-fold>

    //<editor-fold desc="QBRTCSessionConnectionCallbacks">
    /**
     * Called in case when connection establishment process is started
     */
    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }
    /**
     * Called in case when the opponent is disconnected by timeout
     */
    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }
    /**
     * Called in case when connection has failed with the opponent
     */
    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }
    /**
     * Called in case of some errors occurred during connection establishment process
     */
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }

//    public void switchCamera(Callback cb) {
//        QBRTCCameraVideoCapturer videoCapturer = (QBRTCCameraVideoCapturer) (session.getMediaStreamManager().getVideoCapturer());
//        videoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
//            @Override
//            public void onCameraSwitchDone(boolean b) {
////                QuickbloxHandler.this.release();
////                quickbloxClient.userHungUp(integer);
//                cb.invoke(b);
//            }
//
//            @Override
//            public void onCameraSwitchError(String s) {
//            }
//        });
//    }
    //</editor-fold>
}
