export interface IdItem {
  id: number;
}

export interface MultiLocaleString {
  en: string;
  de: string;
}

export interface Token {
  token: string;
}

export interface VersionDto {
  version: string;
}

export interface EnvDto {
  key: string;
  description: string;
  default: string;
  value: string;
}

export enum JwtStatus {
  OK = "OK",
  DEFAULT = "DEFAULT",
  TOO_SHORT = "TOO_SHORT"
}

export interface ServerStatus {
  version: string;
  date: string;
  runTime: number;
  database: boolean;
  storageRead: boolean;
  storageWrite: boolean;
  adminPassword: boolean;
  jwtWebsite: JwtStatus;
  jwtApp: JwtStatus;
  env?: EnvDto[];
}

export interface SysLogsDto {
  logs: string;
}

export interface MeDto {
  name: string;
  admin: boolean;
}

export interface DashboardDto {
  logs: LogDto[];
  posts: PostDto[];
  events: EventDto[];
}

export enum LogType {
  CREATE_USER = 'CREATE_USER',
  UPDATE_USER_NAME = 'UPDATE_USER_NAME',
  UPDATE_USER_PASSWORD = 'UPDATE_USER_PASSWORD',
  DELETE_USER = 'DELETE_USER',
  CREATE_CHANNEL = 'CREATE_CHANNEL',
  UPDATE_CHANNEL = 'UPDATE_CHANNEL',
  DELETE_CHANNEL = 'DELETE_CHANNEL',
  CREATE_POST = 'CREATE_POST',
  UPDATE_POST = 'UPDATE_POST',
  DELETE_POST = 'DELETE_POST',
  PIN_POST = 'PIN_POST',
  UNPIN_POST = 'UNPIN_POST',
  ARCHIVE_POST = 'ARCHIVE_POST',
  UNARCHIVE_POST = 'UNARCHIVE_POST',
  CREATE_EVENT = 'CREATE_EVENT',
  UPDATE_EVENT = 'UPDATE_EVENT',
  DELETE_EVENT = 'DELETE_EVENT',
  UPDATE_EVENT_OPEN_REGISTRATION = 'UPDATE_EVENT_OPEN_REGISTRATION',
  UPDATE_EVENT_CLOSE_REGISTRATION = 'UPDATE_EVENT_CLOSE_REGISTRATION',
  CREATE_LINK = 'CREATE_LINK',
  UPDATE_LINK = 'UPDATE_LINK',
  DELETE_LINK = 'DELETE_LINK',
  UPDATE_HANDBOOK = 'UPDATE_HANDBOOK',
  UPDATE_HANDBOOK_BOOKMARKS = 'UPDATE_HANDBOOK_BOOKMARKS',
  CREATE_AUDIO = 'CREATE_AUDIO',
  UPDATE_AUDIO = 'UPDATE_AUDIO',
  DELETE_AUDIO = 'DELETE_AUDIO',
  CREATE_AUDIO_FILE = 'CREATE_AUDIO_FILE',
  UPDATE_AUDIO_FILE = 'UPDATE_AUDIO_FILE',
  DELETE_AUDIO_FILE = 'DELETE_AUDIO_FILE',
  CREATE_CONTACT = 'CREATE_CONTACT',
  UPDATE_CONTACT = 'UPDATE_CONTACT',
  DELETE_CONTACT = 'DELETE_CONTACT',
  CREATE_FEATURE = 'CREATE_FEATURE',
  UPDATE_FEATURE = 'UPDATE_FEATURE',
  DELETE_FEATURE = 'DELETE_FEATURE',
}

export interface LogDto {
  user: UserDto | null;
  timestamp: string;
  type: LogType;
  info: string;
}

export interface UserDto {
  id: number;
  name: string;
}

export interface ChannelDto {
  id: number;
  name: MultiLocaleString;
}

export interface AllChannelDto {
  post: ChannelDto[];
  event: ChannelDto[];
}

export interface CreateChannelDto {
  name: MultiLocaleString;
}

export interface PostFileDto {
  id: number;
  fileName: string;
}

