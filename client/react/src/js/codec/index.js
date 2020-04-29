const base64ToArray = (value) => {
    console.log(value);
    return Uint8Array.from(atob(value).split('').map((c) => {
        return c.charCodeAt(0);
    }));
};

const arrayToBase64 = (value) => {
//    const b = btoa(String.fromCharCode.apply(null, value));
    return btoa(new Uint8Array(value).reduce((s, byte) => s + String.fromCharCode(byte), ''));
};

export {base64ToArray, arrayToBase64};