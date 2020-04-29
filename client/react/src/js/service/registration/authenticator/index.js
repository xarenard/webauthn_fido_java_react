import axios from 'axios';


const initRegistration = (data) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/registration/init`,data,{withCredentials: true});
};

const finalizeRegistration = (data,userId) => {
    return axios.post(`${WEBAUTHN_SERVER_URL}:${WEBAUTHN_SERVER_PORT}/registration/final/${userId}`,data,{
        withCredentials: true,
        headers: {
            'Content-Type': 'application/json',
        }});
};

export {initRegistration, finalizeRegistration};