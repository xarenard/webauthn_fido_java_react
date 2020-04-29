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