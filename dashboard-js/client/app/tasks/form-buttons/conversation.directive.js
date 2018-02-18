'use strict';

angular.module('dashboardJsApp').directive('conversation', ['$http', 'tasks', 'Auth', '$rootScope', '$timeout', 'Modal',
  function($http, tasks, Auth, $rootScope, $timeout, Modal) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/conversation.html',
      link: function (scope) {
        scope.accepted = {
          selected : null
        };
      }
    };
  }]);


