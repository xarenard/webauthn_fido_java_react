/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const base64ToArray = (value) => {
    return Uint8Array.from(atob(value).split('').map((c) => {
        return c.charCodeAt(0);
    }));
};

const arrayToBase64 = (value) => {
//    const b = btoa(String.fromCharCode.apply(null, value));
    return btoa(new Uint8Array(value).reduce((s, byte) => s + String.fromCharCode(byte), ''));
};

export {base64ToArray, arrayToBase64};