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
                <div className="pt1"><Link to="/user" onClick={this.reset}>1. Register user</Link></div>
                <div className="pt1"><Link to="/device" onClick={this.reset}>2. Register Device</Link></div>
                <div className="pt1"><Link to="/authentication" onClick={this.reset}>3. Authenticate</Link></div>
            </div>
        )
    }
}

export default Menu;

