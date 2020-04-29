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

