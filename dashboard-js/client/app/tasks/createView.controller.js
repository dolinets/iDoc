angular.module('dashboardJsApp').controller('createView', ['$scope', '$rootScope', 'DocumentsService', 'iGovNavbarHelper',
  '$stateParams', 'Auth', 'tasks', '$state', 'lunaService', 'tasksSearchService', 'snapRemote', 'Tab', '$location', 'CurrentServer', '$cookies',
  function ($scope, $rootScope, DocumentsService, iGovNavbarHelper, $stateParams, Auth, tasks, $state, lunaService,
            tasksSearchService, snapRemote, Tab, $location, CurrentServer, $cookies) {
    $scope.selectedBP = null;
    $scope.createButtonDisabled = false;
    $rootScope.spinner = false;
    $rootScope.tabMenu = $stateParams.tab;
    CurrentServer.resetServer();

    $scope.hasTasksOrDocuments = function () {
      var user = JSON.parse($cookies.get('user')).id;
      if(user) {
        if($rootScope.sUserOnTab){
          if($rootScope.sUserOnTab === user){
            // skip doing request
          } else {
            fillHasDocuments(user);
          }
        } else {
          fillHasDocuments(user);
        }
      }
    };
    function fillHasDocuments(user) {
      $rootScope.sUserOnTab = user;
      DocumentsService.isUserHasDocuments(user).then(function (res) {
        $rootScope.usersDocumentsBPs = [];
        $rootScope.userTasksBPs = [];
        if(Array.isArray(res) && res.length > 0) {
          $rootScope.usersDocumentsBPs = res.filter(function (item) {
            return item.oSubjectRightBP.sID_BP.charAt(0) === '_' && item.oSubjectRightBP.sID_BP.split('_')[1] === 'doc';
          });
          $rootScope.userTasksBPs = res.filter(function (item) {
            return item.oSubjectRightBP.sID_BP.indexOf('_doc_') !== 0;
          });
        }
      });
    }
    $scope.hasTasksOrDocuments();

    $scope.onSelectDocument = function (item) {
      $scope.createButtonDisabled = true;
      $rootScope.spinner = true;
      DocumentsService.createNewDocument(item.oSubjectRightBP.sID_BP).then(function (res) {
        var type = 'myDocuments',
          params = {
            tab: Tab.getCurrentTab($location.url(), $stateParams, $location.hash()),
            type: type,
            '#': type,
            sID_Order: res.sID_Order
          };
        $state.go('tasks.typeof.view', params);
      }).catch(function (e) {
        $scope.createButtonDisabled = false;
        $rootScope.spinner = false;
      });
    };

    $scope.onSelectTask = function (task) {
      localStorage.setItem('selected-task', JSON.stringify(task));
      tasks.createNewTask(task.oSubjectRightBP.sID_BP).then(function (res) {
        localStorage.setItem('creating', JSON.stringify(res.data[0]));
        $state.go('tasks.typeof.newtask', {id:res.data[0].deploymentId});
      })
    };

    $scope.createViewTab = function (type) {
      if($stateParams.tab) {
        if(type === $stateParams.tab)
          return true;
      }
    };

    $scope.selectBPTemplate = function (temp) {
      $scope.selectedBP = temp;
    };

    $scope.assignTask = function (id) {
      tasks.assignTask(id, Auth.getCurrentUser().id)
    };

    function toggleMenu(status) {
      if(typeof status === 'boolean') {
        if(status) {
          $scope.isMenuOpened = true;
          snapRemote.open('left');
        } else {
          $scope.isMenuOpened = false;
          snapRemote.close();
        }
        localStorage.setItem('menu-status', JSON.stringify(status));
      }
    }

    var menuStatus = localStorage.getItem('menu-status');
    if(menuStatus) {
      var status = JSON.parse(menuStatus);
      toggleMenu(status);
    } else {
      $scope.isMenuOpened = false;
      snapRemote.close();
    }

    $rootScope.toggleMenu = function () {
      $scope.isMenuOpened = !$scope.isMenuOpened;
      localStorage.setItem('menu-status', JSON.stringify($scope.isMenuOpened));
    };

    $scope.historyBack = function () {
      window.history.back()
    };
  }]);
