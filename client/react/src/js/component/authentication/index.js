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
import InputForm from "../forms/InputForm";

class Authentication extends React.Component {

    constructor(props) {
        super(props);
        this.authenticateClick = this.authenticateClick.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    authenticateClick(e) {
        e.preventDefault();
        this.props.authenticate();
    }

    handleInputChange(e){
        e.preventDefault();
        this.props.handleUserInput(e.target.name, e.target.value);
    }

    render(){
        const {message, authenticationStatus,email} = this.props;
        return (
            <div>
                <form className="measure">
                    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                        <legend className="f4 fw6 ph0 mh0">Authentication</legend>
                        <div className="mt3">
                            <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                            <InputForm name="user_email" value={email} type="email" onChangeText={this.handleInputChange}/>
                        </div>
                    </fieldset>
                    <div>
                        <button name="authenticate" value="Authenticate" onClick={this.authenticateClick} className="b ph3 pv2 input-reset ba grow pointer f6 dib">Authenticate</button>
                    </div>
                </form>
                <div className={`mv3 ${authenticationStatus?'red':'green'}`}>{message}</div>
            </div>
        )
    }
}

export default Authentication;