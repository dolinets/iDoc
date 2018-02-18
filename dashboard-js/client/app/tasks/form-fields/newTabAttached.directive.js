'use strict';
angular.module('dashboardJsApp')
  .directive('newTabAttach', function($http, CurrentServer) {
    return {
      restrict: 'E',
      link: function($scope, element, attr) {
        var taskServer = CurrentServer.getServer();
        var server = taskServer.another ? '/server/' + taskServer.name : '';
        var url;
        element.click(function() {
          var name = $scope.field.fileName;
          if($scope.field.isFromHTML) {
            url = '/api/tasks/download/' + $scope.field.sKey + '/attachment/' + $scope.field.storageType + '/' + name;
          } else {
            url = '/api/tasks/download/' + $scope.taskData.oProcess.nID + '/attachment/' + $scope.field.id + server;
          }

          if ($scope.sSelectedTask === 'docHistory'){
            if (server) {
              url = '/api/tasks/download/' + $scope.field.sKey + '/attachment/' + $scope.field.storageType + '/' + name + '' + server;
            } else {
              url = '/api/tasks/download/' + $scope.field.sKey + '/attachment/' + $scope.field.storageType + '/' + name ;
            }
          }
          $http.get(url).then(function(data) {
            (function() {
              var winRef = window.open("", "_blank");
              winRef.document.write("<head> <meta charset='utf-8'><title>" + name + "</title></head>");
              if (attr.fileext === 'img') {
                winRef.document.write("<img src= '" + url + "'/>");
              } else if (attr.fileext === 'text') {
                winRef.document.write("<p style='max-width:95%; margin:15px auto'>" + data.data + "</p>");
              }
              return winRef;
            })();
          });
        })
      },
      template: '<button class="btn btn-igov">Переглянути</button>',
      replace: true
    };
  });
