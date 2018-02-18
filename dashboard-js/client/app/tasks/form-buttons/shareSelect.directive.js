'use strict';

angular.module('dashboardJsApp').directive('shareSelect', ['$http', 'tasks', 'Auth', '$rootScope', '$timeout', 'DocumentsService', 'Modal', 'CurrentServer',
  function($http, tasks, Auth, $rootScope, $timeout, DocumentsService, Modal, CurrentServer) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/shareSelect.html',
      link: function (scope) {
        scope.currentUrl = window.location.href;

        scope.copyLink = function () {
          var linkToCopy = scope.currentUrl;

          var dummy = document.createElement('input');
          document.body.appendChild(dummy);
          dummy.setAttribute('id', 'dummy_id');
          document.getElementById('dummy_id').value = linkToCopy;
          dummy.select();
          document.execCommand('copy');
          document.getElementById('dummy_id').style.display = 'none';
          document.body.removeChild(dummy);
        };
      }
    };
  }]);
