(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .config([
      '$stateProvider',
      function ($stateProvider) {
        $stateProvider
          .state('tools', {
            url: '/tools',
            templateUrl: 'app/tools/tools.html',
            access: {
              requiresLogin: true
            }
          });
      }
    ]);
})();
