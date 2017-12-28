/**
 * Created by DaoNamTien on 12/20/17.
 */

import React from "react";
import PropTypes from 'prop-types';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Image,
} from "react-native";
import VideoCall from './VideoCalling';
import AvatarLogo from '../assets/avatar.jpg';
import DeclineLogo from '../assets/decline_call.png';
import AcceptLogo from '../assets/accept_call.png';
import QuickbloxManager from './QuickbloxManager';

export default class Calling extends React.Component {
    static propTypes = {
        // user: PropTypes.object,
        onReject: PropTypes.func.isRequired,
        isCaller: PropTypes.bool.isRequired,
        opponentInfor: PropTypes.object
    }

    constructor(props) {
        super(props);
        this.quickbloxManager = new QuickbloxManager();
        this.quickbloxManager.addUserActionSubcriber(this);
    }
    state = {
        acceptCall: false,
        hangup: false
    };

    acceptCall() {
        this.setState({ acceptCall: true });
        this.quickbloxManager.acceptCall();
    }

    userAcceptCall() {
        console.log("HAHAHA")
        this.setState({ acceptCall: true });
    }

    userRejectCall() {
        console.log("Close call");
        this.props.onReject();
    }

    userHangup() {
        this.setState({ hangup: true });
    }

    renderOptions() {
        if (this.props.isCaller) 
            return (
                <View style={{ alignItems: 'center' }}>
                    <TouchableOpacity 
                        style={[styles.buttonContainer, { backgroundColor: "red" }]} 
                        onPress={() => this.props.onReject()} 
                        underlayColor="transparent"
                    >
                        <Image
                            style={styles.icon}
                            source={DeclineLogo}
                        />
                        {/* <Text style={styles.iconTitle}>Decline</Text> */}
                    </TouchableOpacity>
                </View>
            );
        return (
            <View style={[styles.callButtonContainer]}>
                <TouchableOpacity 
                    style={[styles.buttonContainer, { backgroundColor: "rgb(68,175,35)" }]} 
                    onPress={() => this.acceptCall()} 
                    underlayColor="transparent"
                >
                    <Image
                        style={styles.icon}
                        source={AcceptLogo}
                    />
                        {/* <Text style={styles.iconTitle}>Accept</Text> */}
                </TouchableOpacity>
                <View style = {{ width: 20 }} /> 
                <TouchableOpacity 
                    style={[styles.buttonContainer, { backgroundColor: "red" }]} 
                    onPress={() => this.props.onReject()} 
                    underlayColor="transparent"
                >
                    <Image
                        style={styles.icon}
                        source={DeclineLogo}
                    />
                    {/* <Text style={styles.iconTitle}>Decline</Text> */}
                </TouchableOpacity>
            </View>
        );
    }

    render() {
        const { acceptCall, hangup } = this.state;
        const { opponentInfor, isCaller } = this.props;
        // console.log(" User opponent ", opponentInfor);

        if (acceptCall)
            return <VideoCall hangup = {hangup} callEnd = {() => this.props.onReject()} />;
        return (
            <View style={styles.container}>
                <View style={styles.callDetails}>
                    <Image
                        style={{ height: 60, width: 60 }}
                        source={AvatarLogo}
                    />
                    <Text style = {styles.opponentName}>{ isCaller ? opponentInfor.full_name : opponentInfor.realName}</Text>
                    <View style={{ marginTop: 20, marginBottom: 20 }} />
                    <Text style={styles.name}>Call Request</Text>
                    <TouchableOpacity onPress={() => {}} underlayColor="transparent">
                    <Text>Ringing...</Text>
                    </TouchableOpacity>
                </View>
                {this.renderOptions()}
            </View>
        );
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "white",
    padding: 20
  },
  callDetails: {
    flex: 3,
    justifyContent: "center",
    alignItems: "center"
  },
  avatar: {
    height: 100,
    width: 100,
    borderRadius: 50
  },
  opponentName: {
      textAlign: 'center',
      fontSize: 22,
  },
  name: {
    fontSize: 18
  },
  callButtonContainer: {
    flex: 1,
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between"
  },
  buttonContainer: {
    alignItems: "center",
    height: 60,
    width: 60,
    borderRadius: 30,
    justifyContent: "center",
  },
  icon: {
    height: 30,
    width: 30
  },
//   iconTitle: {
    // fontSize: 15,
//   }
});
