/**
 * Created by Dat Tran on 2/10/17.
 * Modified by Dao Tien on 13/12/17.
 */
import React from 'react';
import { NativeEventEmitter, Platform } from 'react-native';
import RNQuickblox from 'react-native-video-quickblox';

export const DID_RECEIVE_CALL_SESSION = 'DID_RECEIVE_CALL_SESSION';
export const USER_ACCEPT_CALL = 'USER_ACCEPT_CALL';
export const USER_REJECT_CALL = 'USER_REJECT_CALL';
export const USER_HUNG_UP = 'USER_HUNG_UP';
export const SESSION_DID_CLOSE = 'SESSION_DID_CLOSE';
const APP_ID = "65913";
const AUTH_KEY = "S7wGTmdLV4Np8cs";
const AUTH_SECRET = "GFMUzTPryFggTGR";
const ACCOUNT_KEY = "qAz3cN5pzqMhEY7oMks-";


const QuickbloxModule = new NativeEventEmitter(RNQuickblox);

let instance = null;

export default class QuickbloxManager {
  constructor() {
    if (!instance) {
      instance = this;
      this.subscriber = [];
      this.userActionSubcriber = [];
      this.registerEvents();
    }
    return instance;
  }

  init() {
    // RNQuickblox.setupQuickblox(APP_ID, AUTH_KEY, AUTH_SECRET, ACCOUNT_KEY);
    RNQuickblox.setupQuickblox("44519", "YqHTqrJPDkAzht3", "fgYy8K3hL6LKHaS", "6XDmKdXBfwPuJsWv9Fxp");
    // RNQuickblox.setupQuickblox("65913", "S7wGTmdLV4Np8cs", "GFMUzTPryFggTGR", "qAz3cN5pzqMhEY7oMks-");
  }

  addSubscriber(subscriber) {
    this.subscriber.push(subscriber);
  }

  addUserActionSubcriber(subscriber) {
      this.userActionSubcriber = subscriber;
  }

  registerEvents() {
    QuickbloxModule.addListener(RNQuickblox.DID_RECEIVE_CALL_SESSION, this.receiveCall.bind(this));
    QuickbloxModule.addListener(RNQuickblox.USER_ACCEPT_CALL, this.userAcceptCall.bind(this));
    QuickbloxModule.addListener(RNQuickblox.USER_REJECT_CALL, this.userRejectCall.bind(this));
    QuickbloxModule.addListener(RNQuickblox.SESSION_DID_CLOSE, this.sessionDidClose.bind(this));
    QuickbloxModule.addListener(RNQuickblox.USER_HUNG_UP, this.userHungUp.bind(this));
  }

  getUsers(page = 1, limit = 50, users) {
      if (Platform.OS === 'android')
        RNQuickblox.getUsers(page, limit, users);
    else 
        RNQuickblox.getUsers(users);
  }

  login(userName, password, cb) {
    RNQuickblox.connectUser(userName, password, (qbId) => {
      if (cb)
        cb(qbId);
    });
  }

  signUp(userName, password, realName, email, complete) {
    RNQuickblox.signUp(userName, password, realName, email, () => {
      if (complete)
        complete({ userName, password });
    });
  }

  switchCamera() {
      RNQuickblox.switchCamera((err, data) => {
          if (err) {
              alert("Error switch camera");
          } else {
              console.log(data);
          }
      });
  }

  setAudioEnabled(isEnabled) {
      RNQuickblox.setAudioEnabled(isEnabled);
  }

  toggleAudio() {
    RNQuickblox.toggleAudio();
  }

  setVideoEnabled(isEnabled) {
      console.log(isEnabled)
    RNQuickblox.setVideoEnabled(isEnabled);
}

toggleVideo() {
    RNQuickblox.toggleVideo();
}

  hangUp() {
    RNQuickblox.hangUp();
  }

  callUsers(userIds, callId, avatar) {
    RNQuickblox.callToUsers(userIds, callId, avatar);
  }

  acceptCall() {
    RNQuickblox.acceptCall();
  }

  receiveCall(userId, userInfor) {
    console.log(userInfor);
    // RNQuickblox.getTime(time => console.log(time));
    this.subscriber.forEach(sub => sub.receiveCall(userId, userInfor));

    // store.dispatch({type: SET_IN_COMING_CALL_MODAL})
  }

  userAcceptCall(data) {
    console.log('userAcceptCall', data);
    console.log(this);
    // this.userActionSubcriber.forEach(sub => sub.userAcceptCall()); // --> Error userAcceptCall is not a function.
    this.userActionSubcriber.userAcceptCall();
    // this.subscriber.forEach(sub => sub.userAcceptCall());
    // this.store.dispatch({type: USER_ACCEPT_CALL})
  }

  userRejectCall() {
    RNQuickblox.rejectCall();
    this.userActionSubcriber.userRejectCall();
    console.log('userRejectCall');
    // this.store.dispatch({type: USER_REJECT_CALL})
  }

  sessionDidClose() {
    console.log('sessionDidClose', RNQuickblox);
    // RNQuickblox.onSessionClosed();
    // this.store.dispatch({type: SESSION_DID_CLOSE})
  }

  userHungUp() {
    console.log('hangup');
    this.userActionSubcriber.userHangup();
    // this.store.dispatch({type: USER_HUNG_UP})
  }
}
