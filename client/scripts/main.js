(function(run) {

  	run(window.jQuery, window, document);

}(function($, window, document) {

   $(function() {
      let content = $('.content');

      if (isLoggedIn()) {
         content.load('app.html', app);
      } else {
         content.load('login.html', auth);
      }
   });

}));