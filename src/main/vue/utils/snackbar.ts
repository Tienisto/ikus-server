let snackbarFunc: (text: string) => void | null;

export function initSnackbar(func: (text: string) => void) {
  snackbarFunc = func;
}

export function showSnackbar(text: string) {
  snackbarFunc(text);
}
