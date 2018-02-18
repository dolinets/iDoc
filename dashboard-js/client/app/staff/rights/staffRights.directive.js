'use strict'

angular.module('dashboardJsApp')
    .directive('staffRights', ['Staff', 'Modal',
        function(Staff, Modal) {
            return {
                restrict: 'EA',
                templateUrl: 'app/staff/rights/staffRights.html',

                transclude: true,
                link: function(scope, elem, attrs) {
                    scope.rights = [];
                    scope.selectedRight = {};

                    function getAllRights() {
                        scope.contactLoading = 'Завантаження...';
                        Staff.getAllBP(scope.currHuman.login).then(function(res) {
                            if (res && res.length)
                                scope.rights = res.filter(function(item, i, arr) {
                                    return arr.indexOf(item) === i;
                                });

                            scope.contactLoading = false;
                        });
                    }

                    scope.showAddModal = function() {
                        scope.showRightsModal = true;
                        getAllRights();
                    };

                    scope.cancel = function() {
                        scope.showRightsModal = false;
                        scope.contactLoading = false;
                        scope.rights = [];
                        scope.selectedRight = {};
                    };

                    scope.addRight = function() {
                        scope.contactLoading = 'Збереження...';

                        Staff.setSubjectRightBP(scope.selectedRight.s.sID, scope.currHuman.login).then(function(res) {
                            if (res.code === 'BUSINESS_ERR') {
                                Modal.inform.error()(res.message);
                                return;
                            }

                            if (!scope.currHuman.rights)
                                scope.currHuman.rights = [];
                            scope.currHuman.rights.push(scope.selectedRight.s);

                            scope.cancel();
                            Modal.inform.info()("Права додано");
                        });
                    };

                    scope.removeRight = function(index) {
                        scope.contactLoading = 'Видалення...';

                        var oRight = scope.currHuman.rights[index];
                        Staff.removeSubjectRightBP(oRight.sID, scope.currHuman.login).then(function(res) {
                            if (res.code === 'BUSINESS_ERR') {
                                Modal.inform.error()(res.message);
                                return;
                            }
                            scope.currHuman.rights.splice(index, 1);

                            scope.cancel();
                            Modal.inform.info()("Права видалені");
                        });
                    };

                }

            };
        }]);
