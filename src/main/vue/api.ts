import type {
  AllChannelDto,
  AppStartDto, AudioDto, ChannelDto, ContactDto, CoordsDto, CurrentAppStarts,
  DashboardDto,
  EventDto, FeatureDto, HandbookBookmarkDto, LinkDto, LinkGroupDto, LogDto,
  IntervalType,
  MeDto, MultiLocaleString,
  PostDto,
  PostGroupDto,
  ServerStatus, SysLogsDto, UserDto,
  VersionDto, CreateLinkDto, HandbookBookmarkResponseDto, CreatePostDto, CreateChannelDto, CreateEventDto
} from "@/models";
import {RegistrationField} from "@/models";

const API_URL = "/api";

let me: MeDto | null = null;
let handle401: Function | null = null;

export const CHANNEL_TYPE = {
  POST: 'POST',
  EVENT: 'EVENT',
  FAQ: 'FAQ'
};

interface RequestParams {
  method: string;
  route: string;
  body?: any;
  params?: any;
  no401Callback?: boolean;
  noException?: boolean;
  useJSON?: boolean;
}

async function makeRequest({method, route, body, params, no401Callback, noException, useJSON = true}: RequestParams) {
  const url = new URL(API_URL + '/' + route, document.location.href);

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
    for (const key in original) {
      body.append(key, original[key]);
    }
  }

  const response = await fetch(url, {
    method,
    body,
    headers: useJSON ? {"Content-Type": "application/json"} : {},
  });

  // handle 401
  if (!no401Callback && response.status === 401 && handle401) {
    me = null;
    await handle401();
    return {};
  }

  // handle errors
  if (!noException && response.status >= 400) {
    throw response.status;
  }

  // return the result
  return JSON.parse((await response.text()) || "{}");
}

