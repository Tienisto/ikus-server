
let snackbarFunc;

export function initSnackbar(func) {
    snackbarFunc = func;
}

export function showSnackbar(text) {
    snackbarFunc(text);
}

export function localizedString(obj, locale) {
    return locale === 'EN' ? obj.en : obj.de;
}

export function sleep(millis) {
    return new Promise(resolve => setTimeout(resolve, millis));
}

export function logTypeString(type) {
    switch(type) {
        case 'CREATE_USER': return 'Moderator/in erstellt';
        case 'UPDATE_USER_NAME': return 'Nutzername geändert';
        case 'UPDATE_USER_PASSWORD': return 'Nutzerpasswort geändert';
        case 'DELETE_USER': return 'Moderator/in gelöscht';
        case 'CREATE_CHANNEL': return 'Kanal erstellt';
        case 'UPDATE_CHANNEL': return 'Kanal umbenannt';
        case 'DELETE_CHANNEL': return 'Kanal gelöscht';
        case 'CREATE_POST': return 'Beitrag erstellt';
        case 'UPDATE_POST': return 'Beitrag geändert';
        case 'DELETE_POST': return 'Beitrag gelöscht';
        case 'PIN_POST': return 'Beitrag angepinnt';
        case 'UNPIN_POST': return 'Beitrag-Pin entfernt'
        case 'CREATE_EVENT': return 'Event erstellt';
        case 'UPDATE_EVENT': return 'Event geändert';
        case 'DELETE_EVENT': return 'Event gelöscht';
        case 'CREATE_LINK': return 'Link erstellt';
        case 'UPDATE_LINK': return 'Link geändert';
        case 'DELETE_LINK': return 'Link gelöscht';
        case 'UPDATE_HANDBOOK': return 'Handbook geändert';
        case 'UPDATE_HANDBOOK_BOOKMARKS': return 'Handbook (Inhaltsverzeichnis) geändert';
        case 'CREATE_CONTACT': return 'Neue Kontaktdaten';
        case 'UPDATE_CONTACT': return 'Kontaktdaten geändert';
        case 'DELETE_CONTACT': return 'Kontaktdaten gelöscht';
        case 'CREATE_FEATURE': return 'Feature erstellt';
        case 'UPDATE_FEATURE': return 'Feature geändert';
        case 'DELETE_FEATURE': return 'Feature gelöscht';
        default: return 'unbekannt';
    }
}

export const ICONS = {
    'mdi-virus': 'coronavirus',
    'mdi-text-subject': 'subject',
    'mdi-school': 'school'
}