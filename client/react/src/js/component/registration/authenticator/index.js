import React from 'react';
import InputForm from "../../forms/InputForm";

class DeviceRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.onRegisterDeviceClick = this.onRegisterDeviceClick.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleInputChange(e) {
        e.preventDefault();
        this.props.handleUserInput(e.target.name, e.target.value);
    }

    onRegisterDeviceClick(e){
        e.preventDefault();
        this.props.register();
    }

    render(){
        const {message,registrationStatus,email} = this.props;

        return (

                <form className="measure" key="registration_authenticator_form">
                    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                        <legend className="f4 fw6 ph0 mh0">Register Authenticator</legend>
                        <div className="mt3" key="ppp">
                            <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                            <InputForm name="user_email" value={email} type="email" onChangeText={this.handleInputChange}/>
                        </div>
                    </fieldset>
                    <div>
                        <button name="register" value="Register" onClick={this.onRegisterDeviceClick} className="b ph3 pv2 input-reset ba  grow pointer f6 dib">Register</button>
                    </div>
                    <div className={`mv3 ${registrationStatus ? 'red':'green'}`}> {message}</div>
                    </form>


                )
    }
}

export default DeviceRegistration;