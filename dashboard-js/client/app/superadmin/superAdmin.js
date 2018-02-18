(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .config(superAdminConfig);

  superAdminConfig.$inject = ['$stateProvider'];
  function superAdminConfig($stateProvider) {
    $stateProvider
      .state('superadmin', {
        url: '/superadmin',
        views: {
          '@': {
            templateUrl: 'app/superadmin/superadmin.html',
            controller: 'SuperAdminCtrl'
          }
        },
        access: {
          requiresLogin: true
        },
        resolve: {
          accountSubjects: [
            'Profile',
            'Auth',
            function (Profile, Auth) {
              return Profile.getSubjects(Auth.getCurrentUser().id);
            }
          ]
        }
      });
  }
})();
