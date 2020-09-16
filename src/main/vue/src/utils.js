
let snackbarFunc;

export function initSnackbar(func) {
    snackbarFunc = func;
}

export function showSnackbar(text) {
    snackbarFunc(text);
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
        case 'CREATE_EVENT': return 'Event erstellt';
        case 'UPDATE_EVENT': return 'Event geändert';
        case 'DELETE_EVENT': return 'Event gelöscht';
        case 'CREATE_LINK_GROUP': return 'Linkgruppe erstellt';
        case 'UPDATE_LINK_GROUP': return 'Linkgruppe geändert';
        case 'DELETE_LINK_GROUP': return 'Linkgruppe gelöscht';
        case 'CREATE_LINK': return 'Link erstellt';
        case 'UPDATE_LINK': return 'Link geändert';
        case 'DELETE_LINK': return 'Link gelöscht';
        case 'UPDATE_HAND_BOOK': return 'Handbook geändert';
        case 'CREATE_CONTACT': return 'Neue Kontaktdaten';
        case 'UPDATE_CONTACT': return 'Kontaktdaten geändert';
        case 'DELETE_CONTACT': return 'Kontaktdaten gelöscht';
        default: return 'unbekannt';
    }
}

export function localizedString(obj, locale) {
    return locale === 'EN' ? obj.en : obj.de;
}