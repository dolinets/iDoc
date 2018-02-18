(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('NavbarCtrl', navbarCtrl);

  navbarCtrl.$inject = ['$scope', '$rootScope', '$location', 'Auth', 'envConfigService', 'iGovNavbarHelper', 'tasksSearchService',
    '$state', 'tasks', 'lunaService', 'Modal', '$stateParams', 'processes', '$localStorage', 'signDialog', '$http', '$cookies', '$timeout'];
  function navbarCtrl($scope, $rootScope, $location, Auth, envConfigService, iGovNavbarHelper, tasksSearchService,
                      $state, tasks, lunaService, Modal, $stateParams, processes, $localStorage, signDialog, $http, $cookies, $timeout) {

    $scope.menu = [{
      'title': 'Задачі',
      'link': '/tasks'
    }];

    function isSessionExpired() {
      if ((!$cookies.get('JSESSIONID') || !$cookies.get('referent'))
            && window.location.pathname.indexOf('/share') !== 0) {
        $scope.logoutSpinner = true;
        Auth.logout();
        $state.go('main');
      }
    }
    isSessionExpired();

    $scope.navBarIsCollapsed = true;
    $scope.openCloseMenu = function () {
      $scope.navBarIsCollapsed = !$scope.navBarIsCollapsed;
    };

    envConfigService.loadConfig(function (config) {
      iGovNavbarHelper.isTest = config.bTest;
      $rootScope.config = config;
    });

    $scope.isAdmin = Auth.isAdmin;
    $scope.areInstrumentsVisible = false;
    $scope.iGovNavbarHelper = iGovNavbarHelper;
    iGovNavbarHelper.getSubFolders();
    $scope.state = $state;

    $scope.isSuperAdmin = Auth.isSuperAdmin();

    $scope.showLogoForUrl = function() {
      var location = window.location.href;
      var result = {
        showIdoc: true
      };

      var bValue = false;

      if (location.indexOf('cib') > -1) {
        bValue = true;
        result.showIdoc = false;
      }
      else if (location.indexOf('ntu') > -1 || location.indexOf('nstu') > -1) {
        bValue = true;
        result.showIdoc = true;
        result.type = 2;
      }

      $scope.personalLogo = result;

      return bValue;
    };

    $scope.isVisible = function(menuType){
      //$scope.menus = [{
      if(menuType === 'all'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit');
      }
      if(menuType === 'finished'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit', 'supervisor');
      }
      return Auth.isLoggedIn();
    };

    $scope.isVisibleInstrument = function(menuType){
      if(menuType === 'tools.users'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('superadmin');
      }
      if(menuType === 'tools.groups'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('superadmin');
      }
      return Auth.isLoggedIn();
    };

    $scope.getCurrentUserName = function() { //always returns name of user which was logged in
      var user = Auth.getCurrentUser();
      var referent = JSON.parse($cookies.get('referent'));
      if (user === referent){
        return user.firstName + ' ' + user.lastName;
      }
      else {
        return referent.firstName + ' ' + referent.lastName;
      }
    };

    $scope.getRealUserName = function () { //returns name of real user(if we takes a role of referent)
      var user = JSON.parse($cookies.get('user'));
      return user.firstName + ' ' + user.lastName;
    };

    $scope.goToServices = function() {
      $location.path('/services');
    };

    $scope.goToEscalations = function() {
      $location.path('/escalations');
    };

    $scope.goToReports = function () {
      $location.path('/reports');
    };

    $scope.logout = function() {
      $scope.logoutSpinner = true;
      Auth.logout().then(function () {
        $scope.logoutSpinner = false;
        $location.search({});
        $location.path('/login');
        $cookies.remove('referent');
      });
    };

    $scope.isActive = function(route) {
      return route === $location.path();
    };

    $scope.goToUsers = function () {
      $location.path('/users');
    };

    $scope.goToGroups = function () {
      $location.path('/groups');
    };

    $scope.goToDeploy = function () {
      $location.path('/deploy');
    };

    var bSelectedTasksSortReverse = false;

    $scope.$on('set-sort-order-reverse-true', function () {
      bSelectedTasksSortReverse = true;
    });

    $scope.$on('set-sort-order-reverse-false', function () {
      bSelectedTasksSortReverse = false;
    });

    //$scope.tasksSearch = iGovNavbarHelper.tasksSearch;
    var tempCountValue = 0;

    $scope.searchInputKeyup = function ($event) {
      if ($event.keyCode === 13 && $rootScope.tasksSearch.value) {
        runSearchingProcess();
      }
      if($event.keyCode === 8 || $event.keyCode === 46) {
        $scope.switchArchive = false;
      }
    };

    // запуск поиска для автотестов
    $rootScope.runSearchingProcess = function () {
      console.log('Start searching process');
      $rootScope.tasksSearch.value = $('.searched-text')["0"].value;
      console.log('$rootScope.tasksSearch.value = ' + $rootScope.tasksSearch.value);
      if ($rootScope.tasksSearch.value) {
        runSearchingProcess();
      }
      console.log('End searching process');
    };

    function runSearchingProcess() {
      $rootScope.tasksSearch.loading=true;
      $rootScope.tasksSearch.count=0;
      $rootScope.tasksSearch.submited=true;
      if($rootScope.tasksSearch.archive) {
        tasks.getProcesses($rootScope.tasksSearch.value).then(function (res) {
          var response = JSON.parse(res);
          $scope.archive = response[0];
          $scope.archive.aVisibleAttributes = [];
          angular.forEach($scope.archive.aAttribute, function (oAttribute) {
            if (oAttribute.oAttributeName.nOrder !== -1){
              $scope.archive.aVisibleAttributes.push(oAttribute);
            }
          });
          $scope.archive.aVisibleAttributes.sort(function (a, b) {
            return a.oAttributeName.nOrder - b.oAttributeName.nOrder;
          });
          $scope.switchArchive = true;
        })
      } else {
        tasksSearchService.searchTaskByUserInput($rootScope.tasksSearch.value, $scope.iGovNavbarHelper.currentTab, bSelectedTasksSortReverse)
          .then(function(res) {
            if(res.aIDs.length > 1){
              if(bSelectedTasksSortReverse){
                tempCountValue = (res.aIDs.length - res.nCurrentIndex) + ' / ' + res.aIDs.length;
              } else {
                tempCountValue = (res.nCurrentIndex + 1) + ' / ' + res.aIDs.length;
              }
              $rootScope.tasksSearch.count = '... / ' + res.aIDs.length;
            } else {
              tempCountValue = res.aIDs.length;
              $rootScope.tasksSearch.count = res.aIDs.length;
            }
          })
          .finally(function(res) {
            $rootScope.tasksSearch.loading=false;
          });
      }
    }

    $scope.$on('update-search-counter', function () {
      $rootScope.tasksSearch.count = tempCountValue;
    });

    $scope.closeArchive = function () {
      $scope.switchArchive = false;
    };

    $scope.archiveTextValue = function () {
      return isNaN($rootScope.tasksSearch.value);
    };

    $scope.isSelectedInstrumentsMenu = function(menuItem) {
      return menuItem.state==$state.current.name;
    };

    $scope.assignTask = function (id) {

      tasks.assignTask(id, Auth.getCurrentUser().id)
        .then(function (result) {
          /*Modal.assignDocument(function (event) {

          }, 'Документ успiшно створено');*/
        })
        .catch(function (e) {
          /*Modal.assignDocument(function (event) {

          }, 'Документ успiшно створено');*/
        });
    };

    $scope.showOrHideSelect = {show:false,type:''};
    $scope.hasDocuments = function () {
      var user;
      var referent;
      if ($cookies.get('referent')){
        referent = JSON.parse($cookies.get('referent'));
      }
      if (referent){
        user = referent.sLogin;
      } else {
        user = Auth.getCurrentUser() ? Auth.getCurrentUser().id : null;
      }
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
      tasks.isUserHasDocuments(user).then(function (res) {
        $rootScope.usersDocumentsBPs = [];
        $rootScope.userTasksBPs = [];
        if(Array.isArray(res) && res.length > 0) {
          $rootScope.usersDocumentsBPs = res.filter(function (item) {
            return item.oSubjectRightBP.sID_BP.charAt(0) === '_' && item.oSubjectRightBP.sID_BP.split('_')[1] === 'doc';
          });
          $rootScope.userTasksBPs = res.filter(function (item) {
            return item.oSubjectRightBP.sID_BP.indexOf('_doc_') !== 0;
          })
        }
      })
    }
    $scope.hasDocuments();

    $scope.document = {};
    $scope.openCloseUsersSelect = function (type) {
      $scope.showOrHideSelect.type = type;
      $scope.showOrHideSelect.show = !$scope.showOrHideSelect.show;
    };

    $scope.showCreateDocButton = function () {
      return $stateParams.type === "unassigned" || $stateParams.type === "selfAssigned" || $stateParams.type === 'documents';
    };

    $scope.onSelectDocument = function (item) {
      tasks.createNewDocument(item.oSubjectRightBP.sID_BP).then(function (res) {
        if(res.snID_Process) {
          tempCountValue = 0;
          var val = res.snID_Process + lunaService.getLunaValue(res.snID_Process);
          tasksSearchService.searchTaskByUserInput(val, 'documents')
            .then(function(res) {
              $scope.assignTask(res.aIDs[0], val)
            });
          $scope.showOrHideSelect.show = false;
        }
      });
    };

    $scope.onSelectTask = function (task) {
      tasks.createNewTask(task.oSubjectRightBP.sID_BP).then(function (res) {
        localStorage.setItem('creating', JSON.stringify(res.data[0]));
        $state.go('tasks.typeof.create', {id:res.data[0].deploymentId});
        $scope.showOrHideSelect.show = false;
      });
    };

    function setEcpStatusToLS(status) {
      var stringifyStatus = JSON.stringify(status);
      localStorage.setItem('auto-ecp-status', stringifyStatus);
    }

    function setDtasksToLS(status) {
      var stringifyStatus = JSON.stringify(status);
      localStorage.setItem('deleted-tasks-status', stringifyStatus);
    }

    var ecpStatusInLS = localStorage.getItem('auto-ecp-status');

    if(ecpStatusInLS !== null) {
      $rootScope.checkboxForAutoECP = JSON.parse(ecpStatusInLS);
    }else {
      $rootScope.checkboxForAutoECP = {status : true};
      setEcpStatusToLS($rootScope.checkboxForAutoECP);
    }

    var bShowDeletedTasksInLS = localStorage.getItem('deleted-tasks-status');
    if (bShowDeletedTasksInLS !== null)
      $rootScope.showDeletedTasks = JSON.parse(bShowDeletedTasksInLS);
    else {
      $rootScope.showDeletedTasks = {status: false};
      setDtasksToLS($rootScope.showDeletedTasks);
    }

    $scope.showDeletedCheckbox = Auth.isAdmin();

    $scope.$watch('[checkboxForAutoECP.status, showDeletedTasks.status]', function (newValues, oldValues) {
      if (oldValues[0] !== newValues[0]) {
        $rootScope.checkboxForAutoECP.status = newValues[0];
        setEcpStatusToLS($rootScope.checkboxForAutoECP);
      }

      if (oldValues[1] !== newValues[1]) {
        $rootScope.showDeletedTasks.status = newValues[1];
        setDtasksToLS($rootScope.showDeletedTasks);

        $state.reload();

      }

    }, true);

    $scope.showSignDialog = function () {
      signDialog.signManuallySelectedFile(function (signedContent) {
        console.log('PDF Content:' + signedContent.content);
      }, function () {
        console.log('Sign Dismissed');
      })
    };

    $scope.isActive = function (tab) {
      return $state.current.name.indexOf(tab) === 0;
    };

    var getReferents = function () {
      var queryParams = {params:{}};
      if($cookies.get('referent')) {
        var login = JSON.parse($cookies.get('referent')).id;
        queryParams.params.sLogin = login;
      } else {
        queryParams.params.sLogin = 'guest';
      }
      $http.get('/api/users/getUserGroupMember', queryParams).then(function (res) {
        $scope.referentList = res.data;
      });
    };
    getReferents();

    $scope.getShortName = function (name) {
      var nameArray = name.split(' ');
      for (var i = 1; i < nameArray.length; i++) {
        nameArray[i] = nameArray[i].slice(0, 1).concat(".");
      }
      return nameArray.join(" ");
    };

    $scope.setsLoginPrincipal = function (sLoginPrincipal) {
      $rootScope.spinner = true;
      var request = {
        method: 'GET',
        url: 'api/access/setsLoginPrincipal',
        params: {
          sLoginPrincipal: sLoginPrincipal ? sLoginPrincipal : JSON.parse($cookies.get('referent')).id
        }
      };
      $http(request).success(function (res) {
        $rootScope.spinner = false;
        if (sLoginPrincipal){
          var firstName = res.sFIO.split(' ')[0];
          var lastName = res.sFIO.split(' ')[1] + ' ' + res.sFIO.split(' ')[2];
          var temp = {
            firstName: firstName,
            lastName: lastName,
            id: res.sLogin
          }
          $cookies.putObject('user', temp);
          $state.reload();
          $scope.getRealUserName();
        } else {
          $cookies.putObject('user', JSON.parse($cookies.get('referent')));
          $state.reload();
        }
      }).finally(function () {
        $rootScope.spinner = false;
      });
    };

    var getRealUserData = function () {
      if($cookies.get('user')) {
        $scope.realUserData = JSON.parse($cookies.get('user'));
      } else {
        $scope.realUserData = null;
      }
    };
    getRealUserData();

    $scope.resizeToolMenu = function() {
      setTimeout(function() {
        var el = document.getElementsByClassName('dropdown-menu')[0];
        if (!el) return;
        var rect = el.getBoundingClientRect();

        if (rect.bottom >= window.innerHeight)
          el.style.height = (window.innerHeight-63) + 'px';
        else if (window.innerHeight-63-rect.bottom > 10)
          el.style.height = 'auto';
      }, 10);
    };

    window.addEventListener('resize', $scope.resizeToolMenu);
  }
})();
