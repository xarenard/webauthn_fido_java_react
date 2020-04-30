/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import 'tachyons';
import Menu from '../component/menu';
import DeviceRegistration from '../component/registration/authenticator';
import {Route,Switch} from 'react-router';
import {initRegistration, finalizeRegistration} from '../service/registration/authenticator';
import Home from '../component/home';
import '../../css/index.css';
import {base64ToArray, arrayToBase64} from '../codec';
import Navigation from '../component/navigation';
import Authentication from '../component/authentication';
import {authenticationInit, authenticationFinalize} from '../service/authentication';
import UserRegistration from '../component/registration/user';
import {registerFidoUser} from "../service/registration/user";

class App extends React.Component {

    constructor(props) {
        super(props);
        this.createCredential = this.createCredential.bind(this);
        this.registerAuthenticator = this.registerAuthenticator.bind(this);
        this.authenticate = this.authenticate.bind(this);
        this.initialStateValues = this.initialStateValues.bind(this);
        this.resetState = this.resetState.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.registerUser = this.registerUser.bind(this);
        this.state = this.initialStateValues();
    }

    initialStateValues() {
        return {
            message: '',
            registrationStatus: 1,
            authenticationStatus: 1,
            email: DEFAULT_USER_EMAIL,
            user_email: '',
            lastName: '',
            firstName: ''
        };
    }

    resetState() {
        const stateValues = this.initialStateValues();
        this.setState(() => ({...stateValues,
            email: this.state.email,
            user_email: this.state.user_email,
            firstName: this.state.firstName,
            lastName: this.state.lastName
        }));
    }

    createCredential(publicKeyCredentials) {
        publicKeyCredentials.publicKey.challenge = base64ToArray(publicKeyCredentials.publicKey.challenge);
        const userId = publicKeyCredentials.publicKey.user.id;
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

                finalizeRegistration(data, userId)
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
            const data = {email: this.state.email};

            authenticationInit(data).then(
                (res) => {
                    const publicKeyCredentials = res.data;
                    publicKeyCredentials.publicKey.challenge = base64ToArray(publicKeyCredentials.publicKey.challenge);
                   // const userId = publicKeyCredentials.publicKey.user.id;
                    if(publicKeyCredentials.publicKey.allowCredentials){
                        publicKeyCredentials.publicKey.allowCredentials = publicKeyCredentials.publicKey.allowCredentials
                            .map(ac => {
                                    const obj = Object.assign({}, ac);
                                    obj.id = base64ToArray(obj.id);
                                    return obj;
                                }
                            );
                    }
                    const that = this;
                    navigator.credentials.get(publicKeyCredentials)
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

    registerUser(){
        this.setState(() => ({message: ''}));
        registerFidoUser(
            {email: this.state.user_email,
                    lastName: this.state.lastName,
                    firstName: this.state.firstName}
        )

    }

    registerAuthenticator() {
        this.setState(() => ({message: ''}));
        console.log(this.state.email);
        if (!window.PasswordCredential) {
            this.setState(() => ({message: 'Browser not supported'}));
        } else {
            const data = {email: this.state.email};
            initRegistration(data)
                .then((response) => {
                    this.createCredential(response.data);
                })
                .catch(err => {
                    this.setState(() => ({'message': `${err.message} - ${err.response.data.message}`}));
                });
        }
    };

    handleInputChange(inputName, inputValue){
        this.setState(() => ({[inputName]: inputValue}))
    }

    render() {
        return (
            <div>
                <Navigation/>
                <div className='fl w-100 pt4'>
                    <div className='fl w-30'><Menu init={this.resetState}/></div>
                    <div className='fl w-60'>
                        <Switch>
                        <Route exact path='/' component={() => <Home/>}/>
                        <Route exact path='/user' render={() => <UserRegistration  registerUser={this.registerUser}
                                                                                        handleUserInput={this.handleInputChange}
                                                                                        email={this.state.user_email}
                                                                                        lastName = {this.state.lastName}
                                                                                        firstName = {this.state.firstName}
                        />} />
                        <Route exact path='/device' render={() => <DeviceRegistration register={this.registerAuthenticator}
                                                                                         message={this.state.message}
                                                                                         email={this.state.user_email ===''?this.state.email:this.state.user_email}
                                                                                         registrationStatus={this.state.registrationStatus}
                                                                                         handleUserInput={this.handleInputChange}
                        />}/>
                        <Route exact path='/authentication'
                               render={() => <Authentication authenticate={this.authenticate}
                                                                message={this.state.message}
                                                                email={this.state.user_email ===''?this.state.email:this.state.user_email}
                                                                handleIdChange={this.handleIdChange}
                                                                authenticationStatus={this.state.authenticationStatus}/>}/>
                        </Switch>
                    </div>
                </div>
            </div>);
    }
}

export default App;