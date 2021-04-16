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
import InputForm from "../forms/InputForm";
import {authenticationFinalize, authenticationInit} from "../../service/authentication";
import {arrayToBase64, base64ToArray} from "../../codec";

const Authentication = (props) => {

    const {email:defaultEmail} = props;
    const [email,setEmail] = useState(defaultEmail);
    const [authenticationResult, setAuthenticationResult] = useState({status: 1,message: ''});

    const authenticate = () => {

        if (!window.PasswordCredential) {
            setAuthenticationResult({'status': 1, message:'Browser Not Supported'});
        } else {
            const data = {email:email};

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
                                .then(() => {
                                        setAuthenticationResult({'status': 0, message: 'Authentication Successful'});
                                    }
                                ).catch((err) =>
                                setAuthenticationResult({'status': 1, message:err.message}))
                        })
                        .catch(err =>
                            setAuthenticationResult ({'message': err.message, 'status': 1}));
                }
            ).catch(err => setAuthenticationResult ({'message': err.message, 'status': 1}));
        }
    };

    const onAuthenticateClick = (e) => {
        e.preventDefault();
        setAuthenticationResult({'status': 1, message:''});
        authenticate();
    };

    const handleInputChange = (e) =>{
        e.preventDefault();
        setEmail(e.target.value);
        //props.handleUserInput(e.target.name, e.target.value);
    };

    return (
        <div>
            <form className="measure">
                <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                    <legend className="f4 fw6 ph0 mh0">Authentication</legend>
                    <div className="mt3">
                        <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                        <InputForm name="user_email" value={email} type="email" onChangeText={handleInputChange}/>
                    </div>
                    </fieldset>
                    <div>
                        <button name="authenticate" value="Authenticate" onClick={onAuthenticateClick} className="b ph3 pv2 input-reset ba grow pointer f6 dib">Authenticate</button>
                    </div>
                </form>
                <div className={`mv3 ${authenticationResult.status?'red':'green'}`}>{authenticationResult.message}</div>
            </div>
        )

};

export default Authentication;