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

import React, {useState} from 'react';
import InputForm from "../../forms/InputForm";
import {finalizeRegistration, initRegistration} from "../../../service/registration/authenticator";
import {arrayToBase64, base64ToArray} from "../../../codec";

const DeviceRegistration = (props) => {

    const [deviceRegistrationState, setDeviceRegistrationState] = useState({message: ''});
    const [registrationEmail, setRegistrationEmail] = useState(props.email);

    const createCredential = (publicKeyCredentials)  => {
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
                        setDeviceRegistrationState({status: 0,message: 'Device successfully registered'});
                    })
                    .catch(err => {
                    setDeviceRegistrationState({status: 1,message: err.message});
                });
                // send attestation response and client extensions
                // to the server to proceed with the registration
                // of the credential
            }).catch((err) => {
                setDeviceRegistrationState({status: 1,message: err.message});
        });
    };

    const registerAuthenticator = () => {
        if (!window.PasswordCredential) {
            setDeviceRegistrationState({status: 1, message: 'Browser not supported'});
        } else {
            const data = {email: registrationEmail};
            initRegistration(data)
                .then((response) => {
                    createCredential(response.data);
                })
                .catch(err => {
                    setDeviceRegistrationState({
                        status: 1,
                        message: `${err.message} - ${err.response.data.message}`
                    });
                })
        }
    };

    const handleTextChange = (e) => {
        e.preventDefault();
        setRegistrationEmail(e.target.value);
    };

    const onRegisterAuthenticatorClick = (e) => {
        e.preventDefault();
        registerAuthenticator()
    };

    return (
        <form className="measure" key="registration_authenticator_form">
            <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                <legend className="f4 fw6 ph0 mh0">Register Authenticator</legend>
                <div className="mt3" key="ppp">
                    <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                    <InputForm name="user_email" value={registrationEmail} type="email" onChangeText={handleTextChange}/>
                </div>
            </fieldset>
            <div>
                <button name="register" value="Register" onClick={onRegisterAuthenticatorClick} className="b ph3 pv2 input-reset ba  grow pointer f6 dib">Register</button>
            </div>
            <div className={`mv3 ${deviceRegistrationState.status ? 'red':'green'}`}> {deviceRegistrationState.message}</div>
        </form>
    )
};

export default DeviceRegistration;