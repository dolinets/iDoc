/**
 * Created by irish on 10.11.17.
 */

(function () {
    'use strict';

    angular
        .module('dashboardJsApp')
        .controller('staffPassword',  staffPassword);

    staffPassword.$inject = ['$scope', 'Staff', 'Modal', '$rootScope', '$http'];
    function staffPassword($scope, Staff, Modal, $rootScope, $http) {

        $scope.oldPasswordStaff = "";
        $scope.newPasswordStaff = "";
        $scope.newPasswordStaff2 = "";

        $scope.changeStaffPass = function () {
            $scope.passLoading = 'Збереження...';
            delete $rootScope.userStaffLogin;
            if($scope.newPasswordStaff2 == $scope.newPasswordStaff){
                if (!/(?=^.{8,}$)(?=.*\d)((?=.*[a-z])|(?=.*[A-Z])).*$/.test($scope.newPasswordStaff)) {
                    console.log($scope.newPasswordStaff);
                    Modal.inform.error()("Пароль повинен складатися мінімум з 8 символів та містити хоча б 1 цифру та 1 літеру");
                    return;
                }

                Staff.changeStaffPassword($rootScope.currentStaffUser_sID_Group_Activiti, $scope.oldPasswordStaff, $scope.newPasswordStaff).then(function(data){
                    Modal.inform.info()("Пароль змінено");
                }, function (err) {
                    //Modal.inform.error()('Старий пароль вказано невірно');
                }).finally(function() {$scope.cancelStaffPass();});
            } else {
                Modal.inform.error()("Нові паролі не збігаються");
            }
        };

        $scope.cancelStaffPass = function () {
            $scope.oldPasswordStaff = "";
            $scope.newPasswordStaff = "";
            $scope.newPasswordStaff2 = "";
            $scope.passLoading = false;

            $scope.show = false;
        };
    }
})();
