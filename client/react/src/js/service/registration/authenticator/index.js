import axios from 'axios';


const initRegistration = () => {
    return axios.get(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/registration/init`,{withCredentials: true});
};

const finalizeRegistration = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/registration/final`,data,{
        withCredentials: true,
        headers: {
            'Content-Type': 'application/json',
        }});
};

export {initRegistration, finalizeRegistration};