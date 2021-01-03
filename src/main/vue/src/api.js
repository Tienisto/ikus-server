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
    if(!no401Callback && response.status === 401 && handle401) {
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
            route: "account",
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

export async function updateMyPassword({ oldPassword, newPassword }) {
    return await makeRequest({
        route: 'account/password',
        method: 'POST',
        body: { oldPassword, newPassword }
    });
}

export async function getDashboard() {
    return await makeRequest({
        route: 'dashboard',
        method: 'GET'
    });
}

export async function getAllNews() {
    return await makeRequest({
        route: 'posts/news/all',
        method: 'GET'
    });
}

export async function getNews({ channelId }) {
    return await makeRequest({
        route: 'posts/news',
        method: 'GET',
        params: { channelId }
    });
}

export async function getPostsGrouped({ type }) {
    return await makeRequest({
        route: 'posts/grouped-order-position',
        method: 'GET',
        params: { type }
    });
}

export async function searchPosts({ query }) {
    return await makeRequest({
        route: 'posts/search',
        method: 'GET',
        params: { query }
    });
}

export async function createPost({ channelId, title, content, files }) {
    return await makeRequest({
        route: 'posts',
        method: 'POST',
        body: { channelId, title, content, files }
    });
}

export async function updatePost({ id, channelId, title, content, files }) {
    return await makeRequest({
        route: 'posts',
        method: 'PUT',
        body: { id, channelId, title, content, files }
    });
}

export async function togglePinPost({ postId }) {
    return await makeRequest({
        route: 'posts/toggle-pin',
        method: 'POST',
        params: { postId }
    });
}

export async function moveUpPost({ id }) {
    return await makeRequest({
        route: 'posts/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownPost({ id }) {
    return await makeRequest({
        route: 'posts/move-down',
        method: 'POST',
        body: { id }
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

export function getFileUrl(name, { download } = {}) {
    if (download)
        return API_URL + '/public/file/' + name + '?download=true';
    else
        return API_URL + '/public/file/' + name;
}

export async function getAllEvents() {
    return await makeRequest({
        route: 'events/all',
        method: 'GET'
    });
}

export async function getEventsByChannel({ channelId }) {
    return await makeRequest({
        route: 'events',
        method: 'GET',
        params: { channelId }
    });
}

export async function createEvent({ channelId, name, info, place, coords, startTime, endTime }) {
    return await makeRequest({
        route: 'events',
        method: 'POST',
        body: { channelId, name, info, place, coords, startTime, endTime }
    });
}

export async function updateEvent({ id, channelId, name, info, place, coords, startTime, endTime }) {
    return await makeRequest({
        route: 'events',
        method: 'PUT',
        body: { id, channelId, name, info, place, coords, startTime, endTime }
    });
}

export async function deleteEvent({ id }) {
    return await makeRequest({
        route: 'events',
        method: 'DELETE',
        body: { id }
    });
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

export async function moveUpChannel({ id }) {
    return await makeRequest({
        route: 'channels/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownChannel({ id }) {
    return await makeRequest({
        route: 'channels/move-down',
        method: 'POST',
        body: { id }
    });
}

export async function deleteChannel({ id }) {
    return await makeRequest({
        route: 'channels',
        method: 'DELETE',
        body: { id }
    });
}

export async function getLinksGrouped() {
    return await makeRequest({
        route: 'links',
        method: 'GET'
    });
}

export async function searchLinks({ query }) {
    return await makeRequest({
        route: 'links/search',
        method: 'GET',
        params: { query }
    });
}

export async function createLink({ channelId, url, info }) {
    return await makeRequest({
        route: 'links',
        method: 'POST',
        body: { channelId, url, info }
    });
}

export async function updateLink({ id, channelId, url, info }) {
    return await makeRequest({
        route: 'links',
        method: 'PUT',
        body: { id, channelId, url, info }
    });
}

export async function moveUpLink({ id }) {
    return await makeRequest({
        route: 'links/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownLink({ id }) {
    return await makeRequest({
        route: 'links/move-down',
        method: 'POST',
        body: { id }
    });
}

export async function deleteLink({ id }) {
    return await makeRequest({
        route: 'links',
        method: 'DELETE',
        body: { id }
    });
}

export async function getHandbookBookmarks() {
    return await makeRequest({
        route: 'handbook',
        method: 'GET'
    });
}

export async function uploadHandbook({ file, locale }) {
    return await makeRequest({
        route: 'handbook/upload',
        method: 'PUT',
        body: { file },
        params: { locale },
        useJSON: false
    });
}

export async function updateBookmarks({ bookmarks, locale }) {
    return await makeRequest({
        route: 'handbook/bookmarks',
        method: 'PUT',
        body: { bookmarks, locale }
    });
}

export async function getAudio() {
    return await makeRequest({
        route: 'audio',
        method: 'GET'
    });
}

export async function createAudio({ name }) {
    return await makeRequest({
        route: 'audio',
        method: 'POST',
        body: { name }
    });
}

export async function updateAudio({ id, name }) {
    return await makeRequest({
        route: 'audio',
        method: 'PUT',
        body: { id, name }
    });
}

export async function moveUpAudio({ id }) {
    return await makeRequest({
        route: 'audio/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownAudio({ id }) {
    return await makeRequest({
        route: 'audio/move-down',
        method: 'POST',
        body: { id }
    });
}

export async function deleteAudio({ id }) {
    return await makeRequest({
        route: 'audio',
        method: 'DELETE',
        body: { id }
    });
}

export async function setAudioImage({ audioId, file, locale }) {
    return await makeRequest({
        route: 'audio/image',
        method: 'PUT',
        body: { file },
        params: { audioId, locale },
        useJSON: false
    });
}

export async function deleteAudioImage({ audioId }) {
    return await makeRequest({
        route: 'audio/image',
        method: 'DELETE',
        params: { audioId }
    });
}

export async function createAudioFile({ audioId, name, text }) {
    return await makeRequest({
        route: 'audio/file',
        method: 'POST',
        body: { audioId, name, text }
    });
}

export async function uploadAudioFile({ fileId, token, file, locale }) {
    return await makeRequest({
        route: 'audio/file/upload',
        method: 'PUT',
        body: { file },
        params: { fileId, token, locale },
        useJSON: false
    });
}

export async function updateAudioFile({ id, audioId, name, text }) {
    return await makeRequest({
        route: 'audio/file',
        method: 'PUT',
        body: { id, audioId, name, text }
    });
}

export async function moveUpAudioFile({ id }) {
    return await makeRequest({
        route: 'audio/file/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownAudioFile({ id }) {
    return await makeRequest({
        route: 'audio/file/move-down',
        method: 'POST',
        body: { id }
    });
}

export async function deleteAudioFile({ id }) {
    return await makeRequest({
        route: 'audio/file',
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

export async function getSysLogs() {
    return await makeRequest({
        route: 'sys-logs',
        method: 'GET'
    });
}

export async function getContacts() {
    return await makeRequest({
        route: 'contacts',
        method: 'GET'
    });
}

export async function createContact({ name, email, phoneNumber, place, openingHours, links }) {
    return await makeRequest({
        route: 'contacts',
        method: 'POST',
        body: { name, email, phoneNumber, place, openingHours, links }
    });
}

export async function updateContact({ id, name, email, phoneNumber, place, openingHours, links }) {
    return await makeRequest({
        route: 'contacts',
        method: 'PUT',
        body: { id, name, email, phoneNumber, place, openingHours, links }
    });
}

export async function moveUpContact({ id }) {
    return await makeRequest({
        route: 'contacts/move-up',
        method: 'POST',
        body: { id }
    });
}

export async function moveDownContact({ id }) {
    return await makeRequest({
        route: 'contacts/move-down',
        method: 'POST',
        body: { id }
    });
}

export async function setContactFile({ contactId, file }) {
    return await makeRequest({
        route: 'contacts/file',
        method: 'PUT',
        body: { file },
        params: { contactId },
        useJSON: false
    });
}

export async function deleteContactFile({ contactId }) {
    return await makeRequest({
        route: 'contacts/file',
        method: 'DELETE',
        params: { contactId }
    });
}

export async function deleteContact({ id }) {
    return await makeRequest({
        route: 'contacts',
        method: 'DELETE',
        body: { id }
    });
}

export async function getFeatures() {
    return await makeRequest({
        route: 'features',
        method: 'GET'
    });
}

export async function createFeature({ name, icon, postId, linkId }) {
    return await makeRequest({
        route: 'features',
        method: 'POST',
        body: { name, icon, postId, linkId }
    });
}

export async function updateFeature({ id, name, icon, postId, linkId }) {
    return await makeRequest({
        route: 'features',
        method: 'PUT',
        body: { id, name, icon, postId, linkId }
    });
}

export async function toggleFeatureFavorite({ featureId }) {
    return await makeRequest({
        route: 'features/toggle-favorite',
        method: 'POST',
        params: { featureId }
    });
}

export async function moveUpFeature({ featureId }) {
    return await makeRequest({
        route: 'features/move-up',
        method: 'POST',
        params: { featureId }
    });
}

export async function moveDownFeature({ featureId }) {
    return await makeRequest({
        route: 'features/move-down',
        method: 'POST',
        params: { featureId }
    });
}

export async function deleteFeature({ featureId }) {
    return await makeRequest({
        route: 'features',
        method: 'DELETE',
        params: { featureId }
    });
}

// analytics

export async function getStats({ type }) {
    return await makeRequest({
        route: 'analytics/app-starts',
        method: 'GET',
        params: { type }
    });
}

export async function getStatsCurr() {
    return await makeRequest({
        route: 'analytics/app-starts/curr',
        method: 'GET'
    });
}