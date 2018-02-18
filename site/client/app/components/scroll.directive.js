angular.module('siteiDoc')
  .directive('scroll',
    function() {
      return {
        controller : "mainController",
        scope: true,
        link: function(scope, elem, attr) {

         jQuery.noConflict();
          jQuery(document).ready(function () {
            jQuery(".move-pages").onepage_scroll({
              sectionContainer: ".new-pages",
              easing: "ease",
              animationTime: 1000,
              pagination: false,
              updateURL: true,
              beforeMove: function (index) {

              },
              afterMove: function (index) {

              },
              loop: false,
              keyboard: true,
              responsiveFallback: 960,
              direction: "vertical"
            });
          });
        }
      };
    });
