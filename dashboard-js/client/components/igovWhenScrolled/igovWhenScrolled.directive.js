'use strict';

(function () {

  angular
    .module('dashboardJsApp')
    .directive('igovWhenScrolled', [
      function () {
        return {
          link: function igovWhenScrolledPotLink(scope, element, attrs) {
            var elem = element[0];
            element.bind('scroll', function () {
              if (elem.scrollTop + elem.offsetHeight >= elem.scrollHeight - 100) {
                scope.$apply(attrs.igovWhenScrolled);
              }
            });
          }
        }
      }
    ])
})();

