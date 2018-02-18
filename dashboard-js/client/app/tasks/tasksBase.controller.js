(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('TasksBaseCtrl', [
      '$scope',
      '$state',
      'tasksStateModel',
      '$rootScope',
      function ($scope, $state, tasksStateModel, $rootScope) {
        $scope.tasksStateModel = tasksStateModel;
        if ($state.current.name == 'tasks'){
          $state.go('tasks.typeof', {tab: 'tasks', type: 'tickets'});
        }
      }
    ]);
})();
