/**
 * Created by irish on 10.11.17.
 */
'use strict'

angular.module('dashboardJsApp')
    .directive('modalDialogStaffAddReferents', ['Staff', 'Modal', '$cookieStore',
        function(Staff, Modal, $cookieStore) {
            return {
                restrict: 'EA',
                templateUrl: 'app/staff/addreferents/staffAddReferents.html',

                transclude: true,
                link: function(scope, elem, attrs) {
                    var currentUser, currentReferent;
                    if ($cookieStore.get('user')) {
                      currentUser = $cookieStore.get('user');
                      currentUser = currentUser ? currentUser.id : '';

                      currentReferent = $cookieStore.get('referent');
                      currentReferent = currentReferent ? currentReferent.id : currentUser;
                    }

                    scope.referents = [];
                    scope.selectedReferent = {};
                    scope.referentLoading = false;

                    scope.showStafReferentModal = function() {
                        scope.showStafReferent = true;
                    };

                    scope.cancelReferent = function() {
                        scope.showStafReferent = false;
                        scope.referentLoading = false;
                        scope.referents = [];
                        scope.selectedReferent = {};
                    };

                    scope.foundReferent = function(sFind) {
                        scope.selectFilterHuman.params.sFind = sFind;
                        Staff.findForFilter(scope.selectFilterHuman).then(function (res) {
                            var response = [];
                            if (res && res.length) {
                              response = res;
                              angular.forEach(response, function (user) {
                                user.sName = user.sName;
                                user.sPosition = user.oSubjectHumanPositionCustom.sNote;
                                user.sId = user.sID_Group_Activiti;
                              })
                            }
                            scope.referents = response;

                        });
                    };

                    scope.saveStaffReferent = function() {
                        scope.referentLoading = 'Збереження...';
                        var paramsRef = {params: { 
                          sID_Group: scope.selectedReferent.s.sId, sLoginStaff: scope.currHuman.sID_Group_Activiti,
                          sLogin: currentUser,
                          sLoginReferent: currentReferent
                        }};
                        Staff.addReferentStaff(paramsRef).then(function (res) {

                          if (res.code === "BUSINESS_ERR" || res.code === 'SYSTEM_ERR') {
                            Modal.inform.error()(res.message);
                            scope.cancelReferent();
                            return;
                          }

                          if (!scope.currHuman.referents)
                                scope.currHuman.referents = [];
                            scope.currHuman.referents.push(scope.selectedReferent.s);

                          scope.cancelReferent();
                          Modal.inform.info()("Референт успішно доданий");
                        });
                    };

                    scope.removeStaffReferent = function (index) { // delete table string
                      var referentToRemove =  scope.currHuman.referents[index];

                      var paramsRef = {params: {
                        sID_Group_Activiti: referentToRemove.sId,
                        sLoginStaff: scope.currHuman.sID_Group_Activiti,
                        sLogin: currentUser,
                        sLoginReferent: currentReferent
                      }};

                      Staff.removeReferentStaff(paramsRef).then(function (res) {
                        if (res.code === "BUSINESS_ERR" || res.code === 'SYSTEM_ERR') {
                          Modal.inform.error()(res.message);
                          return;
                        }

                        scope.currHuman.referents.splice(index, 1);
                        Modal.inform.info()("Референта видалено успішно");
                      });
                      
                    };

                }

            };
        }]);
