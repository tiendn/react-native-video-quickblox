import React from 'react';
import PropTypes from 'prop-types';
import { View, StyleSheet } from 'react-native';
import ContactList from './ContactList';
import Calling from './Calling';

export default class Main extends React.Component {
    static propTypes = {
        user: PropTypes.object.isRequired
    }

    state = {
        isCaller: false,
        calling: false,
        opponentInfor: null
    }
    
    render() {
        return (
            <View style = {styles.container}>
                {
                    this.state.calling 
                        ? <Calling opponentInfor = {this.state.opponentInfor} isCaller = {this.state.isCaller} onReject = {() => this.setState({ calling: false })} />
                        : <ContactList currentUser = {this.props.user} onCalling = {(isCaller, opponentInfor) => this.setState({ calling: true, isCaller, opponentInfor })} />
                }
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        // alignItems: 'center'
    }
});
