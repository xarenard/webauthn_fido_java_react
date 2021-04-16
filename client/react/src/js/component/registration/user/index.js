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


 const UserRegistration  = (props) => {
     const [userRegistrationMessage, setUserRegistrationMessage] = useState('');


    const handleInputChange = (e) =>{
        e.preventDefault();
        props.handleUserInput(e.target.name, e.target.value);

    };

    const onUserRegistrationClick = (e) => {
        e.preventDefault();
        props.registerUser()
            .then(() => setUserRegistrationMessage("User registered"))
            .catch(err => setUserRegistrationMessage(err.message));
    };


    return (
            <div>
            <form className="measure" key="form_user_registration">

                <fieldset className="ba b--transparent ph0 mh0">
                    <div className="mt3" key="user_registration_email">
                         <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                        <InputForm name="email" value={props.userInfo.email} type="email" onChangeText={handleInputChange}/>
                    </div>
                    <div className="mt3" key="eee">
                        <label className="db fw6 lh-copy f6" htmlFor="Last Name">Last Name</label>
                        <InputForm name="lastName" value={props.userInfo.lastName} type="text" onChangeText={handleInputChange}/>
                    </div>
                    <div className="mt3" key="eeeaa">
                        <label className="db fw6 lh-copy f6" htmlFor="First Name">First Name</label>
                        <InputForm name="firstName" value={props.userInfo.firstName} type="text" onChangeText={handleInputChange}/>
                    </div>
                    <div className="mt3">
                        <button name="register" value="Register" onClick={onUserRegistrationClick} className="b ph3 pv2 input-reset ba  grow pointer f6 dib">Register</button>
                    </div>
                </fieldset>
            </form>
            <div>{userRegistrationMessage}</div>
            </div>
    )
};

export default UserRegistration;