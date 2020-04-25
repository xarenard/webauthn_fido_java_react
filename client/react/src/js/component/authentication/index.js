import React from 'react';

class Authentication extends React.Component {

    constructor(props) {
        super(props);
        this.authenticateClick = this.authenticateClick.bind(this);
    }

    authenticateClick(e) {
        e.preventDefault();
        this.props.authenticate();
    }

    render(){
        const {message, authenticationStatus} = this.props;
        return (
            <div>
                <form className="measure">
                    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                        <legend className="f4 fw6 ph0 mh0">Authentication</legend>
                        <div className="mt3">
                            <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                            <input className="pa2 input-reset ba bg-transparent hover-bg-black hover-white w-100"
                                   type="email" name="email-address"  id="email-address" />
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