function auth() {
   $('#login-form').on('submit', function (e) {
      e.preventDefault();

      const login = $('#input-login').val();
      const password = $('#input-password').val();
      const token = btoa(login + ':' + password);

      $.ajax({
         url: 'http://localhost:8211/',
         method: 'GET',
         beforeSend: function (xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + token);
         },
         success: function (data) {
            localStorage.setItem('userToken', token);
            location.reload(); 
         },
         error: function (xhr) {
            if (xhr.status === 401) {
               spawnAlert('Invalid login or password');
            } else if (xhr.status === 0) {
               spawnAlert('Cannot connect to server');
            } else {
               spawnAlert('An unexpected error occurred');
            }
         }
      });
   });
}

function isLoggedIn() {
   return !!localStorage.getItem('userToken');
}

function spawnAlert(message) {
   var alertBox = `
   <div class="alert alert-dismissible alert-danger">
      <button id="close-login-error" type="button" class="btn-close" data-bs-dismiss="alert"></button>
      <strong>Error:</strong> ${message}.
   </div>
   `;

   $('#login-error')
      .css('display', 'none')
      .html(alertBox)
      .fadeIn();

   $('#close-login-error').on('click', function () {
      $('#login-error').fadeOut();
   });
}