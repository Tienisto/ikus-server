const API_URL = "/api";

let me = null;
let handle401 = null;

export const CHANNEL_TYPE = {
    POST: 'POST',
    EVENT: 'EVENT',
    FAQ: 'FAQ'
};

async function makeRequest({ method, route, body, params, no401Callback }) {
    const url = new URL(API_URL+'/'+route, document.location);
    url.search = new URLSearchParams(params).toString();

    const response = await fetch(url, {
        method,
        body: JSON.stringify(body),
        headers: { "Content-Type": "application/json" },

    });

    if(!no401Callback && response.status === 401) {
        me = null;
        await handle401();
        return {};
    }

    if(response.status >= 400) {
        throw response.status;
    }

    return {
        status: response.status,
        data: JSON.parse((await response.text()) || "{}")
    };
}

export function initAPI({ handle401: h401 }) {
    handle401 = h401;
    console.log("API initialized.");
}

export function getUserInfo() {
    return me;
}

export async function initAccountInfo() {
    const response = await makeRequest({
        route: "me",
        method: "GET"
    });

    if (response.status === 200) {
        me = response.data;
        console.log("Already logged in.");
    } else {
        console.log("Not logged in.");
    }
}

export async function login({ name, password }) {
    const response = await makeRequest({
        route: "login",
        method: "POST",
        body: { name, password },
    });

    if(response.status === 200) {
        me = response.data;
        console.log('saved jwt');
    }

    return response;
}

export async function logout() {
    me = null;
    await makeRequest({
        route: 'logout',
        method: 'POST',
        no401Callback: true
    });
}

export async function getChannels({ type }) {
    return await makeRequest({
        route: 'channels',
        method: 'GET',
        params: { type }
    });
}