(function () {
    'use strict';

    angular
      .module('dashboardJsApp')
      .config(staffConfig);

    staffConfig.$inject = ['$stateProvider'];
    function staffConfig($stateProvider) {
      $stateProvider
        .state('staff', {
          url: '/staff',
          views: {
            '@': {
              templateUrl: 'app/staff/staff.html',
              controller: 'StaffCtrl'
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
        })
        .state('staff.profile', {
          access: {
            requiresLogin: true
          },
          templateUrl: 'app/staff/position/position.html',
          controller: 'PositionCtrl'
        })
        .state('staff.employee', {
          access: {
            requiresLogin: true
        },
        templateUrl: 'app/staff/employee/employee.html',
        controller: 'EmployeeCtrl'
      });
     }
  })();
