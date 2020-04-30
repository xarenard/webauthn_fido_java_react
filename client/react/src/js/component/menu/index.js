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
import {Link} from "react-router-dom";

class Menu extends React.Component {

    constructor(props) {
        super(props);
        this.reset = this.reset.bind(this);
    }

    reset(){
        this.props.init();
    }

    render(){
        return(
            <div id="menu" className="pl3">
                <div className="pt1 mv2"><Link to="/user" onClick={this.reset}>Register User</Link></div>
                <div className="pt1 mv2"><Link to="/device" onClick={this.reset}>Register Authenticator</Link></div>
                <div className="pt1"><Link to="/authentication" onClick={this.reset}>Authenticate</Link></div>
            </div>
        )
    }
}

export default Menu;

