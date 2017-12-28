/**
 * Created by DaoNamTien on 12/20/17.
 */

import React from "react";
import PropTypes from 'prop-types';
import {
  StyleSheet,
  Text,
  View,
  TouchableHighlight,
  Image,
  Platform
} from "react-native";
import {
  QuickbloxLocalVideoView,
  QuickbloxRemoteVideoView
} from "react-native-video-quickblox";
import QuickbloxManager from "./QuickbloxManager";
import DeclineLogo from '../assets/decline_call.png';
import SwitchCameraLogo from '../assets/switch_camera.png';
import HangupLogo from '../assets/hangup.png';

export default class VideoCalling extends React.Component {

    static propTypes = {
        callEnd: PropTypes.func.isRequired,
        hangup: PropTypes.bool
    }

    constructor(props) {
        super(props);
        this.state = {
            bottom: 1,
            counter: 0
        //   calling: true
        };
        this.quickbloxManager = new QuickbloxManager();
    } 

    componentDidMount() {
        // this.countTime();
    }

    // countTime() {
    //     this.countInterval = setInterval(() => {
    //         this.setState(prevState => ({ counter: prevState.counter + 1 }));
    //     }, 1000);
    // }

  render() {
      return (
        <View style={styles.container}>
          <Text style={styles.iconTitle}>
            {setTime(this.state.counter)}
          </Text>
          <QuickbloxRemoteVideoView style={styles.callDetails}>
            {Platform.OS === "android" ? (
              <QuickbloxLocalVideoView
                style={[styles.userVideo, { bottom: this.state.bottom }]}
                onRendered={() => this.setState({ bottom: 0 })}
              />
            ) : (
              <QuickbloxLocalVideoView style={styles.userVideo} />
            )}
          </QuickbloxRemoteVideoView>
          <View style={styles.callButtonContainer}>
            <TouchableHighlight
                onPress={() => {
                    this.quickbloxManager.switchCamera();
                }}
                underlayColor="transparent"
            >
                <View style={styles.buttonContainer}>
                    <View
                    style={[styles.iconContainer, { width: 50, height: 50, backgroundColor: "white", borderColor: 'black', borderWidth: 1 }]}
                    >
                    <Image style={styles.icon} source={SwitchCameraLogo} />
                    </View>
                </View>
            </TouchableHighlight>
            <TouchableHighlight
              onPress={() => {
                this.quickbloxManager.userRejectCall();
                this.props.callEnd();
              }}
              underlayColor="transparent"
            >
              <View style={styles.buttonContainer}>
                <View
                  style={[styles.iconContainer, { backgroundColor: "red" }]}
                >
                  <Image style={styles.icon} source={DeclineLogo} />
                </View>
              </View>
            </TouchableHighlight>
            <TouchableHighlight
              onPress={() => {
                // this.quickbloxManager.hangUp();
                this.quickbloxManager.toggleVideo(); // Mute 
              }}
              underlayColor="transparent"
            >
              <View style={styles.buttonContainer}>
                <View
                  style={[styles.iconContainer, { width: 50, height: 50, backgroundColor: "white", borderColor: 'black', borderWidth: 1 }]}
                >
                  <Image style={styles.icon} source={HangupLogo} />
                </View>
              </View>
            </TouchableHighlight>
          </View>
        </View>
      );
  }
}

function setTime(totalSeconds) {
  const sec = pad(totalSeconds % 60, 10);
  const min = pad(parseInt(totalSeconds / 60, 10));
  return `${min} : ${sec}`;
}

function pad(val) {
  if (val < 10) {
    return `0${val}`;
  }

  return val;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "white",
    paddingTop: 20,
    // flexDirection: 'row'
  },
  callDetails: {
    flex: 3,
    // width: '100%',
    backgroundColor: "#6969"
  },
  userVideo: {
    position: "absolute",
    bottom: 1,
    right: 0,
    width: 80,
    height: 120,
    backgroundColor: "green",
    overflow: "hidden"
  },
  callButtonContainer: {
    flex: 1,
    justifyContent: "space-around",
    alignItems: "center",
    flexDirection: "row"
  },
  iconContainer: {
    height: 60,
    width: 60,
    borderRadius: 30,
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 10
  },
  buttonContainer: {
    alignItems: "center"
  },
  icon: {
    height: 30,
    width: 30
  },
  iconTitle: {
    alignItems: "center",
    fontSize: 15,
    alignSelf: "center"
  }
});
