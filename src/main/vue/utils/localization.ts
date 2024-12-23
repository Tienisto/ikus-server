import type {MultiLocaleString} from "~/models";

export function localizedString(obj: MultiLocaleString, locale: string) {
  return locale === 'EN' ? obj.en : obj.de;
}

const dateTimeFormat = {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false // Use 24-hour format
} as Intl.DateTimeFormatOptions;

const dateTimeFormatter = new Intl.DateTimeFormat('de-DE', dateTimeFormat);

// yyyy-MM-dd HH:mm
export function formatDateTime(date: Date): string {
  return dateTimeFormatter.format(date);
}

// HH (not HH:mm)
export function formatTimeHourOnly(date: Date): string {
  const hour = date.getHours();
  return hour < 10 ? `0${hour}` : `${hour}`;
}

// HH:mm
export function formatTime(date: Date): string {
  const hour = date.getHours();
  const minute = date.getMinutes();
  return `${hour < 10 ? `0${hour}` : `${hour}`}:${minute < 10 ? `0${minute}` : `${minute}`}`;
}

// yyyy-MM-dd
export function formatIsoDate(date: Date): string {
  return date.toISOString().substring(0, 10);
}
