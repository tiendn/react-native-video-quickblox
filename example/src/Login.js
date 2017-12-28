/**
 * Created by Dao Nam Tien on 12/20/17.
 */
import React from "react";
import PropTypes from 'prop-types';
import { Text, Button, View, TextInput, StyleSheet } from "react-native";
import QuickbloxManager from "./QuickbloxManager";
import IndicatorDialog from "./IndicatorDialog";

export default class Login extends React.Component {

    static propTypes = {
        onLoginSuccess: PropTypes.func.isRequired
    }
    
  constructor(props) {
    super(props);
    this.state = {
      userName: "",
      password: "12345678",
      waiting: false
    };

    this.quickbloxManager = new QuickbloxManager();
  }

  login = () => {
    this.setState({ waiting: true });
    this.quickbloxManager.login(
      this.state.userName,
      this.state.password,
      (user) => {
        //   console.log(typeof qbId);
        
        const userInfor = typeof user === 'string' ? JSON.parse(user) : user;
        // console.log(user);
        this.setState({ waiting: false });
        this.props.onLoginSuccess(userInfor);
      }
    );
  };

  render() {
    return (
      <View style={styles.container}>
        {/* <Text>login name: test or test01 - password: 12345678</Text> */}
        <Text>Login name: test01 or test02 </Text>
        <Text>password: 12345678</Text>
        <View style = {{ height: 10 }} />
        <TextInput
          autoFocus
          style = {styles.input}
          placeholder="Login name"
          underlineColorAndroid="transparent"
          onChangeText={text => this.setState({ userName: text })}
        />
        <View style = {{ height: 10 }} />
        <TextInput
          placeholder="Password"
          style = {styles.input}
          underlineColorAndroid="transparent"
          secureTextEntry
          onChangeText={text => this.setState({ password: text })}
        />
        <View style = {{ height: 10 }} />
        <Button onPress={() => this.login()} title="Login" />
        {this.state.waiting && <IndicatorDialog message="Please wait" />}
      </View>
    );
  }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        paddingTop: '20%'
    },
    input: {
        width: '80%',
        paddingVertical: 5,
        paddingHorizontal: 10,
        borderRadius: 5,
        borderWidth: 1,
        borderColor: '#d7d7d7'
    }
});