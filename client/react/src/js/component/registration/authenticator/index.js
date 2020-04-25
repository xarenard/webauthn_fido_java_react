import React from 'react';

class DeviceRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.onRegisterDeviceClick = this.onRegisterDeviceClick.bind(this);
    }


    onRegisterDeviceClick(e){
        e.preventDefault();
        this.props.register();
    }


    render(){
        const {message,registrationStatus} = this.props;

        return (
            <div>
                <form className="measure">
                    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
                        <legend className="f4 fw6 ph0 mh0">Register Device</legend>
                        <div className="mt3">
                            <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
                            <input className="pa2 input-reset ba bg-transparent hover-bg-black hover-white w-100"
                                   type="email" name="email-address" id="email-address" />
                        </div>

                    </fieldset>
                    <div>
                     <button name="register" value="Register" onClick={this.onRegisterDeviceClick} className="b ph3 pv2 input-reset ba  grow pointer f6 dib">Register</button>
                    </div>
                    </form>
                <div className={`mv3 ${registrationStatus ? 'red':'green'}`}> {message}</div>
            </div>
                )
    }
}

export default DeviceRegistration;