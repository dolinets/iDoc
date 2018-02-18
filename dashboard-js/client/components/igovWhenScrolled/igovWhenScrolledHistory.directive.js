'use strict';

(function () {

  angular
    .module('dashboardJsApp')
    .directive('igovWhenScrolledHistory', [
      function () {
        return {
          link: function igovWhenScrolledPotLink(scope, element, attrs) {
            var elem = element[0];
            element.bind('scroll', function () {
              if (elem.scrollTop + elem.offsetHeight >= elem.scrollHeight - 50) {
                scope.$apply(attrs.igovWhenScrolledHistory);
              }
            });
          }
        };
      }
    ]);
})();

