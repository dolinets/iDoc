'use strict';

(function (angular) {
  var tasksSearchService = function (tasks, Modal, defaultSearchHandlerService, $location, $rootScope, iGovNavbarHelper, $route, $q, ValidationService) {

    var self = this;
    var messageMap = {'CRC Error': 'Неправильний ID', 'Record not found': 'ID не знайдено'};
    var oPreviousTextSearch = {
      value : '',
      onTab: '',
      cursor : 0,
      aIds : [],
      result : '',
      sortType: ''
    };

    var searchTaskByUserInput = function (value, tab, bSortReverse, goTo) {
      var defer = $q.defer();
      self.searchTaskByOrder(value, tab, bSortReverse, goTo).then(function (result) {
        defer.resolve(result);
      }, function (err) {
        self.searchTaskByText(value, tab, bSortReverse, defer);
      });
      return defer.promise;
    };

    var searchTaskByOrder = function (value, tab, bSortReverse, goTo) {
      var defer = $q.defer();
      var matches = value.match(/((\d+)-)?(\d+)/);
      if(matches && matches[3].length > 4 && ValidationService.validatorFunctionsByFieldId['OrderValue'](matches[3])){
        tasks.getTasksByOrder(matches[3], true).then(function (result) {
          if (messageMap.hasOwnProperty(result)){
            defer.reject();
          } else {
            cleanPreviousTextSearch();
            var aIds = JSON.parse(result);
            if (angular.isArray(aIds) && aIds.length > 0) {
              var params = {
                taskId : aIds[0],
                bSortReverse: bSortReverse
              };
              if (tab) {
                params.type = tab;
              }
              if(goTo) {
                params.goTo = goTo;
                params.onlyThisType = true;
              }
              if(aIds.length === 1){
                searchSuccess(params);
              }
              defer.resolve({
                aIDs : aIds,
                nCurrentIndex : 0
              });
            } else {
              defer.reject();
            }
          }
        }).catch(function () {
          defer.reject();
        })
      } else {
        defer.reject();
      }

      return defer.promise;
    };

    var searchTaskByText = function (value, tab, bSortReverse, defer, bDontGoToTask) {
      if(!defer){
        defer = $q.defer();
      }
      var oSearchParams = {
        taskId: null,
        type: tab,
        onlyThisType: true,
        bSortReverse: bSortReverse
      };
      if (oPreviousTextSearch.value === value && oPreviousTextSearch.onTab === tab){
        oSearchParams.taskId = getNextTaskId(oPreviousTextSearch.aIds, bSortReverse, false);
        if(!bDontGoToTask) searchSuccess(oSearchParams);
        defer.resolve({
          aIDs : oPreviousTextSearch.aIds,
          nCurrentIndex : oPreviousTextSearch.cursor
        });
      } else {
        cleanPreviousTextSearch();
        tasks.getTasksByText(value, tab)
          .then(function (result) {
            if (messageMap.hasOwnProperty(result)) {
              Modal.inform.error()(messageMap[result]);
              defer.reject();
            } else {
              var aIds = JSON.parse(result);
              if (angular.isArray(aIds) && aIds.length > 0) {
                if (oPreviousTextSearch.result === result) {
                  oPreviousTextSearch.onTab = tab;
                  oPreviousTextSearch.value = value;

                  oSearchParams.taskId = getNextTaskId(aIds, bSortReverse, false);
                  if(!bDontGoToTask) {
                    searchSuccess(oSearchParams);
                  }
                  defer.resolve({
                    aIDs : aIds,
                    nCurrentIndex : oPreviousTextSearch.cursor
                  });
                } else {
                  oPreviousTextSearch = {
                    value : value,
                    onTab : tab,
                    cursor : 0,
                    aIds: aIds,
                    result : result,
                    sortType: bSortReverse
                  };
                  oSearchParams.taskId = getNextTaskId(aIds, bSortReverse, true);
                  if(!bDontGoToTask) {
                    searchSuccess(oSearchParams);
                  }
                  defer.resolve({
                    aIDs : aIds,
                    nCurrentIndex : oPreviousTextSearch.cursor
                  });
                }
              } else {
                Modal.inform.error()('За даним критерієм задач не знайдено');
                defer.reject();
              }
            }
          }).catch(function (response) {
          cleanPreviousTextSearch();
          defaultSearchHandlerService.handleError(response, messageMap);
          defer.reject();
        });
      }
      return defer.promise;
    };

    function cleanPreviousTextSearch () {
      oPreviousTextSearch = {
        value : '',
        onTab: '',
        cursor : 0,
        aIds : [],
        result : '',
        sortType: ''
      };
    }

    function getNextTaskId(aIds, bSortReverse, isTheFirstIteration) {
      if(bSortReverse){
        oPreviousTextSearch.cursor--;
      } else {
        oPreviousTextSearch.cursor++;
      }
      if(oPreviousTextSearch.cursor < 0 || (isTheFirstIteration && bSortReverse)){
        oPreviousTextSearch.cursor = aIds.length - 1;
      }
      if(oPreviousTextSearch.cursor == aIds.length || (isTheFirstIteration && !bSortReverse)){
        oPreviousTextSearch.cursor = 0;
      }
      return aIds[oPreviousTextSearch.cursor];
    }

    var searchTypes = ['myDocuments','documents','unassigned','selfAssigned','ecp','viewed'];

    var searchSuccess = function (params) {
      if (!params.onlyThisType){
        params.onlyThisType = false;
      }
      if (!params.bSortReverse) {
        params.bSortReverse = false;
      }

      if (params.onlyThisType) {
        if (params.type) {
          searchTaskInType(params);
        } else {
          params.type = searchTypes[0];
          searchTaskInType(params);
        }
      } else {
        params.type = searchTypes[0];
        searchTaskInType(params);
      }
    };

    var searchTaskInType = function(params, page) {

      if (!page){
        page = 0;
      }
      tasks.list(params.type, {page: page}).then(function(response){
        var taskFound = false;

        for (var i = 0; i < response.aoTaskDataVO.length; i++) {
          if (response.aoTaskDataVO[i].id == params.taskId) {
            var taskID = params.taskId;
            var direction;

            if(params.goTo && params.goTo === 'next'){
              direction = 1;
            } else if (params.goTo && params.goTo === 'previous'){
              direction = -1;
            }

            if(params.goTo && response.aoTaskDataVO[i + direction]){
              taskID = response.aoTaskDataVO[i + direction].id;
            } else if (params.goTo && !response.aoTaskDataVO[i + direction].id && response.aoTaskDataVO.length > 1){
              taskID = response.aoTaskDataVO[0].id;
            }

            var newPath = '/tasks/sID_Order=' + taskID + '#' + params.type;
            if (newPath == $location.$$path){
              $route.reload();
              $rootScope.$broadcast("update-search-counter");
            } else {
              if (params.type === 'myDocuments' && $rootScope.isEdit){
                break;
              } else {
                $location.path(newPath);
              }
            }
            iGovNavbarHelper.load();
            taskFound = true;
            break;
          }
        }

        if (!taskFound) {
          if ((response.start + response.size) < response.total){
            searchTaskInType(params, page + 1);
          } else if (searchTypes.indexOf(params.type) < searchTypes.length - 1) {
            if (!params.onlyThisType) {
              params.type = searchTypes[searchTypes.indexOf(params.type) + 1];
              searchTaskInType(params, 0);
            }
          }
        }
      })
    };

    var searchNextTask = function (params, page) {
      var deferred = $q.defer();
      var searchParams = {params: params, page: page};
      var firstPage = [];

      (function searchNextTask(params, page, searchEnd) {
        tasks.list(params.type, {page: page}).then(function(response){
          if (searchEnd){
            deferred.resolve(response.aoTaskDataVO[0].id);
          } else if (page === 0) {
            firstPage = response;
          }

          var taskFound = false;
          for (var i = 0; i < response.aoTaskDataVO.length; i++) {
            if (response.aoTaskDataVO[i].id == params.taskId) {
              var taskID = response.aoTaskDataVO[i + 1];

              if (taskID){
                taskID = taskID.id;
              } else if (!taskID && response.aoTaskDataVO.length > 1 || !taskID && page > 0){
                taskID = firstPage.aoTaskDataVO[0].id;
              } else if (!taskID && response.aoTaskDataVO.length === 1 && page !== 0){
                searchNextTask(params, 0, true);
              }

              deferred.resolve(taskID);
              break;
            }
          }

          if (!taskFound) {
            if ((response.start + response.size) < response.total){
              searchNextTask(params, page + 1);
            } else{
              deferred.resolve();
            }
          }
        });
      })(searchParams.params, searchParams.page);

      return deferred.promise;
    };

    return {
      searchTaskByUserInput: searchTaskByUserInput,
      searchTaskByOrder: searchTaskByOrder,
      searchTaskByText: searchTaskByText,
      searchSuccess: searchSuccess,
      searchNextTask: searchNextTask
    }
  };

  tasksSearchService.$inject = ['tasks', 'Modal', 'defaultSearchHandlerService', '$location', '$rootScope', 'iGovNavbarHelper',
    '$route', '$q', 'ValidationService'];

  angular
    .module('dashboardJsApp')
    .factory('tasksSearchService', tasksSearchService);

})(angular);
