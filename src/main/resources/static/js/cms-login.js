/* Progressive enhancement for the sign-in form: a show/hide control for the password field.
   Nothing here is required to sign in. The file is served by this application, and the page's
   Content-Security-Policy allows no inline script, so this is the only code that runs on the page. */
(function () {
  var button = document.querySelector('[data-reveal]');
  var field = document.getElementById('password');
  if (!button || !field) {
    return;
  }

  button.hidden = false;
  button.addEventListener('click', function () {
    var revealed = field.type === 'text';
    field.type = revealed ? 'password' : 'text';
    button.setAttribute('aria-pressed', revealed ? 'false' : 'true');
    button.textContent = revealed ? 'Show' : 'Hide';
    field.focus();
  });
})();
