'use strict';

angular.module('dashboardJsApp')
  .directive('searchBox', ['tasksSearchService', 'iGovNavbarHelper', '$rootScope', '$stateParams', '$state', 'tasks', '$location', 'Tab',
    'searchService', '$http', 'Auth', 'SearchBox', '$modal', '$cookies',
    function (tasksSearchService, iGovNavbarHelper, $rootScope, $stateParams, $state, tasks, $location, Tab,
              searchService, $http, Auth, SearchBox, $modal, $cookies) {
      return {
        restrict: 'EA',
        templateUrl: 'components/search-box/search-box.template.html',
        link: function (scope, element, attrs, ngModel) {
          scope.searchbox = {
            selected: null
          };

          scope.openedSelectWithLeaving = {};
          $('.idoc-search-dropdown').click(function (e) {
            e.stopPropagation();
            if (Object.keys(scope.openedSelectWithLeaving).length > 0) {
              scope.openedSelectWithLeaving.close();
              scope.openedSelectWithLeaving = {};
            }
          });
          $('.close-search-box').on('click', function (e) {
            $('.idoc-search-dropdown').parent().removeClass('open');
          });

          scope.onClick = function ($select) {
            $select.searchInput.on('blur', function () {
              scope.openedSelectWithLeaving = $select;
            });
          }

          scope.openModal = function () {
            $modal.open({
              animation: true,
              templateUrl: 'components/search-box/saveFilter.modal.html',
              controller: 'SaveFilterModalCtrl',
              scope: scope
            });
          };

          scope.dateDisabled = true;

          var type = $stateParams.type ? $stateParams.type : $location.hash();

          var user;
          if (JSON.parse($cookies.get('user'))) {
            user = JSON.parse($cookies.get('user'));
          }

          var bSelectedTasksSortReverse = false;
          scope.iGovNavbarHelper = iGovNavbarHelper;

          scope.$on('set-sort-order-reverse-true', function () {
            bSelectedTasksSortReverse = true;
          });

          scope.$on('set-sort-order-reverse-false', function () {
            bSelectedTasksSortReverse = false;
          });

          scope.searchInputKeyup = function ($event) {
            if ($event.keyCode === 13 && $rootScope.tasksSearch.value) {
              runSearchingProcess();
            }
            if ($event.keyCode === 8 || $event.keyCode === 46) {
              if ($rootScope.tasksSearch.value === '') {
                $state.go('tasks.typeof', {
                  tab: Tab.getCurrentTab(type),
                  type: type
                });
              }
              scope.switchArchive = false;
            }
          };

          var prepareSearch = function(val) {
            var res = val.replace(/\s/g, "");
            res = res.split('-');

            if ( isNaN(parseInt(res[0])) )
              res[0] = res[0].slice(1);

            return res.join('-');
          };

          function runSearchingProcess() {
            if (/\d{1,3}-[0-9]+\d/.test($rootScope.tasksSearch.value)) {
              $rootScope.spinner = true;
              searchService.searchByOrder( prepareSearch($rootScope.tasksSearch.value) );
            } else {
              $rootScope.spinner = true;
              tasksSearchService.searchTaskByOrder( prepareSearch($rootScope.tasksSearch.value), type, bSelectedTasksSortReverse).then(function (res) {
                if (res.aIDs.length > 1) {
                  filterTasksList(res.aIDs);
                }
              }, function (err) {
                tasksSearchService.searchTaskByText( prepareSearch($rootScope.tasksSearch.value), type, bSelectedTasksSortReverse, false, true).then(function (res) {
                  filterTasksList(res.aIDs);
                }, function (err) {
                  $rootScope.spinner = false;
                });
              });
            }
          }

          function filterTasksList(aTasksIDs) {
            tasks.listByTasksIDs(type, aTasksIDs).then(function (result) {
              $rootScope.filteredTasks = angular.copy(result);
              $rootScope.tasks = angular.copy(result);
              $rootScope.spinner = false;
            });
          }

          scope.runSearch = function () {
            if ($rootScope.tasksSearch.value === '') {
              $state.go('tasks.typeof', {
                tab: Tab.getCurrentTab(type),
                type: type
              });
            } else {
              runSearchingProcess();
            }
          };

          scope.options = {};

          scope.prepareAdditionalParams = function (params) {
            console.log(params);
            var returnValue = [];
            for (var i = 0; i < params.length; i++) {
              if (params[i].sID_Field !== undefined && params[i].sID_Field !== null) {
                angular.forEach(params[i], function (value, key) {
                  if (key === 'sID_Field') {
                    returnValue.push({
                      sID_Field: params[i].sID_Field.sID,
                      sCondition: params[i].sCondition,
                      sValue: params[i].sValue
                    });

                    angular.forEach(scope.listOfFieldsOriginal, function(item) { //add hiden params to search req
                      if (item.sName === params[i].sID_Field.sName && item.sID !== params[i].sID_Field.sID)
                        returnValue.push({
                          sID_Field: item.sID,
                          sCondition: params[i].sCondition,
                          sValue: params[i].sValue
                        });
                    });
                  }
                });
              }
            }
            return returnValue;
          };

          scope.getBPFields = function (sID) {
            SearchBox.getBpFields(sID).then(function (res) {
              scope.showBPfields = true;
              scope.listOfFields = [];
              scope.listOfFieldsOriginal = JSON.parse(res);

              angular.forEach(scope.listOfFieldsOriginal, function(val) {
                if (!scope.listOfFields.some(function(item) {
                  return item.sName === val.sName;
                }) )
                  scope.listOfFields.push(val);
              });

            });
          };

          scope.refreshAuthorList = function (sFind) {
            SearchBox.getCurrentUserCompany().then(function (res) {
              if (res && res[0] && res[0].sChain) {
                scope.company = res[0].sChain;
                SearchBox.refreshAuthorList(scope.company, sFind).then(function (res) {
                  var response = subjectUserFilter(res.aSubjectGroupTree);
                  angular.forEach(response, function (user) {
                    user.sName = user.sFirstName + " " + user.sLastName;
                    user.sCompany = user.sCompany;
                    user.sPosition = user.sPosition;
                  });
                  scope.searchUsersList = response;
                });
              }
            });
          };

          scope.clearUserList = function () {
            scope.searchUsersList = null;
          };

          scope.searchByParams = function (form) {
            var startDate, endDate;
            if (scope.options.startDate) {
              startDate = new Date(scope.options.startDate).getTime();
            }
            if (scope.options.endDate) {
              endDate = new Date(scope.options.endDate).getTime();
            }
            if (startDate && endDate && startDate > endDate) {
              scope.dateError = true;
              return;
            } else {
              scope.dateError = false;
            }

            var preparedParams = scope.prepareAdditionalParams(scope.aoFilterField);
            if (form.$valid) {
              $rootScope.spinner = true;
              var dateOption;
              if (!scope.options.dateOption) {
                if ($rootScope.tabMenu === 'tasks') {
                  dateOption = 'executionTime';
                }
              } else {
                dateOption = scope.options.dateOption;
              }
              var data = {
                sLogin: user.id,
                sLoginAuthor: scope.options.author ? scope.options.author.sLogin : null,
                sDateType: scope.options.dateOption,
                sDateFrom: scope.options.startDate,
                sDateTo: scope.options.endDate,
                sProcessDefinitionKey: scope.options.docTemplate ? (scope.options.docTemplate.sID === 'all' ?
                  null : scope.options.docTemplate.sID) : null,
                sLoginController: scope.options.controller ? scope.options.controller.sLogin : null,
                sLoginExecutor: scope.options.executor ? scope.options.executor.sLogin : null,
                sFind: scope.options.searchText,
                sFilterStatus: scope.options.tabSelect === 'Executed' ? 'ExecutionFinished' : 
                  scope.options.tabSelect === 'Controled' ? 'ControlFinished' : scope.options.tabSelect,
                bIncludeDeleted: false,
                bSearchExternalTasks: false,
                aoFilterField: preparedParams.length > 0 ? preparedParams : null,
              }
              SearchBox.searchBoxTasks(data).then(function (res) {
                $rootScope.filteredTasks = angular.copy(res.aoTaskDataVO);
                $rootScope.tasks = angular.copy(res.aoTaskDataVO);
                $rootScope.searchCounter = res.total;
                $rootScope.spinner = false;
              }).finally(function () {
                $rootScope.spinner = false;
              });
            }
          };

          scope.setCurrentUserAsAuthor = function () {
            var currentUser = user;
            scope.options.author = {
              sLogin: currentUser.id,
              sName: currentUser.firstName + ' ' + currentUser.lastName
            };
          };

          scope.setCurrentUserAsExecutor = function () {
            var currentUser = user;
            scope.options.executor = {
              sLogin: currentUser.id,
              sName: currentUser.firstName + ' ' + currentUser.lastName
            };
          };

          scope.setCurrentUserAsController = function () {
            var currentUser = user;
            scope.options.controller = {
              sLogin: currentUser.id,
              sName: currentUser.firstName + ' ' + currentUser.lastName
            };
          };

          var oTabName = {
            Control: 'control',
            OpenedAssigned: 'selfAssigned',
            OpenedUnassigned: 'unassigned',
            Closed: 'finished',
            DocumentOpenedUnassignedUnprocessed: 'documents',
            DocumentOpenedUnassignedWithoutECP: 'ecp',
            DocumentOpenedUnassignedProcessed: 'viewed',
            OpenedCreatorDocument: 'myDocuments',
            DocumentClosed: 'docHistory',
            Execution: 'execution'
          };

          scope.setCurrentTab = function (tab, tabMenu) {
            SearchBox.getListOfAllBpForLogin(tabMenu === 'documents').then(function (res) {
              scope.listOfBpsForLogin = JSON.parse(res);
              scope.listOfBpsForLogin.push({sID: 'all', sName: "Усі шаблони"});
            });
            angular.forEach(oTabName, function (key, value) {
              if (key === tab) {
                scope.options.tabSelect = value;
              }
            });
          };

          scope.enableDate = function () {
            scope.dateDisabled = false;
          };

          scope.aoFilterField = [];

          scope.addParam = function () {
            scope.aoFilterField.push({sID_Field: null, sCondition: null, sValue: null});
          };

          scope.removeParam = function (index) {
            scope.aoFilterField.splice(index, 1);
          };

          // $rootScope.listOfFilters = [
          //   {title: 'Длинное название фильтра'},
          //   {title: 'Длинное название фильтра 2'}];

          $rootScope.setCustomFilter = function (filter) {
            $rootScope.currentFilter = filter;
          };

          $rootScope.clearAllFilterFields = function () {
            angular.forEach(scope.options, function (value, key) {
              scope.options[key] = null;
            });
            scope.aoFilterField = [];
          };

          var subjectUserFilter = function (arr) {
            var allUsers = [], filteredUsers = [], logins = [];
            (function loop(arr) {
              angular.forEach(arr, function (item) {
                if (item.aUser) {
                  angular.forEach(item.aUser, function (user) {
                    allUsers.push(user);
                  });
                }
                if (allUsers[allUsers.length - 1] !== undefined) {
                  if (item.oSubjectHumanPositionCustom && item.oSubjectHumanPositionCustom.sNote && item.oSubjectHumanPositionCustom.sNote.length > 0) {
                    allUsers[allUsers.length - 1].sPosition = item.oSubjectHumanPositionCustom.sNote;
                  }
                  if ((typeof item.sName_SubjectGroupCompany === 'string') && item.sName_SubjectGroupCompany.length > 0) {
                    allUsers[allUsers.length - 1].sCompany = item.sName_SubjectGroupCompany;
                  }
                }
                if (item.aSubjectGroupChilds && item.aSubjectGroupChilds.length > 0) {
                  loop(item.aSubjectGroupChilds);
                }
              });
            })(arr);
            for (var i = 0; i < allUsers.length; i++) {
              if (logins.indexOf(allUsers[i].sLogin) === -1) {
                filteredUsers.push(allUsers[i]);
                logins.push(allUsers[i].sLogin);
              }
            }
            return filteredUsers;
          };
        }
      };
    }])
;
