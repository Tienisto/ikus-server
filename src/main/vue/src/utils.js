
let snackbarFunc;

export function initSnackbar(func) {
    snackbarFunc = func;
}

export function showSnackbar(text) {
    snackbarFunc(text);
}