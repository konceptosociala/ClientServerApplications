function app() {
   $('#logout').on('click', function(e) {
      e.preventDefault();

      localStorage.removeItem('userToken');
      location.reload(); 
   })
}