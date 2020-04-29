import axios from 'axios';

const authenticationInit = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/authenticate/init`,data,{withCredentials: true})
};

const authenticationFinalize = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/authenticate/final`,data, {withCredentials: true});
};
export {authenticationInit,authenticationFinalize}