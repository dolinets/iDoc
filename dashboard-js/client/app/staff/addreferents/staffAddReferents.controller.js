/**
 * Created by irish on 10.11.17.
 */

(function () {
    'use strict';

    angular
        .module('dashboardJsApp')
        .controller('staffAddReferents', staffAddReferents);

    staffAddReferents.$inject = ['$scope', 'Auth', 'Modal', '$http', 'Staff'];
    function staffAddReferents($scope, Auth, Modal, $http, Staff) {
        $scope.newReferents = "";
        
        $scope.saveStaffReferents = function () {
            var posit = {params: {sNote: $scope.newReferents}};

          Staff.createNewReferents(posit).then(function (res) {
                Modal.inform.info()("Посаду додано успішно");
                console.log(res);
                $scope.cancelStaffReferents();
            }, function (err) {
                Modal.inform.error()(JSON.parse(err).message);

            });
        };

        $scope.modalShownReferents = false;
        $scope.cancelStaffReferents = function() {
            $scope.modalShownReferents = !$scope.modalShownReferents;
            $scope.newReferents = "";
            $scope.show = false;
        };
    }
})();
