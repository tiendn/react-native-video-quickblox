package com.sts.RNQuickblox;

import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.result.HttpStatus;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Dat Tran on 3/22/17.
 */

public class RNQuickbloxModule extends ReactContextBaseJavaModule {
    private static final String TAG = RNQuickbloxModule.class.getSimpleName();

    private static final String DID_RECEIVE_CALL_SESSION = "DID_RECEIVE_CALL_SESSION";
    private static final String USER_ACCEPT_CALL = "USER_ACCEPT_CALL";
    private static final String USER_REJECT_CALL = "USER_REJECT_CALL";
    private static final String USER_HUNG_UP = "USER_HUNG_UP";
    private static final String SESSION_DID_CLOSE = "SESSION_DID_CLOSE";

    private ReactApplicationContext reactApplicationContext;
    private Gson gson;

    public RNQuickbloxModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactApplicationContext = reactContext;
        QuickbloxHandler.getInstance().setQuickbloxClient(this, reactContext);
        this.gson = new Gson();
    }

    private JavaScriptModule getJSModule() {
        return reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    @Override
    public String getName() {
        return "RNQuickblox";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DID_RECEIVE_CALL_SESSION, DID_RECEIVE_CALL_SESSION);
        constants.put(USER_ACCEPT_CALL, USER_ACCEPT_CALL);
        constants.put(USER_REJECT_CALL, USER_REJECT_CALL);
        constants.put(USER_HUNG_UP, USER_HUNG_UP);
        constants.put(SESSION_DID_CLOSE, SESSION_DID_CLOSE);
        return constants;
    }

    @ReactMethod
    public void setupQuickblox(String AppId, String authKey, String authSecret, String accountKey) {
        QBSettings.getInstance().init(reactApplicationContext, AppId, authKey, authSecret);
        QBSettings.getInstance().setAccountKey(accountKey);

        QBChatService.setDebugEnabled(true);
        QBRTCConfig.setDebugEnabled(true);
    }

    @ReactMethod
    public void connectUser(String userId, String password, Callback callback) {
        this.login(userId, password, callback);
    }

    @ReactMethod
    public void signUp(final String userName, final String password, String realName, String email, final Callback callback) {
        final QBUser user = new QBUser();
        user.setLogin(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.setFullName(realName);
        QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                QuickbloxHandler.getInstance().setCurrentUser(qbUser);
                login(userName, password, callback);
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getHttpStatusCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                    login(userName, password, callback);
                } else {
                    callback.invoke(e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void getUsers(int page, int limit, final Callback callback) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(limit);

        QBUsers.getUsers(pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                Log.i(TAG, "Users: " + qbUsers.toString());
                callback.invoke(gson.toJson(qbUsers));
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @ReactMethod
    public void callToUsers(ReadableArray userIDs, final Integer callRequestId, final String realName, final String avatar) {
        List<Integer> ids = new ArrayList<>();
//        ids.add(25581924);

        for (int i = 0; i < userIDs.size(); i++)
            ids.add(userIDs.getInt(i));

        QuickbloxHandler.getInstance().startCall(ids, callRequestId, realName, avatar);
    }

    private void login(String userId, String password, final Callback callback) {
        final QBUser user = new QBUser(userId, password);
        QBAuth.createSession(user).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                user.setId(qbSession.getUserId());
                QBChatService chatService = QBChatService.getInstance();
                chatService.login(user, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        QuickbloxHandler.getInstance().setCurrentUser(user);
                        QuickbloxHandler.getInstance().init();
                        Log.d("User login ", user.toString());
                        callback.invoke(user.getId());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        callback.invoke(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                callback.invoke(e.getMessage());
            }
        });
    }

    @ReactMethod
    public void acceptCall() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");
        QuickbloxHandler.getInstance().getSession().acceptCall(userInfo);
    }

    @ReactMethod
    public void hangUp() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");
        QuickbloxHandler.getInstance().getSession().hangUp(userInfo);
        QuickbloxHandler.getInstance().setSession(null);

        QuickbloxHandler.getInstance().release();
    }

    @ReactMethod
    public void switchCamera(final Callback callback) {
        QBRTCCameraVideoCapturer videoCapturer = (QBRTCCameraVideoCapturer) (QuickbloxHandler.getInstance().getSession().getMediaStreamManager().getVideoCapturer());
        videoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                callback.invoke(null, b);
            }

            @Override
            public void onCameraSwitchError(String s) {
                callback.invoke(s);
            }
        });
    }

    @ReactMethod
    public void toggleAudio() {
        QBMediaStreamManager mediaStreamManager = QuickbloxHandler.getInstance().getSession().getMediaStreamManager();
        mediaStreamManager.setAudioEnabled(!mediaStreamManager.isAudioEnabled());
    }

    @ReactMethod
    public void toggleVideo() {
        QBMediaStreamManager mediaStreamManager = QuickbloxHandler.getInstance().getSession().getMediaStreamManager();
        mediaStreamManager.setVideoEnabled(!mediaStreamManager.isVideoEnabled());
    }

    /**
     * Set mute/unmute audio
     * @param isEnabled
     */
    @ReactMethod
    public void setAudioEnabled(boolean isEnabled) {
        QBMediaStreamManager mediaStreamManager = QuickbloxHandler.getInstance().getSession().getMediaStreamManager();
        mediaStreamManager.setAudioEnabled(isEnabled);
    }

    /**
     * Set mute/unmute video
     * @param isEnabled
     */
    @ReactMethod
    public void setVideoEnabled(boolean isEnabled) {
        QBMediaStreamManager mediaStreamManager = QuickbloxHandler.getInstance().getSession().getMediaStreamManager();
        mediaStreamManager.setVideoEnabled(isEnabled);
    }

    @ReactMethod
    public void rejectCall() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");
        QuickbloxHandler.getInstance().getSession().rejectCall(userInfo);
        QuickbloxHandler.getInstance().setSession(null);
    }

    public void receiveCallSession(QBRTCSession session) {
        WritableMap params = Arguments.createMap();
//        Log.d("ReceiveCallSession ", session.getUserInfo().toString());
//        Log.d("ReceiveCallSession ", session.getUserInfo().get("realName").toString());

        params.putInt("userId", Integer.valueOf(session.getUserInfo().get("userId")));
        params.putString("realName", session.getUserInfo().get("realName"));

//        params.putString("realName", );
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(DID_RECEIVE_CALL_SESSION, params);
    }

    public void userAcceptCall(Integer userId) {
        Log.d("UserAcceptCallMonkey", userId.toString());
        WritableMap params = Arguments.createMap();
        params.putString("", "");
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(USER_ACCEPT_CALL, params);
    }

    public void userRejectCall(Integer userId) {
        WritableMap params = Arguments.createMap();
        params.putString("", "");
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(USER_REJECT_CALL, params);
    }

    public void userHungUp(Integer userId) {
        WritableMap params = Arguments.createMap();
        params.putString("", "");
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(USER_HUNG_UP, params);
    }

    public void sessionDidClose(QBRTCSession session) {
        WritableMap params = Arguments.createMap();
        params.putString("", "");
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(SESSION_DID_CLOSE, params);
    }
}
