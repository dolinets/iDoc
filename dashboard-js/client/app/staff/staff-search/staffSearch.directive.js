'use strict';

angular.module('dashboardJsApp').directive('staffSearchSelect', ['$http', 'tasks', 'Auth', '$rootScope', '$timeout', 'DocumentsService', 'CurrentServer', 'Staff',
  function($http, tasks, Auth, $rootScope, $timeout, DocumentsService, CurrentServer, Staff) {
    return {
      restrict: 'EA',
      templateUrl: 'app/staff/staff-search/staffSearchSelect.html',
      controller: 'StaffCtrl',
      link: function (scope) {
        scope.subposition = {
          selected : null
        };
        scope.subdivision = {
          selected : null
        };
        scope.dataUsersPosit = {
          selected : null
        };
        scope.dataUsers = {
          selected : null
        };
        scope.dataReferent = {
          selected: null
        };
        scope.isMenuOpened = false


        var queryParams = {params:{}};
        var login = Auth.getCurrentUser();
        var taskServer = CurrentServer.getServer();

        var subjectUserFilter = function (arr) {
          var allUsers = [], filteredUsers = [], logins = [];

          (function loop(arr) {
            angular.forEach(arr, function(item) {
              if(item.aUser) {
                angular.forEach(item.aUser, function(user) {
                  allUsers.push(user);
                })

              }

              if(allUsers[allUsers.length-1] !== undefined){
                if(item.oSubjectHumanPositionCustom && item.oSubjectHumanPositionCustom.sNote && item.oSubjectHumanPositionCustom.sNote.length > 0){
                  allUsers[allUsers.length-1]['sPosition'] = item.oSubjectHumanPositionCustom.sNote;
                }
                if((typeof item.sName_SubjectGroupCompany === 'string') && item.sName_SubjectGroupCompany.length > 0){
                  allUsers[allUsers.length-1]['sCompany'] = item.sName_SubjectGroupCompany;
                }
                if((typeof item.sName === 'string') && item.sName.length > 0){
                  allUsers[allUsers.length-1]['sName'] = item.sName;
                }
                if((typeof item.oSubject.sLabel === 'string') && item.oSubject.sLabel.length > 0){
                  allUsers[allUsers.length-1]['sNameCompany'] = item.oSubject.sLabel;
                }
                if((typeof item.sID_Group_Activiti === 'string') && item.sID_Group_Activiti.length > 0){
                  allUsers[allUsers.length-1]['sID_Group_Activiti_Organ'] = item.sID_Group_Activiti;
                }

              }
              if(item.aSubjectGroupChilds && item.aSubjectGroupChilds.length > 0){
                loop(item.aSubjectGroupChilds)
              }
            })
          })(arr);

          for(var i=0; i<allUsers.length; i++) {
            if(logins.indexOf(allUsers[i].sLogin) === -1) {
              filteredUsers.push(allUsers[i]);
              logins.push(allUsers[i].sLogin);
            }
          }

          return filteredUsers;
        };

        scope.rootStaffCompany = function () {
          scope.spinner = true;

          if ($rootScope.tabMenu === 'position'){
            scope.selectRootCompany = {
              params: {
                sSubjectType: 'Organ',
                sID_Group_Activiti: scope.currentUser.id,
                nDeepLevel: 0
              }
            };

            $http.get('./api/staff/getSubjectGroupsTreeUp', scope.selectRootCompany).then(function (res) {
              if (res.data && res.data.constructor === Array && !res.data.length) {
                scope.spinner = false;
                return;
              }

              if (typeof res.data === 'object') {
                var response = res.data;
              }

              scope.rootGroupActiviti = response[0].sID_Group_Activiti;

              scope.selectFilterCompany = {
                params: {
                  sSubjectType: 'Organ',
                  sID_Group_Activiti_Company: scope.rootGroupActiviti
                }
              };
              scope.selectFullCompany = {
                params: {
                  sSubjectType: 'Organ',
                  sID_Group_Activiti: scope.rootGroupActiviti,
                  nDeepLevelWidth: 1,
                  bIncludeRoot: true
                }
              };
              scope.filterCompany = {
                params: {
                  sSubjectType: 'Organ',
                  sID_Group_Activiti_Company: scope.rootGroupActiviti
                }
              };
              scope.foundTreeMenuList();
            });
          }else if ($rootScope.tabMenu === 'employee'){
            scope.selectRootCompany = {
              params: {
                sSubjectType: 'Organ',
                sID_Group_Activiti: scope.currentUser.id,
                nDeepLevel: 1
              }
            };

            $http.get('./api/staff/getSubjectGroupsTreeUp', scope.selectRootCompany).then(function (res) {
              if (res.data && res.data.constructor === Array && !res.data.length) {
                scope.spinner = false;
                return;
              }

              if (typeof res.data === 'object') {
                var response = res.data;
              }
              scope.rootGroupActiviti = response[0].sID_Group_Activiti;

              scope.selectHuman = {
                params: {
                  sSubjectType: 'Human',
                  sID_Group_Activiti: scope.rootGroupActiviti,
                  nDeepLevel: 1
                }
              };
              scope.selectFilterHuman = {
                params: {
                  sSubjectType: 'Human',
                  sID_Group_Activiti_Company: scope.rootGroupActiviti

                }
              };
              scope.filterCompany = {
                params: {
                  sSubjectType: 'Organ',
                  sID_Group_Activiti_Company: scope.rootGroupActiviti
                }
              };
              scope.foundTreeMenuList();
            });
          }
        }

        scope.foundUsers = function (sFind) { // filter for main search to Human
          if(scope.usersList){
            delete scope.usersList;
          }
          if($rootScope.tabMenu === 'employee') {
            scope.selectFilterHuman.params.sFind = sFind;
            Staff.findForFilter(scope.selectFilterHuman).then(function (res) {
              var response = res.map(function(user) {
                if (!user.oSubject.oSubjectStatus || (user.oSubject.oSubjectStatus && user.oSubject.oSubjectStatus.sName !== 'Dismissed') ) {
                  user.sName = user.sName;
                  user.sPosition = user.oSubjectHumanPositionCustom.sNote;
                  user.sCompany = user.sName_SubjectGroupCompany;

                  return user;
                }
              });
              /*angular.forEach(response, function (user) {
                user.sName = user.sName;
                user.sPosition = user.oSubjectHumanPositionCustom.sNote;
                user.sCompany = user.sName_SubjectGroupCompany;
              })*/

              scope.usersList = response;
            });
          }else if($rootScope.tabMenu === 'position') { // // filter for main search to Organ
            scope.selectFilterCompany.params.sFind = sFind;
            Staff.findForFilter(scope.selectFilterCompany).then(function (res) {
              var response = res.filter(function(item) {
                return !item.oSubject.oSubjectStatus || (item.oSubject.oSubjectStatus && item.oSubject.oSubjectStatus.sName !== 'Inactive');
              });

              /*angular.forEach(response, function (user) {
                user.sName = user.sName;
                user.sCompany = user.sCompany;
                user.sPosition = user.sPosition;
              })*/


              scope.usersListPosit = response;

            });
          }
        }

        scope.foundTreeMenuList = function () { // get data for tree-menu to Human
          if($rootScope.tabMenu === 'employee') {
            $http.get('./api/subject-role', scope.selectHuman).then(function (res) {
              if (typeof res.data === 'object') {
                var response = res.data.aSubjectGroupTree;
              }
              scope.humanList = response;
              scope.spinner = false;

            });

            $http.get('./api/subject/getSubjectStatus', {params: {sName_SubjectType: 'Human'}}).then(function(res) {
              scope.userStatuses = res.data;
            });
          }else if($rootScope.tabMenu === 'position') { // get data for tree-menu to Organ
            $http.get('./api/subject-role', scope.selectFullCompany).then(function (res) {
              if (typeof res.data === 'object') {
                var response = res.data.aSubjectGroupTree;
              }

              scope.departList = response;
              scope.spinner = false;

            });

            $http.get('./api/subject/getSubjectStatus', {params: {sName_SubjectType: 'Organ'}}).then(function(res) {
              scope.userStatuses = res.data;
            });
          }
        }


        scope.getCompany = function (attr, $index, position) {  // get current Company model
          $rootScope.newComp = false;
          try {
            document.getElementById('communication').checked = false;
          } catch(e) {}

          scope.currPosition = {
            "position": position.sName,
            "login": position.sID_Group_Activiti

          };
          var parentStaffCompany = {params: {sSubjectType: 'Organ', sID_Group_Activiti: position.sID_Group_Activiti, nDeepLevel: 1}};
          $http.get('./api/staff/getSubjectGroupsTreeUp', parentStaffCompany).then(function (res) {
            if (typeof res.data === 'object') {
              var response = res.data;
            }
            if(response.length > 0){
              scope.currPosition.parent = response[0].sName;
              scope.currPosition.root_login = response[0].sID_Group_Activiti;
            }

          });

          scope.currPosition.status = position.oSubject.oSubjectStatus || '';

          // console.log(scope.currPosition);
        };

        scope.getCompanyFromFilter = function (attr, $index, position) {  // get current Company model
          $rootScope.newComp = false;
          try {
            document.getElementById('communication').checked = false;
          } catch(e) {}

          if(scope.dataUsersPosit.selected) {
            scope.currPosition = {
              "position": scope.dataUsersPosit.selected.sName,
              "login": scope.dataUsersPosit.selected.sID_Group_Activiti

            };
            var parentStaffCompany = {
              params: {
                sSubjectType: 'Organ',
                sID_Group_Activiti: scope.dataUsersPosit.selected.sID_Group_Activiti,
                nDeepLevel: 1
              }
            };
            $http.get('./api/staff/getSubjectGroupsTreeUp', parentStaffCompany).then(function (res) {
              if (typeof res.data === 'object') {
                var response = res.data;
              }
              if(response.length > 0){
                scope.currPosition.parent = response[0].sName;
                scope.currPosition.root_login = response[0].sID_Group_Activiti;
              }
            });

            scope.currPosition.status = scope.dataUsersPosit.selected.oSubject.oSubjectStatus || '';
            
            scope.dataUsersPosit.selected = null;
          }
          //console.log(scope.currPosition);
        };

        scope.getSelectedUser = function () {
          if(scope.dataUsers.selected){
            scope.subposition.selected = {sNote: scope.dataUsers.selected.sPosition};
            scope.subdivision.selected = {sName: scope.dataUsers.selected.sCompany};
          }
        }

        scope.getHumanFromFilter = function () {
          $rootScope.newHuman = false;
          scope.referentList = [];
          var finishCount = 0;

          if(scope.dataUsers.selected){
            scope.disableAllInputs(true);

            var partsOfname = {};
            $http.get('./api/staff/getSubjectGroupsTreeUp', {params: {sSubjectType: 'Organ', sID_Group_Activiti: scope.dataUsers.selected.sID_Group_Activiti, nDeepLevel: 0}})
              .then(function (res) {
                if (res.data) {
                  var response = res.data;
                  scope.sID_Group_Activiti_Organ = response[0].sID_Group_Activiti;
                  //scope.positionID = response[0].oSubjectHumanPositionCustom.sName;
                  //scope.sCompany = response[0].sName;
                }
                finishCount++;
              });

            Staff.getReferentStaff(scope.dataUsers.selected.sID_Group_Activiti).then(function (res) {

              if (res) {
                var response = res.mUserGroupMember;
                scope.phone = res.oSubjectHuman.oDefaultPhone ? res.oSubjectHuman.oDefaultPhone.sValue : '';
                scope.email = res.oSubjectHuman.oDefaultEmail ? res.oSubjectHuman.oDefaultEmail.sValue : '';
                scope.sCompany = res.aSubjectGroupTreeUp[0].sName;
                scope.sID_Group_Activiti_Depart = res.aSubjectGroupTreeUp[0].sID_Group_Activiti;
                scope.position = res.oSubjectGroup.oSubjectHumanPositionCustom;
                if (!scope.checkBoss)
                  scope.checkBoss = {};
                scope.checkBoss.value = res.bHead ? res.bHead : false;
                angular.forEach(response, function (user) {
                  user.sName = user.sFio;

                })

                partsOfname = {
                  sFamily: res.oSubjectHuman.sFamily,
                  sName: res.oSubjectHuman.sName,
                  sSurname: res.oSubjectHuman.sSurname
                };

                scope.referListCur = response;

                scope.status = res.oSubjectGroup.oSubject.oSubjectStatus;
                //console.log(scope.contactHuman, response);
              }

              finishCount++;
            });

            Staff.getUserGroupMember(scope.dataUsers.selected.sID_Group_Activiti).then(function (res) {
              if (res.code === "SYSTEM_ERR") {
                Modal.inform.error()(res.message);
              } else if (res.length) {
                scope.referentList = res.filter(function(item) {
                  return item.sLogin !== scope.dataUsers.selected.sID_Group_Activiti;
                });

                angular.forEach(scope.referentList, function(item, i) {
                  getUserDetails(item, i);
                });
                
              }

              finishCount++;
            }, function (err) {
              Modal.inform.error()(JSON.parse(err).message);
            });

            Staff.getSubjectRightBP(scope.dataUsers.selected.sID_Group_Activiti).then(function(res) {
              scope.currRights = [];
              if (res && res.length) 
                angular.forEach(res, function(item) {
                  scope.currRights.push({sID: item.oSubjectRightBP.sID_BP, sName: item.sName_BP});
                });
            });

            var interval = setInterval(function() {

              /*angular.forEach(scope.contactHuman, function (contact) {
                if(contact.subjectContactType.sName_EN === 'Email'){
                  scope.email = contact.sValue;
                }else if(contact.subjectContactType.sName_EN === 'Phone'){
                  var phone = contact.sValue.split('-');
                  if (phone.length > 0){
                    scope.phone = '+38' + phone[0] + phone[1] + phone[2]  + phone[3];
                  }else {
                    scope.phone = phone;
                  }
                  if (scope.phone.indexOf('+380') > -1)
                    scope.phone = scope.phone.substring(4);
                }
              })*/
              if (finishCount < 3) return;

              clearInterval(interval);

              document.getElementById('communication').checked = scope.checkBoss.value;

              scope.phone = scope.phone ? scope.phone.replace(/^\s*\+*380*/, '') : '';

              scope.currHuman = {
                "firstname": partsOfname.sName,
                "secondname": partsOfname.sSurname,
                "lastname": partsOfname.sFamily,
                "position": scope.position.sNote,
                "positionID": scope.position.sName,
                "login": scope.dataUsers.selected.sID_Group_Activiti,
                "status": scope.status || '',
                "email": scope.email || '',
                "depart": scope.sCompany || scope.dataUsers.selected.sCompany,
                "sID_Group_Activiti": scope.dataUsers.selected.sID_Group_Activiti,
                "sID_Group_Activiti_Organ": scope.sID_Group_Activiti_Organ,
                "phone": scope.phone || '',
                "rights": scope.currRights,
                "referents": scope.referentList

              };
              scope.dataUsers.selected = null;
              $rootScope.currentStaffUser_sID_Group_Activiti = scope.currHuman.sID_Group_Activiti;

              scope.disableAllInputs(false);
              scope.subposition.selected = {sNote: scope.currHuman.position, sName: scope.currHuman.positionID};
              scope.subdivision.selected = {sName: scope.currHuman.depart, sID_Group_Activiti_Organ: scope.sID_Group_Activiti_Depart};
              scope.$apply();

            }, 1000);
            //scope.Employeespinner = false;
          }
        }

        function getUserDetails(item, i) {

          Staff.getReferentStaff(item.sLogin).then(function (res) {
            if (res && res.code !== 'SYSTEM_ERR' && res.oSubjectGroup) {
              var response = res.oSubjectGroup;
              response.sPosition = response.oSubjectHumanPositionCustom.sNote;
              response.sId = response.sID_Group_Activiti;
              
              scope.referentList[i] = response;
            }

          });
        }

        scope.getHuman = function (attr, $index, human) {   // get current Human model
          scope.currHuman = {};
          $rootScope.newHuman = false;
          scope.referentList = [];

          var finishCount = 0;

          scope.disableAllInputs(true);

          var login = human.sID_Group_Activiti;

          var partsOfname = {};
          $http.get('./api/staff/getSubjectGroupsTreeUp', {params: {sSubjectType: 'Organ', sID_Group_Activiti: login, nDeepLevel: 0}})
            .then(function (res) {
              if (res.data) {
                var response = res.data;
                scope.departCurrentUser = response[0].sName;
                scope.departID = response[0].nID;
                scope.sID_Group_Activiti_Organ = response[0].sID_Group_Activiti;
              }
              $rootScope.userStaffLogin = login;
              finishCount++;
            });

          Staff.getReferentStaff(login).then(function (res) {

            if (res) {
              var response = res.mUserGroupMember;
              scope.phone = res.oSubjectHuman.oDefaultPhone ? res.oSubjectHuman.oDefaultPhone.sValue : '';
              scope.email = res.oSubjectHuman.oDefaultEmail ? res.oSubjectHuman.oDefaultEmail.sValue : '';
              scope.sCompany = res.aSubjectGroupTreeUp[0].sName;
              scope.sID_Group_Activiti_Depart = res.aSubjectGroupTreeUp[0].sID_Group_Activiti;
              scope.position = res.oSubjectGroup.oSubjectHumanPositionCustom;
              if (!scope.checkBoss)
                scope.checkBoss = {};
              scope.checkBoss.value = res.bHead ? res.bHead : false;
              angular.forEach(response, function (user) {
                user.sName = user.sFio;
                // scope.dataReferent[$index] = {sName: user.sName};
              })

              partsOfname = {
                sFamily: res.oSubjectHuman.sFamily,
                sName: res.oSubjectHuman.sName,
                sSurname: res.oSubjectHuman.sSurname
              };

              scope.referListCur = response;

              scope.status = res.oSubjectGroup.oSubject.oSubjectStatus;
              // scope.referentList = response;
            }

            finishCount++;

            //console.log(scope.referListCur, scope.referentList , scope.dataReferent[$index]);
          });

          Staff.getUserGroupMember(human.sID_Group_Activiti).then(function (res) {
            if (res.code === "SYSTEM_ERR") {
              Modal.inform.error()(res.message);
            } else if (res.length) {
              scope.referentList = res.filter(function(item) {
                return item.sLogin !== human.sID_Group_Activiti;
              });

              angular.forEach(scope.referentList, function(item, i) {
                getUserDetails(item, i);
              });
              
            }
            finishCount++;
          }, function (err) {
            Modal.inform.error()(JSON.parse(err).message);
          });

          Staff.getSubjectRightBP(human.sID_Group_Activiti).then(function(res) {
            scope.currRights = [];
            if (res && res.length) 
              angular.forEach(res, function(item) {
                scope.currRights.push({sID: item.oSubjectRightBP.sID_BP, sName: item.sName_BP});
              });
          });

          var interval = setInterval(function() {
            /*angular.forEach(scope.contactHuman, function (contact) {
              if(contact.subjectContactType.sName_EN === 'Email' && !scope.email){
                scope.email = contact.sValue;
              }else if(contact.subjectContactType.sName_EN === 'Phone' && !scope.phone){
                var phone = contact.sValue.split('-');
                if (phone.length > 0){
                  scope.phone = phone[0] + phone[1] + phone[2]  + phone[3];
                }else {
                  scope.phone = phone;
                }
                if (scope.phone.indexOf('+380') > -1)
                  scope.phone = scope.phone.substring(4);
              }
            })*/
            if (finishCount < 3) return;

            clearInterval(interval);

            document.getElementById('communication').checked = scope.checkBoss.value;

            scope.phone = scope.phone ? scope.phone.replace(/^\s*\+*380*/, '') : '';

            scope.currHuman = {
              "firstname": partsOfname.sName,
              "secondname": partsOfname.sSurname,
              "lastname": partsOfname.sFamily,
              "position": scope.position.sNote,
              "positionID": scope.position.sName,
              "login": login || '',
              "status": scope.status || '',
              "email": scope.email || '',
              "depart": scope.sCompany || scope.departCurrentUser,
              "phone": scope.phone || '',
              "sID_Group_Activiti": human.sID_Group_Activiti,
              "sID_Group_Activiti_Organ": scope.sID_Group_Activiti_Organ,
              "departID": scope.departID,
              "rights": scope.currRights,
              "referents": scope.referentList
            };

            scope.disableAllInputs(false);

            scope.subposition.selected = {sNote: scope.currHuman.position, sName: scope.currHuman.positionID};
            scope.subdivision.selected = {sName: scope.currHuman.depart, sID_Group_Activiti_Organ: scope.sID_Group_Activiti_Depart};
            $rootScope.currentStaffUser_sID_Group_Activiti = scope.currHuman.sID_Group_Activiti;
            scope.$apply();
            // console.log( scope.currHuman,  scope.subposition.selected);
          }, 1000);
          //scope.Employeespinner = false;


        };

        scope.disableAllInputs = function(condition) {
          var form = document.getElementsByName('employeeForm')[0];
          if (form) {
            if (condition)
              form.children[0].scrollIntoView(true);

            angular.forEach(form.elements, function(element) {
              if ('status login dataReferent'.indexOf(element.id) < 0)
                element.disabled = condition;
            });
          }
            
          scope.Employeespinner = condition;
        }


        // start make tree-menu
        scope.toggle = function(scope) {
          scope.toggle();
        };

        scope.moveLastToTheBeginning = function() {
          var a = scope.companyList.pop();
          scope.companyList.splice(0, 0, a);
        };

        scope.expandAll = function() {
          scope.$broadcast('angular-ui-tree:expand-all');
        };

        scope.collapseAll = function() {
          console.log(scope.humans);
          scope.$broadcast('angular-ui-tree:collapse-all');
        };
      }

      // stop make tree-menu
    };
  }]);
