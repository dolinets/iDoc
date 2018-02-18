/**
 * Created by irish on 10.11.17.
 */

(function () {
    'use strict';

    angular
        .module('dashboardJsApp')
        .controller('staffAddPosition', staffAddPosition);

    staffAddPosition.$inject = ['$scope', 'Auth', 'Modal', '$http', 'Staff', '$cookies'];
    function staffAddPosition($scope, Auth, Modal, $http, Staff, $cookies) {
        $scope.newPosition = "";

        var login = Auth.getCurrentUser();
        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : login.id;

        $scope.saveStaffPosition = function () {
            var posit = {params: {sNote: $scope.newPosition, bCreate: true, sLogin: login.id, sLoginReferent: sCurrReferent}};

          Staff.createNewPosition(posit).then(function (res) {
                if (res.code !== 'SYSTEM_ERR') {
                    Modal.inform.info()("Посаду додано успішно");
                    $scope.cancelStaffPosition();
                } else {
                    if (res.message.indexOf('already exists') > -1) 
                        Modal.inform.warning()('Посада з такою назвою вже існує в системі');
                    else
                        Modal.inform.error()(res.message);
                } 
            }, function (err) {
                Modal.inform.error()(res.message);
            });
        };

        $scope.modalShownPosition = false;
        $scope.cancelStaffPosition = function() {
            $scope.modalShownPosition = !$scope.modalShownPosition;
            $scope.newPosition = "";
            $scope.show = false;

        };
    }
})();
