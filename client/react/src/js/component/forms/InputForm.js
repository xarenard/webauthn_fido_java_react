import React from 'react';

class InputForm extends React.Component{

    constructor(props) {
        super(props);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleInputChange(e){
        this.props.onChangeText(e);
    }
    render(){
        const {value,type,name} = this.props;
        return (
        <input className="pa2  w-100"
               type={type} name={name} id="email-address"  value={value} onChange={this.handleInputChange} />
        )
    }
}

export default InputForm;