/**
 * Sample React Native App integration with QuickBlox chat
 * @author: Dao Nam Tien
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform, PermissionsAndroid
} from 'react-native';
// import Calling from './Calling'
import QuickbloxManager from './QuickbloxManager';
// import VideoCalling from './VideoCalling';
import Login from './Login';
import Main from './Main';

export default class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
        // isLogin: false,
        // calling: false
        user: null
        // userId: 33823668
    };

    this.quickbloxManager = new QuickbloxManager();
    this.quickbloxManager.init();

    if (Platform.OS === 'android')
        requestPermissions();
  }

//   receiveCall() {
//     this.quickbloxManager.acceptCall();
//     this.setState({ calling: true });
//   }

  render() {
    // return !this.state.isLogin ? <Login callSuccess={() => this.setState({ calling: true })} /> : <VideoCalling callEnd = {() => this.setState({ calling: false })} />;
    if (this.state.user)
        return <Main user = {this.state.user} />;
    return <Login onLoginSuccess = {user => this.setState({ user })} />;
  }
}

function requestPermissions() {
  try {
    return PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.CAMERA,
      PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
      PermissionsAndroid.PERMISSIONS.PROCESS_OUTGOING_CALLS,
      PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
      PermissionsAndroid.PERMISSIONS.CALL_PHONE]);
  } catch (err) {
    console.log(err);
  }
}
