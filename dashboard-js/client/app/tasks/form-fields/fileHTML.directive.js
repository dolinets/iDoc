'use strict';
angular.module('dashboardJsApp')
  .directive('fileHtml', [ 'tasks', '$http', 'Modal', '$rootScope',
    function(tasks, $http, Modal, $rootScope) {
      return {
        restrict: 'E',
        templateUrl: 'app/tasks/form-fields/fileHTML.html',
        link: function (scope, element, attrs, ngModel) {
          var path;
          if (scope.item && scope.item.name && scope.item.name.indexOf('pattern/') > -1 && scope.item.writable && scope.item.writable === true
            && scope.taskData && scope.taskData.mProcessVariable && scope.taskData.mProcessVariable.sKey_Step_Document === 'step_1'){
            if (scope.item.name.split(";")[2]){
              path = scope.item.name.split(";")[2];
              path = path.substr(1, path.length - 2);
              path = path.replace("pattern", "");
              tasks.getPatternFile(path).then(function (response) {
                scope.item.valueVisible = response;
              });
            }
          }

          scope.checkLinks = function () {
            scope.$watch('item.valueVisible', function(newValue, oldValue) {
              var match;
              if (newValue){
                match = newValue.toString().match(/<a href="Завантаження..."[^>]*>(.*?)<\/a>/i);
              }
              if (match && match.length !== 0){
                scope.item.valueVisible = scope.item.valueVisible.replace(match[0], match[1]);
                Modal.inform.error()('Необхідно дочекатися закінчення завантаження. Спробуйте додати посилання ще раз.');
              }
            });
          };
        }
      };
    }]);
