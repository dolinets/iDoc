'use strict';

angular.module('dashboardJsApp')
  .controller('LoginCtrl', function(Auth, Idle, Modal, $scope, $location, $state, $rootScope) {

    $scope.user = {};
    $scope.errors = {};

    while (document.getElementById('atlwdg-trigger')) {
      $("#atlwdg-trigger").remove();
      $("#atlwdg-blanket").remove();
      $("#atlwdg-container").remove();
    }

    $scope.login = function(form) {
      $scope.submitted = true;
      $scope.authProcess = false;
      $scope.loggedIn = false;

      if (form.$valid) {
        $scope.authProcess = true;

        Auth.login({
            login: $scope.user.login,
            password: $scope.user.password
          })
          .then(function() {
            $scope.loggedIn = true;

            if ($rootScope.continueUrl) {
              $location.url($rootScope.continueUrl);
              $rootScope.continueUrl = null;
            }

            if($rootScope.ProjectRegion_MainPage_bTasksOnly === 'FALSE'){
              $state.go('tasks.typeof', {tab: 'documents', type:'myDocuments'});
            }else{
              $state.go('tasks.typeof', {tab: 'tasks', type:'unassigned'});
            }
          })
          .catch(function(err) {
            $scope.authProcess = false;
            $scope.errors.other = err ? err.message : 'Невідома помилка';
          });
      }
    };
  });
