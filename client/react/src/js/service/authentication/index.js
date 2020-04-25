import axios from 'axios';

const authenticationInit = () => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/authenticate/init`,{withCredentials: true})
};

const authenticationFinalize = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/authenticate/final`,data);
};
export {authenticationInit,authenticationFinalize}