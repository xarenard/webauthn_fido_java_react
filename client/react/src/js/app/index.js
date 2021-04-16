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
import 'tachyons';
import Menu from '../component/menu';
import DeviceRegistration from '../component/registration/authenticator';
import {Route, Switch} from 'react-router';
import Home from '../component/home';
import '../../css/index.css';
import Navigation from '../component/navigation';
import Authentication from '../component/authentication';
import UserRegistration from '../component/registration/user';
import {registerFidoUser} from "../service/registration/user";

const App = () => {

    const [userInformation, setUserInformation] = useState({
        email: DEFAULT_USER_EMAIL,
        lastName: '',
        firstName: ''
    });

    const registerUser = () => {
        return registerFidoUser(
            {
                email: userInformation.email,
                lastName: userInformation.lastName,
                firstName: userInformation.firstName
            }
        )
    };

    const handleUserRegistrationInputChange = (inputName, inputValue) => {
        setUserInformation({...userInformation, [inputName]: inputValue});
    };

    return (
        <div>
            <Navigation/>
            <div className='fl w-100 pt4'>
                <div className='fl w-30'><Menu/></div>
                <div className='fl w-60'>
                    <Switch>
                        <Route exact path='/' component={() => <Home/>}/>
                        <Route exact path='/user' render={() => <UserRegistration registerUser={registerUser}
                                                                                  userInfo={userInformation}
                                                                                  handleUserInput={handleUserRegistrationInputChange}
                        />}/>

                        <Route exact path='/device' render={() => <DeviceRegistration email={userInformation.email}/>}/>
                        <Route exact path='/authentication'
                               render={() => <Authentication email={userInformation.email}/>}/>
                    </Switch>
                </div>
            </div>

        </div>);

};

export default App;
