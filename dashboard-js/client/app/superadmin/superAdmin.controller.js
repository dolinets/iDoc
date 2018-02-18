(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('SuperAdminCtrl', SuperAdminCtrl);


  SuperAdminCtrl.$inject = ['$scope', 'GetPassService', 'Modal', 'Auth'];
  function SuperAdminCtrl($scope, GetPassService, Modal, Auth) {
    $scope.iGovTitle = "Панель адміністрування";
    $scope.isMenuOpened = false;
    $scope.allUsersPass = {
      bSend: false,
      bSendAdmin: true
    };
    $scope.singleUserPass = {
      sUserLogin: ""
    };


    $scope.checkIfSuperAdmin = function () {
      return Auth.isSuperAdmin();
    };

    if (!Auth.isSuperAdmin()) {
      Modal.inform.error()("Панель адміністрування доступна тільки адміністраторам зі статусом \"superadmin\"")
    }

    $scope.sendPasswordCustom = function () {
      GetPassService.sendPasswordOfUserCustom($scope.singleUserPass.sUserLogin).then(function (response) {
        if (response && response.indexOf("password of User to Admin ok!")) {
          Modal.inform.success()("Пароль було надіслано")
        } else {
          Modal.inform.error()("Під час спроби надіслати пароль сталася помилка");
        }
      });
    };

    $scope.sendPasswordsUsersOnServer = function () {
      GetPassService.sendPasswordsOfUsersOnServer($scope.allUsersPass).then(function (response) {
        if (response && response.indexOf('Sent passwords to Administrator') > -1 ){
          if ($scope.allUsersPass.bSend === true){
            Modal.inform.success()("Усі паролі було надіслано користувачам")
          } else if ($scope.allUsersPass.bSendAdmin === true){
            Modal.inform.success()("Усі паролі користувачів було надіслано адміністратору")
          }
        }
      });
    };
  }

})();
