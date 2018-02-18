(function () {
    'use strict';

    angular
      .module('dashboardJsApp')
      .controller('StaffCtrl', staffCtrl);


    staffCtrl.$inject = ['$scope', '$stateParams', '$location', '$rootScope', '$http', 'Tab','Modal', 'snapRemote', '$cookieStore', 'Auth',  '$timeout', 'Staff'];
    function staffCtrl($scope, $stateParams, $location, $rootScope, $http, Tab, Modal, snapRemote, $cookieStore,  Auth,  $timeout, Staff) {
      $scope.iGovTitle = "Персонал";
      $scope.isMenuOpened = false;
      //snapRemote.settings({minPosition: -466, maxPosition: 466});


      $rootScope.tabMenu = Tab.getCurrentTab($location.url(), $stateParams, $location.hash());

      $scope.tabMenuChange = function (param) {
        $rootScope.tabMenu = param;
          $scope.spinner = true;
          $scope.rootStaffCompany();
      };

        $scope.addNewComp = function () {
            $rootScope.newComp = true;
          $scope.currPosition = {
            "position":  "",
            "parent":  "",
            "login":  ""

          };
            console.log($rootScope.newComp);
        }

        $scope.addNewHuman = function () {
            $rootScope.newHuman = true;
          $scope.currHuman = {
            "firstname": "",
            "secondname": "",
            "lastname": "",
            "position": "",
            "login": "",
            "email": "",
            "depart": "",
            "phone": "",
            "departID": "",
            "isHead": false
          };
          $scope.subposition.selected = null;
          $scope.subdivision.selected = null;
         }



      function toggleMenu(status) {
        if (typeof status === 'boolean') {
            //snapRemote.settings({touchToDrag: false, tapToClose: false, minPosition: -466, maxPosition: 466});
            //snapRemote.globalOptions = {touchToDrag: false, tapToClose: false, minPosition: -466, maxPosition: 466};
            //.globalOptions = { touchToDrag: false, tapToClose: false }
          if (status) {
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
      if (menuStatus) {
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

      snapRemote.getSnapper().then(function (snapper) {
        /*snapper.settings({touchToDrag: false, tapToClose: false, minPosition: -466, maxPosition: 466});*/
        snapper.settings({minPosition: -466, maxPosition: 466});
        snapper.on('animated', function () {
            snapper.settings({minPosition: -466, maxPosition: 466});
          if (snapper.state().state === 'closed') {
            $scope.isMenuOpened = false;
            $scope.$apply();
          } else if (snapper.state().state === 'left') {
            $scope.isMenuOpened = true;
            $scope.$apply();
          }
        });
      });

      /*function checkStaffRole(user){
        var role = {
          params: {
            sID_Group_Activiti: user.id
          }
        };
       // Staff.checkAdmin(role).then(function (res) {
       //   var response = res.data;
       //   $scope.staffRole = response;
       //   console.log(response, $scope.staffRole);
       //  });
        $http.get('./api/staff/checkIsAdmin', role).then(function (res) {
            var response = res.data;
            $rootScope.staffRole = response;
        });
      }
        function getUserLogin(Auth) {
            $scope.currentUser = $cookieStore.get('user');
             checkStaffRole($scope.currentUser);
         }
        getUserLogin();*/

        $scope.currentUser = $cookieStore.get('user');
        $rootScope.staffRole = Auth.isAdmin();

         $scope.doWordWrap = function(value) {
          if (value.trim().indexOf(' ') > -1) 
            return 'normal';
          else
            return 'break-all';
        };
    }

})();