export function initAPI(h401: Function) {
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

export async function login({name, password}: {name: string, password: string}): Promise<MeDto> {
  const response = await makeRequest({
    route: "login",
    method: "POST",
    body: {name, password},
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

export async function getVersion(): Promise<VersionDto> {
  return await makeRequest({
    route: 'version',
    method: 'GET'
  });
}

export async function getStatus(): Promise<ServerStatus> {
  return await makeRequest({
    route: 'status',
    method: 'GET'
  });
}

// admin

export async function getUsers(): Promise<UserDto[]> {
  return await makeRequest({
    route: 'users',
    method: 'GET'
  });
}

export async function createUser({name, password}: { name: string, password: string }) {
  return await makeRequest({
    route: 'users',
    method: 'POST',
    body: {name, password}
  });
}

export async function updateUserName({id, name}: { id: number, name: string }) {
  return await makeRequest({
    route: 'users/name',
    method: 'PUT',
    body: {id, name}
  });
}

export async function updateUserPassword({id, password}: { id: number, password: string }) {
  return await makeRequest({
    route: 'users/password',
    method: 'PUT',
    body: {id, password}
  });
}

export async function deleteUser({id}: { id: number }) {
  return await makeRequest({
    route: 'users',
    method: 'DELETE',
    body: {id}
  });
}

// user

export async function updateMyPassword({oldPassword, newPassword}: { oldPassword: string, newPassword: string }) {
  return await makeRequest({
    route: 'account/password',
    method: 'POST',
    body: {oldPassword, newPassword}
  });
}

export async function getDashboard(): Promise<DashboardDto> {
  return await makeRequest({
    route: 'dashboard',
    method: 'GET'
  });
}

export async function getAllNews(): Promise<PostDto[]> {
  return await makeRequest({
    route: 'posts/news/all',
    method: 'GET'
  });
}

export async function getArchivedNews(): Promise<PostDto[]> {
  return await makeRequest({
    route: 'posts/news/archived',
    method: 'GET'
  });
}

export async function getNews({channelId}: {channelId: number}): Promise<PostDto[]> {
  return await makeRequest({
    route: 'posts/news',
    method: 'GET',
    params: {channelId}
  });
}

export async function getFaq(): Promise<PostGroupDto[]> {
  return await makeRequest({
    route: 'posts/faq',
    method: 'GET',
  });
}

export async function searchPosts({query}: {query: string}): Promise<PostDto[]> {
  return await makeRequest({
    route: 'posts/search',
    method: 'GET',
    params: {query}
  });
}

export async function createPost({channelId, title, content, files, date}: CreatePostDto) {
  return await makeRequest({
    route: 'posts',
    method: 'POST',
    body: {channelId, title, content, files, date}
  });
}

export async function updatePost({id, channelId, title, content, files, date}: {id: number} & CreatePostDto) {
  return await makeRequest({
    route: 'posts',
    method: 'PUT',
    body: {id, channelId, title, content, files, date}
  });
}

export async function moveUpPost({id}: {id: number}) {
  return await makeRequest({
    route: 'posts/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownPost({id}: {id: number}) {
  return await makeRequest({
    route: 'posts/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function toggleArchive({postId}: {postId: number}) {
  return await makeRequest({
    route: 'posts/toggle-archive',
    method: 'POST',
    params: {postId}
  });
}

export async function deletePost({id}: {id: number}) {
  return await makeRequest({
    route: 'posts',
    method: 'DELETE',
    body: {id}
  });
}

export async function uploadPostFile({file}: {file: File}) {
  return await makeRequest({
    route: 'posts/upload',
    method: 'POST',
    body: {file},
    useJSON: false
  });
}

export function getFileUrl(name: string) {
    return API_URL + '/public/file/' + name;
}

export async function getAllEvents(): Promise<EventDto[]> {
  return await makeRequest({
    route: 'events/all',
    method: 'GET'
  });
}

export async function getEventsByChannel({channelId}: {channelId: number}): Promise<EventDto[]> {
  return await makeRequest({
    route: 'events',
    method: 'GET',
    params: {channelId}
  });
}

export async function getEventsById({eventId}: {eventId: number}): Promise<EventDto> {
  return await makeRequest({
    route: `events/${eventId}`,
    method: 'GET'
  });
}

export async function createEvent({channelId, name, info, place, coords, startTime, endTime}: CreateEventDto) {
  return await makeRequest({
    route: 'events',
    method: 'POST',
    body: {channelId, name, info, place, coords, startTime, endTime}
  });
}

export async function updateEvent({id, channelId, name, info, place, coords, startTime, endTime}: CreateEventDto & {id: number}) {
  return await makeRequest({
    route: 'events',
    method: 'PUT',
    body: {id, channelId, name, info, place, coords, startTime, endTime}
  });
}

export async function deleteEvent({id}: {id: number}) {
  return await makeRequest({
    route: 'events',
    method: 'DELETE',
    body: {id}
  });
}

export async function repeatEvent({eventId, interval, endDate}: {eventId: number, interval: IntervalType, endDate: string}) {
  return await makeRequest({
    route: 'events/repeat',
    method: 'POST',
    body: {eventId, interval, endDate}
  });
}

export async function updateEventRegistration({eventId, fields, slots, slotsWaiting, open}: {eventId: number, fields: RegistrationField[], slots: number, slotsWaiting: number, open: boolean}) {
  return await makeRequest({
    route: 'events/registrations',
    method: 'POST',
    body: {id: eventId, fields, slots, slotsWaiting, open}
  });
}

export async function kickEventRegistration({eventId, token}: {eventId: number, token: string}) {
  return await makeRequest({
    route: 'events/registrations/kick',
    method: 'POST',
    body: {eventId, token}
  });
}

export async function getChannels({type}: {type?: 'NEWS' | 'CALENDAR'}): Promise<AllChannelDto | ChannelDto[]> {
  return await makeRequest({
    route: 'channels',
    method: 'GET',
    params: {type}
  });
}

export async function createChannel({type, name}: CreateChannelDto & {type: string}) {
  return await makeRequest({
    route: 'channels',
    method: 'POST',
    params: {type},
    body: {name}
  });
}

export async function renameChannel({id, name}: {id: number, name: MultiLocaleString}) {
  return await makeRequest({
    route: 'channels',
    method: 'PUT',
    body: {id, name}
  });
}

export async function moveUpChannel({id}: {id: number}) {
  return await makeRequest({
    route: 'channels/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownChannel({id}: {id: number}) {
  return await makeRequest({
    route: 'channels/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function deleteChannel({id}: {id: number}) {
  return await makeRequest({
    route: 'channels',
    method: 'DELETE',
    body: {id}
  });
}

export async function getLinksGrouped(): Promise<LinkGroupDto[]> {
  return await makeRequest({
    route: 'links',
    method: 'GET'
  });
}

export async function searchLinks({query}: {query: string}): Promise<LinkDto[]> {
  return await makeRequest({
    route: 'links/search',
    method: 'GET',
    params: {query}
  });
}

export async function createLink({channelId, url, info}: CreateLinkDto) {
  return await makeRequest({
    route: 'links',
    method: 'POST',
    body: {channelId, url, info}
  });
}

export async function updateLink({id, channelId, url, info}: {id: number, channelId: number, url: MultiLocaleString, info: MultiLocaleString}) {
  return await makeRequest({
    route: 'links',
    method: 'PUT',
    body: {id, channelId, url, info}
  });
}

export async function moveUpLink({id}: {id: number}) {
  return await makeRequest({
    route: 'links/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownLink({id}: {id: number}) {
  return await makeRequest({
    route: 'links/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function deleteLink({id}: {id: number}) {
  return await makeRequest({
    route: 'links',
    method: 'DELETE',
    body: {id}
  });
}

export async function getHandbookBookmarks(): Promise<HandbookBookmarkResponseDto> {
  return await makeRequest({
    route: 'handbook',
    method: 'GET'
  });
}

export async function uploadHandbook({file, locale}: {file: File, locale: string}) {
  return await makeRequest({
    route: 'handbook/upload',
    method: 'PUT',
    body: {file},
    params: {locale},
    useJSON: false
  });
}

export async function updateBookmarks({bookmarks, locale}: {bookmarks: HandbookBookmarkDto[], locale: string}) {
  return await makeRequest({
    route: 'handbook/bookmarks',
    method: 'PUT',
    body: {bookmarks, locale}
  });
}

export async function getAudio(): Promise<AudioDto[]> {
  return await makeRequest({
    route: 'audio',
    method: 'GET'
  });
}

export async function createAudio({name}: {name: MultiLocaleString}) {
  return await makeRequest({
    route: 'audio',
    method: 'POST',
    body: {name}
  });
}

export async function updateAudio({id, name}: {id: number, name: MultiLocaleString}) {
  return await makeRequest({
    route: 'audio',
    method: 'PUT',
    body: {id, name}
  });
}

export async function moveUpAudio({id}: {id: number}) {
  return await makeRequest({
    route: 'audio/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownAudio({id}: {id: number}) {
  return await makeRequest({
    route: 'audio/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function deleteAudio({id}: {id: number}) {
  return await makeRequest({
    route: 'audio',
    method: 'DELETE',
    body: {id}
  });
}

export async function setAudioImage({audioId, file, locale}: {audioId: number, file: File, locale: string}) {
  return await makeRequest({
    route: 'audio/image',
    method: 'PUT',
    body: {file},
    params: {audioId, locale},
    useJSON: false
  });
}

export async function deleteAudioImage({audioId}: {audioId: number}) {
  return await makeRequest({
    route: 'audio/image',
    method: 'DELETE',
    params: {audioId}
  });
}

export async function createAudioFile({audioId, name, text}: {audioId: number, name: MultiLocaleString, text: MultiLocaleString}) {
  return await makeRequest({
    route: 'audio/file',
    method: 'POST',
    body: {audioId, name, text}
  });
}

export async function uploadAudioFileAudio({fileId, token, file, locale}: {fileId: number, token: string, file: File, locale: string}) {
  return await makeRequest({
    route: 'audio/file/upload-audio',
    method: 'PUT',
    body: {file},
    params: {fileId, token, locale},
    useJSON: false
  });
}

export async function uploadAudioFileImage({fileId, file, locale}: {fileId: number, file: File, locale: string}) {
  return await makeRequest({
    route: 'audio/file/upload-image',
    method: 'PUT',
    body: {file},
    params: {fileId, locale},
    useJSON: false
  });
}

export async function deleteAudioFileImage({fileId}: {fileId: number}) {
  return await makeRequest({
    route: 'audio/file/image',
    method: 'DELETE',
    params: {fileId}
  });
}

export async function updateAudioFile({id, audioId, name, text}: {id: number, audioId: number, name: MultiLocaleString, text: MultiLocaleString}) {
  return await makeRequest({
    route: 'audio/file',
    method: 'PUT',
    body: {id, audioId, name, text}
  });
}

export async function moveUpAudioFile({id}: {id: number}) {
  return await makeRequest({
    route: 'audio/file/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownAudioFile({id}: {id: number}) {
  return await makeRequest({
    route: 'audio/file/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function deleteAudioFile({id}: {id: number}) {
  return await makeRequest({
    route: 'audio/file',
    method: 'DELETE',
    body: {id}
  });
}

export async function getLogs({limit}: {limit: number}): Promise<LogDto[]> {
  return await makeRequest({
    route: 'logs',
    method: 'GET',
    params: {limit}
  });
}

export async function getSysLogs(): Promise<SysLogsDto> {
  return await makeRequest({
    route: 'sys-logs',
    method: 'GET'
  });
}

export async function getContacts(): Promise<ContactDto[]> {
  return await makeRequest({
    route: 'contacts',
    method: 'GET'
  });
}

export async function createContact({name, email, phoneNumber, place, openingHours, links}: {name: MultiLocaleString, email?: string, phoneNumber?: string, place?: string, openingHours?: MultiLocaleString, links: string[]}) {
  return await makeRequest({
    route: 'contacts',
    method: 'POST',
    body: {name, email, phoneNumber, place, openingHours, links}
  });
}

export async function updateContact({id, name, email, phoneNumber, place, openingHours, links}: {id: number, name: MultiLocaleString, email?: string, phoneNumber?: string, place?: string, openingHours?: MultiLocaleString, links: string[]}) {
  return await makeRequest({
    route: 'contacts',
    method: 'PUT',
    body: {id, name, email, phoneNumber, place, openingHours, links}
  });
}

export async function moveUpContact({id}: {id: number}) {
  return await makeRequest({
    route: 'contacts/move-up',
    method: 'POST',
    body: {id}
  });
}

export async function moveDownContact({id}: {id: number}) {
  return await makeRequest({
    route: 'contacts/move-down',
    method: 'POST',
    body: {id}
  });
}

export async function setContactFile({contactId, file}: {contactId: number, file: File}) {
  return await makeRequest({
    route: 'contacts/file',
    method: 'PUT',
    body: {file},
    params: {contactId},
    useJSON: false
  });
}

export async function deleteContactFile({contactId}: {contactId: number}) {
  return await makeRequest({
    route: 'contacts/file',
    method: 'DELETE',
    params: {contactId}
  });
}

export async function deleteContact({id}: {id: number}) {
  return await makeRequest({
    route: 'contacts',
    method: 'DELETE',
    body: {id}
  });
}

export async function getFeatures(): Promise<FeatureDto[]> {
  return await makeRequest({
    route: 'features',
    method: 'GET'
  });
}

export async function createFeature({name, icon, postId, linkId}: {name: MultiLocaleString, icon: string, postId: number | null, linkId: number | null}) {
  return await makeRequest({
    route: 'features',
    method: 'POST',
    body: {name, icon, postId, linkId}
  });
}

export async function updateFeature({id, name, icon, postId, linkId}: {id: number, name: MultiLocaleString, icon: string, postId: number | null, linkId: number | null}) {
  return await makeRequest({
    route: 'features',
    method: 'PUT',
    body: {id, name, icon, postId, linkId}
  });
}

export async function toggleFeatureFavorite({featureId}: {featureId: number}) {
  return await makeRequest({
    route: 'features/toggle-favorite',
    method: 'POST',
    params: {featureId}
  });
}

export async function moveUpFeature({featureId}: {featureId: number}) {
  return await makeRequest({
    route: 'features/move-up',
    method: 'POST',
    params: {featureId}
  });
}

export async function moveDownFeature({featureId}: {featureId: number}) {
  return await makeRequest({
    route: 'features/move-down',
    method: 'POST',
    params: {featureId}
  });
}

export async function deleteFeature({featureId}: {featureId: number}) {
  return await makeRequest({
    route: 'features',
    method: 'DELETE',
    params: {featureId}
  });
}

// analytics

export enum StatsType {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY'
}

export async function getStats({type}: {type: StatsType}): Promise<AppStartDto[]> {
  return await makeRequest({
    route: 'analytics/app-starts',
    method: 'GET',
    params: {type}
  });
}

export async function getStatsCurr(): Promise<CurrentAppStarts> {
  return await makeRequest({
    route: 'analytics/app-starts/curr',
    method: 'GET'
  });
}
