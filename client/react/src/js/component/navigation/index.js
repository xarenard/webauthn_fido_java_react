import React from 'react';
import {Link} from 'react-router-dom';
import { withRouter } from 'react-router';

const Navigation = () => {

        return (
            <ul className="topnav pl3">
                <li><Link to="/">Home</Link></li>
                <li ><Link to="/about">About</Link></li>
            </ul>
        );
};


export default withRouter(Navigation);