export interface PostDto {
  id: number;
  channel: ChannelDto;
  date: string;
  title: MultiLocaleString;
  content: MultiLocaleString;
  files: PostFileDto[];
}

export interface CreatePostDto {
  channelId: number;
  date: string;
  title: MultiLocaleString;
  content: MultiLocaleString;
  files: PostFileDto[];
}

export interface NewsDto {
  posts: PostDto[];
  pinned: PostDto[];
}

export interface PostGroupDto {
  channel: ChannelDto;
  posts: PostDto[];
}

export interface CoordsDto {
  x: number;
  y: number;
}

export enum RegistrationField {
  MATRICULATION_NUMBER = 'MATRICULATION_NUMBER',
  FIRST_NAME = 'FIRST_NAME',
  LAST_NAME = 'LAST_NAME',
  EMAIL = 'EMAIL',
  ADDRESS = 'ADDRESS',
  COUNTRY = 'COUNTRY',
  DATE_OF_BIRTH = 'DATE_OF_BIRTH'
}

export interface RegistrationData {
  timestamp: string;
  token: string;
  matriculationNumber?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  address?: string;
  country?: string;
  dateOfBirth?: string;
}

export enum ExportType {
  PDF = 'PDF',
  WORD = 'WORD',
  EXCEL = 'EXCEL',
}

export interface EventDto {
  id: number;
  channel: ChannelDto;
  place: string | null;
  coords: CoordsDto | null;
  startTime: string;
  endTime: string;
  name: MultiLocaleString;
  info: MultiLocaleString | null;
  registrationFields: RegistrationField[];
  registrationSlots: number;
  registrationSlotsWaiting: number;
  registrationOpen: boolean;
  registrations: RegistrationData[];
}

export interface CreateEventDto {
  channelId: number;
  name: MultiLocaleString;
  info: MultiLocaleString | null;
  place: string | null;
  coords: CoordsDto | null;
  startTime: string;
  endTime: string | null;
}

export enum IntervalType {
  WEEKLY = 'WEEKLY',
  BIWEEKLY = 'BIWEEKLY',
  MONTHLY = 'MONTHLY',
  MONTHLY_WEEKDAY = 'MONTHLY_WEEKDAY',
}

export interface LinkDto {
  id: number;
  channel: ChannelDto;
  url: MultiLocaleString;
  info: MultiLocaleString;
}

export interface CreateLinkDto {
  channelId: number;
  url: MultiLocaleString;
  info: MultiLocaleString;
}

export interface LinkGroupDto {
  channel: ChannelDto;
  links: LinkDto[];
}

export interface HandbookBookmarkResponseDto {
  DE: HandbookBookmarkDto[];
  EN: HandbookBookmarkDto[];
}

export interface HandbookBookmarkDto {
  page: number;
  name: string;
}

export interface AudioFileDto {
  id: number;
  name: MultiLocaleString;
  file: MultiLocaleString;
  text?: MultiLocaleString;
  image?: MultiLocaleString;
}

export interface AudioDto {
  id: number;
  name: MultiLocaleString;
  image?: MultiLocaleString;
  files: AudioFileDto[];
}

export interface ContactDto {
  id: number;
  file?: string;
  name: MultiLocaleString;
  place?: string;
  email?: string;
  phoneNumber?: string;
  openingHours?: MultiLocaleString;
  links: string[];
}

enum NativeFeature {
  MAP = 'MAP',
  MY_EVENTS = 'MY_EVENTS',
  MENSA = 'MENSA',
  LINKS = 'LINKS',
  HANDBOOK = 'HANDBOOK',
  AUDIO = 'AUDIO',
  FAQ = 'FAQ',
  CONTACTS = 'CONTACTS',
  EMAILS = 'EMAILS',
  GRADES = 'GRADES',
  TIMETABLE = 'TIMETABLE'
}

export interface FeatureDto {
  id: number;
  favorite: boolean;
  name?: MultiLocaleString;
  icon?: string;
  nativeFeature?: NativeFeature;
  post?: PostDto;
  link?: LinkDto;
}

export interface AppStartDto {
  date: string;
  android: number;
  ios: number;
}

export interface CurrentAppStarts {
  month: number;
  week: number;
  day: number;
}
