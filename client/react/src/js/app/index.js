import React from 'react';
import 'tachyons';
import Menu from '../component/menu';
import DeviceRegistration from '../component/registration/authenticator';
import {Route} from 'react-router';
import {initRegistration, finalizeRegistration} from '../service/registration/authenticator';
import Home from '../component/home';
import '../../css/index.css';
import {base64ToArray, arrayToBase64} from '../codec';
import Navigation from '../component/navigation';
import Authentication from '../component/authentication';
import {authenticationInit, authenticationFinalize} from '../service/authentication';


class App extends React.Component {

    constructor(props) {
        super(props);
        this.createCredential = this.createCredential.bind(this);
        this.register = this.register.bind(this);
        this.authenticate = this.authenticate.bind(this);
        this.initialStateValues = this.initialStateValues.bind(this);
        this.resetState = this.resetState.bind(this);
        this.state = this.initialStateValues();
    }

    initialStateValues() {
        return {
            message: '',
            registrationStatus: 1,
            authenticationStatus: 1
        };
    }

    resetState() {
        this.setState(() => this.initialStateValues());
    }

    createCredential(publicKeyCredentials) {
        publicKeyCredentials.publicKey.challenge = base64ToArray(publicKeyCredentials.publicKey.challenge);
        publicKeyCredentials.publicKey.user.id = base64ToArray(publicKeyCredentials.publicKey.user.id);

        if (publicKeyCredentials.publicKey.excludeCredentials) {
            publicKeyCredentials.publicKey.excludeCredentials = publicKeyCredentials.publicKey.excludeCredentials
                .map(ec => {
                        const obj = Object.assign({}, ec);
                        obj.id = base64ToArray(obj.id);
                        return obj;
                    }
                );
        }

        const that = this;
        navigator.credentials.create(publicKeyCredentials)
            .then((newCredentialInfo) => {
                const data = {
                    id: newCredentialInfo.id,
                    rawId: arrayToBase64(newCredentialInfo.rawId),
                    response: {
                        attestationObject: arrayToBase64(newCredentialInfo.response.attestationObject),
                        clientDataJSON: arrayToBase64(newCredentialInfo.response.clientDataJSON)
                    },
                    type: newCredentialInfo.type
                };

                finalizeRegistration(data)
                    .then(() => {
                        that.setState(() => ({'message': 'Device successfully registered.', 'registrationStatus': 0}));
                    }).catch(err => {
                    that.setState(() => ({'message': err.message, 'registrationStatus': 1}));
                });
                // send attestation response and client extensions
                // to the server to proceed with the registration
                // of the credential
            }).catch((err) => {
            that.setState(() => ({'message': err.message, 'registrationStatus': 1}));
        });
    }

    authenticate() {
        this.setState(() => ({message: ''}));

        if (!window.PasswordCredential) {
            this.setState(() => ({message: 'Browser not supported'}));
        } else {
            authenticationInit().then(
                (res) => {

                    const publicKey = res.data;
                    publicKey.publicKey.challenge = base64ToArray(publicKey.publicKey.challenge);
                    publicKey.publicKey.allowCredentials[0].id = base64ToArray(publicKey.publicKey.allowCredentials[0].id);

                    const that = this;
                    navigator.credentials.get(publicKey)
                        .then(credential => {
                            const data = {
                                id: credential.id,
                                rawId: arrayToBase64(credential.rawId),
                                response: {
                                    authenticatorData: arrayToBase64(credential.response.authenticatorData),
                                    clientDataJSON: arrayToBase64(credential.response.clientDataJSON),
                                    signature: arrayToBase64(credential.response.signature),
                                    userHandle: arrayToBase64(credential.response.userHandle)
                                },
                                type: credential.type
                            };

                            authenticationFinalize(data)
                                .then(() =>
                                    that.setState(() => ({
                                        'message': 'Authentication Successful',
                                        'authenticationStatus': 0
                                    }))
                                ).catch(err =>
                                that.setState(() => ({'message': err.message, 'authenticationStatus': 1})));

                        })
                        .catch(err =>
                            that.setState(() => ({'message': err.message, 'authenticationStatus': 1})));
                }
            )
        }
    }

    register() {
        this.setState(() => ({message: ''}));
        if (!window.PasswordCredential) {
            this.setState(() => ({message: 'Browser not supported'}));
        } else {
            initRegistration()
                .then((response) => {
                    this.createCredential(response.data);
                })
                .catch(err => {
                    this.setState(() => ({'message': err.message}));
                });
        }
    };

    render() {
        return (
            <div>
                <Navigation/>
                <div className='fl w-100 pt4'>
                    <div className='fl w-30'><Menu init={this.resetState}/></div>
                    <div className='fl w-60'>
                        <Route exact path='/' component={() => <Home/>}/>
                        <Route exact path='/device' component={() => <DeviceRegistration register={this.register}
                                                                                         message={this.state.message}
                                                                                         registrationStatus={this.state.registrationStatus}/>}/>
                        <Route exact path='/authentication'
                               component={() => <Authentication authenticate={this.authenticate}
                                                                message={this.state.message}
                                                                authenticationStatus={this.state.authenticationStatus}/>}/>
                    </div>
                </div>
            </div>);
    }
}

export default App;