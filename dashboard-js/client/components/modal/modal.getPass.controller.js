'use strict';

angular.module('dashboardJsApp')
  .controller('modalGetPassController', function ($scope, $modalInstance, GetPassService, Modal) {

    $scope.getPassword = function () {
      console.log($scope.userInfo);
      GetPassService.sendPasswordOfUser($scope.userInfo).then(function (response) {
        if (response && !response.code && response.indexOf("Sent password") > -1){
          Modal.inform.success()("На вашу пошту " + $scope.userInfo.email + " було надіслано пароль від акаунта");
          $modalInstance.dismiss('cancel');
        } else {
          if (response.code && response.message && response.code === "BUSINESS_ERR" && response.message.indexOf("aSubjectContact") > -1 && response.message.indexOf("sUserMail") > -1) {
            Modal.inform.error()("Поштова адреса " + $scope.userInfo.email + " не знайдена в системі. Перевірте правильність введеної пошти");
          } else if (response.code && response.message && response.code === "BUSINESS_ERR" && response.message.indexOf("is alien") > -1){
            Modal.inform.error()("Логін " + $scope.userInfo.login + " не відповідає поштовій адресі " + $scope.userInfo.email);
          } else if (response.code && response.message && response.code === "BUSINESS_ERR" && response.message.indexOf("Account not found") > -1){
            Modal.inform.error()("Не знайдено облікового запису для логіну " + $scope.userInfo.login);
          } else {
            Modal.inform.error()("Сталася помилка, спробуйте ще раз пізніше");
          }
        }
      });
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  });
