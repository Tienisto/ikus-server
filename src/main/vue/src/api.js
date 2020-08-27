const API_URL = "/api";

let loggedIn = false;
let handle401 = null;

export const CHANNEL_TYPE = {
    POST: 'POST',
    EVENT: 'EVENT',
    FAQ: 'FAQ'
};

async function makeRequest({ method, route, body, params }) {
    const url = new URL(API_URL+'/'+route, document.location);
    url.search = new URLSearchParams(params).toString();

    const response = await fetch(url, {
        method,
        body: JSON.stringify(body),
        headers: { "Content-Type": "application/json" },

    });

    if(response.status === 401) {
        logout();
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

export function isLoggedIn() {
    return loggedIn;
}

export async function login({ name, password }) {
    const response = await makeRequest({
        route: "login",
        method: "POST",
        body: { name, password },
    });

    if(response.status === 200) {
        loggedIn = true;
        console.log('saved jwt');
    }

    return response;
}

export function logout() {
    loggedIn = false;
}

export async function getChannels({ type }) {
    return await makeRequest({
        route: 'channels',
        method: 'GET',
        params: { type }
    });
}