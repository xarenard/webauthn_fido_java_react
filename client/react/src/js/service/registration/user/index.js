import axios from 'axios';


const register = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/registration/user`,data,{withCredentials: false});
};

export {register as registerFidoUser};