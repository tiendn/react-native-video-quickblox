/**
 * Created by Dat Tran on 9/27/17.
 */
import React from 'react';
import PropTypes from 'prop-types';
import {
  Text,
  View,
  TouchableOpacity,
  FlatList
} from 'react-native';
import QuickbloxManager from './QuickbloxManager';

export default class ContactList extends React.Component {
    static propTypes = {
        currentUser: PropTypes.object.isRequired
    }

  constructor(props) {
    super(props);


    this.state = {
      contacts: [],
    };
    this.quickbloxManager = new QuickbloxManager();
    this.quickbloxManager.addSubscriber(this);
  }

  componentWillMount() {
    this.quickbloxManager.getUsers(1, 50, (contacts) => {
        let contactResults = [...contacts];
      if (typeof (contacts) === 'string')
        contactResults = JSON.parse(contacts);
        // this.setState({ contacts: JSON.parse(contacts) });
    //   else if (typeof (contacts) === 'object' && Array.isArray(contacts))
        // this.setState({ contacts });
        contactResults = contactResults.filter(contact => contact.id != this.props.currentUser.id);
        this.setState({ contacts: contactResults });
    });
  }

  receiveCall(userInfor) {
        console.log("Receive call");
        const isCaller = false;
        console.log(userInfor)
        // if (typeof realName === 'string')
            this.props.onCalling(isCaller, userInfor);
        // else 
        //     this.props.onCalling(isCaller, 'realName');
  }

  renderListItem(item) {
    return (
        <TouchableOpacity onPress={() => {
            this.quickbloxManager.callUsers([item.id], 1, 'https://qph.ec.quoracdn.net/main-qimg-7ea75331d55c74f7e3c0815cca3e8b4a-c');
            const isCaller = true;
            this.props.onCalling(isCaller, item);
        }}
        >
        <View style={{ flexDirection: 'row', height: 44, alignItems: 'center' }}>
            <Text>{item.id}</Text>
            <View style={{ width: 40 }} />
            <Text>{item.login}</Text>
        </View>
        </TouchableOpacity>
    );
  }

  render() {
    return (
        <View style = {{ paddingTop: '10%' }} >
        <Text>{`Username: ${this.props.currentUser.fullName ? this.props.currentUser.fullName : this.props.currentUser.login}`}</Text>
        <Text>Click to call</Text>
        <FlatList
            keyboardShouldPersistTaps="always"
            style={{ backgroundColor: 'white' }}
            data={this.state.contacts}
            keyExtractor={(item, index) => index}
            renderItem={({ item, index }) => this.renderListItem(item, index)} 
        />
        </View>
    );
  }
}
