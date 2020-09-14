const API_URL = "/api";

let me = null;
let handle401 = null;

export const CHANNEL_TYPE = {
    POST: 'POST',
    EVENT: 'EVENT',
    FAQ: 'FAQ'
};

async function makeRequest({ method, route, body, params, no401Callback, noException, useJSON = true }) {
    const url = new URL(API_URL+'/'+route, document.location);

    if (params) {
        // add query params
        for (let propName in params) {
            if (params[propName] === null || params[propName] === undefined) {
                delete params[propName];
            }
        }

        url.search = new URLSearchParams(params).toString();
    }

    // build body
    if (useJSON) {
        body = JSON.stringify(body); // as json
    } else {
        const original = body;
        body = new FormData(); // as form data, e.g. file upload
        for(const key in original) {
            body.append(key, original[key]);
        }
    }

    const response = await fetch(url, {
        method,
        body,
        headers: useJSON ? { "Content-Type": "application/json" } : {},
    });

    // handle 401
    if(!no401Callback && response.status === 401) {
        me = null;
        await handle401();
        return {};
    }

    // handle errors
    if(!noException && response.status >= 400) {
        throw response.status;
    }

    // return the result
    return JSON.parse((await response.text()) || "{}");
}

export function initAPI({ handle401: h401 }) {
    handle401 = h401;
    console.log("API initialized.");
}

export function isInitialized() {
    return !!handle401;
}

export function getUserInfo() {
    return me;
}

export async function initAccountInfo() {
    try {
        me = await makeRequest({
            route: "me",
            method: "GET",
            no401Callback: true
        });
        console.log("Already logged in.");
    } catch (e) {
        console.log("Not logged in.");
    }
}

export async function login({ name, password }) {
    const response = await makeRequest({
        route: "login",
        method: "POST",
        body: { name, password },
    });

    me = response;
    console.log('saved jwt');

    return response;
}

export async function logout() {
    me = null;
    await makeRequest({
        route: 'logout',
        method: 'POST',
        no401Callback: true,
        noException: true
    });
}

export async function getVersion() {
    return await makeRequest({
        route: 'version',
        method: 'GET'
    });
}

export async function getStatus() {
    return await makeRequest({
        route: 'status',
        method: 'GET'
    });
}

// admin

export async function getUsers() {
    return await makeRequest({
        route: 'users',
        method: 'GET'
    });
}

export async function createUser({ name, password }) {
    return await makeRequest({
        route: 'users',
        method: 'POST',
        body: { name, password }
    });
}

export async function updateUserName({ id, name }) {
    return await makeRequest({
        route: 'users/name',
        method: 'PUT',
        body: { id, name }
    });
}

export async function updateUserPassword({ id, password }) {
    return await makeRequest({
        route: 'users/password',
        method: 'PUT',
        body: { id, password }
    });
}

export async function deleteUser({ id }) {
    return await makeRequest({
        route: 'users',
        method: 'DELETE',
        body: { id }
    });
}

// user

export async function getDashboard() {
    return await makeRequest({
        route: 'dashboard',
        method: 'GET'
    });
}

export async function getPosts({ channelId }) {
    return await makeRequest({
        route: 'posts',
        method: 'GET',
        params: { channelId }
    });
}

export async function createPost({ channelId, title, content }) {
    return await makeRequest({
        route: 'posts',
        method: 'POST',
        body: { channelId, title, content }
    });
}

export async function updatePost({ id, channelId, title, content }) {
    return await makeRequest({
        route: 'posts',
        method: 'PUT',
        body: { id, channelId, title, content }
    });
}

export async function deletePost({ id }) {
    return await makeRequest({
        route: 'posts',
        method: 'DELETE',
        body: { id }
    });
}

export async function uploadPostFile({ file }) {
    return await makeRequest({
        route: 'posts/upload',
        method: 'POST',
        body: { file },
        useJSON: false
    });
}

export async function deletePostFile({ fileId }) {
    return await makeRequest({
        route: 'posts/file',
        method: 'DELETE',
        body: { id: fileId }
    });
}

export function getFileUrl({ name }) {
    return API_URL + '/public/file/' + name;
}

export async function getChannels({type}) {
    return await makeRequest({
        route: 'channels',
        method: 'GET',
        params: { type }
    });
}

export async function createChannel({ type, name }) {
    return await makeRequest({
        route: 'channels',
        method: 'POST',
        params: { type },
        body: { name }
    });
}

export async function renameChannel({ id, name }) {
    return await makeRequest({
        route: 'channels',
        method: 'PUT',
        body: { id, name }
    });
}

export async function deleteChannel({ id }) {
    return await makeRequest({
        route: 'channels',
        method: 'DELETE',
        body: { id }
    });
}

export async function getLogs({ limit }) {
    return await makeRequest({
        route: 'logs',
        method: 'GET',
        params: { limit }
    });
}