(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('TasksCtrl', tasksCtrl);

  tasksCtrl.$inject = [
    '$scope', 'tasks', 'processes', 'Modal', 'identityUser', '$localStorage', '$filter', 'lunaService',
    'taskFilterService', 'defaultSearchHandlerService', '$rootScope', '$location', 'Tab', '$stateParams', '$q', '$timeout',
    '$state', 'tasksStateModel', 'stateModel', 'Auth', 'iGovNavbarHelper', 'snapRemote', '$http', 'CurrentServer', 'SearchBox',
  ];
  function tasksCtrl($scope, tasks, processes, Modal, identityUser, $localStorage, $filter, lunaService,
                     taskFilterService, defaultSearchHandlerService, $rootScope, $location, Tab, $stateParams, $q, $timeout,
                     $state, tasksStateModel, stateModel, Auth, iGovNavbarHelper, snapRemote, $http, CurrentServer, SearchBox) {

    CurrentServer.init();
    $rootScope.searchCounter = null;

    $scope.helpDeskScriptExecutionCondition = function () {
      return (Auth.isLoggedIn() && !document.getElementById('atlwdg-trigger') && $rootScope.bHelpDesk === "TRUE");
    };

    $scope.loadHelpDeskScript = function () {
      var script = document.createElement("script");
      script.src = "https://idoc-develop.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e-T/-pg4gku/b/8/a44af77267a987a660377e5c46e0fb64/" +
        "_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/" +
        "com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=ru-RU&collectorId=761b5a9b";
      script.type = "text/javascript";
      document.head.appendChild(script);
      initHelpDesk();
    };

    function initHelpDesk(){
      var defaultComponent;
      var chainToComponent = {
        'https://doc.btsol.com.ua': '10174',
        'https://coorg-pgok.idoc.com.ua': '10177',
        'https://coorg-nzf.idoc.com.ua': '10175',
        'https://coorg-zfz.idoc.com.ua': '10176',
        'https://coorg-mgok.idoc.com.ua': '10178',
        'https://coorg-azot.idoc.com.ua': '10180',
        'https://coorg-agro.idoc.com.ua': '10183',
        'https://coorg-tgok.idoc.com.ua': '10179',
        'https://coorg-bvg.idoc.com.ua': '10181',
        'https://idoc-nfm.test.region.igov.org.ua': '10182',
        'https://coorg-autopapa.idoc.com.ua': '10185',
        'https://coorg-peoplenet.idoc.com.ua': '10184'
      };

      angular.forEach(chainToComponent, function (value, key) {
        if (key.indexOf(window.location.hostname) > -1) {
          defaultComponent = value;
        }
      });

      window.ATL_JQ_PAGE_PROPS = $.extend(window.ATL_JQ_PAGE_PROPS, {
        triggerFunction : function( showCollectorDialog ) {
          $('#feedback-button').on( 'click', function(e) {
            e.preventDefault();
            showCollectorDialog();
          });
        },
        fieldValues: {
          customfield_10040 : Auth.getCurrentUser().id,
          customfield_10039 : Auth.getCurrentUser().firstName + ' ' + Auth.getCurrentUser().lastName,
          customfield_10041 : window.location.protocol + '\/\/' + window.location.hostname,
          email : Auth.getCurrentUser().email && Auth.getCurrentUser().email === 'null' ? '' : Auth.getCurrentUser().email,
          components : defaultComponent ? [defaultComponent] : null,
          priority : '3'
        }
      });
    };

    var sort = $rootScope.sortDirection  = localStorage.getItem($stateParams.type);
    $scope.changeSortDirection = function (tab, direction) {
      localStorage.setItem(tab, direction);
      sort = $rootScope.sortDirection  = localStorage.getItem($stateParams.type);
      $state.reload();
    };

    $rootScope.tasks = null;
    $rootScope.spinner = false;
    $scope.sSelectedTask = $stateParams.type;
    $scope.iGovNavbarHelper = iGovNavbarHelper;
    iGovNavbarHelper.loadTaskCounters();
    iGovNavbarHelper.loadTaskCounters('documents');

    $scope.printModalState = {show: false}; // wrapping in object required for 2-way binding
    $scope.taskDefinitions = taskFilterService.getTaskDefinitions();
    $scope.model = stateModel;
    $scope.userProcesses = taskFilterService.getDefaultProcesses();
    $scope.model.userProcess = $scope.userProcesses[0];
    if (!$rootScope.tasksOrders)
      $rootScope.tasksOrders = {};

    $scope.bSelectAllForEDS = false;
    var nSelectedTaskForEdsCount = 0;

    if ($state.is("tasks.typeof") && !$stateParams.tab && !$stateParams.type){
      if ($rootScope.ProjectRegion_StartView_bDocuments === 'TRUE'){
        $state.go("tasks.typeof", {tab: 'tasks', type: 'myDocuments'});
      } else {
        $state.go("tasks.typeof", {tab: 'tasks', type: 'unassigned'});
      }
    }

    $scope.selectForEDS = function (task) {
      if(task.bSelectedForEDS){
        nSelectedTaskForEdsCount++;
      } else {
        nSelectedTaskForEdsCount--;
      }
      $scope.bSelectAllForEDS = !!($rootScope.filteredTasks && (nSelectedTaskForEdsCount == $rootScope.filteredTasks.length));
    };
    $scope.selectAllForEDS = function (value) {
      angular.forEach($rootScope.filteredTasks, function (task) {
        task.bSelectedForEDS = value;
      });
      if(value){
        nSelectedTaskForEdsCount = $rootScope.filteredTasks.length;
      } else {
        nSelectedTaskForEdsCount = 0;
      }
    };

    $scope.signAll = function () {
      if(!$rootScope.filteredTasks || !angular.isArray($rootScope.filteredTasks) || $rootScope.filteredTasks.length < 1) return;
      var aTasksForSigning = $rootScope.filteredTasks.filter(function (task) {
        return task.bSelectedForEDS;
      });
      if(aTasksForSigning.length < 1) return;
      angular.forEach(aTasksForSigning, function (task) {
        angular.forEach(task.globalVariables, function (field, key) {
          try {
            task.globalVariables[key] = angular.fromJson(field);
          } catch (e){
            // skip converting from json
          }
        })
      });

      tasks.signTasks(aTasksForSigning).then(function (result) {
        Modal.inform.success()(result);
      }, function (error) {
        Modal.inform.error()('Під час підписання документів сталася помилка: ' + angular.toJson(error));
      })

    };

    taskFilterService.getProcesses().then(function (userProcesses) {
      $scope.userProcesses = userProcesses;
    });

    $rootScope.filteredTasks = null;
    $scope.$storage = $localStorage.$default({
      selfAssignedTaskDefinitionFilter: $scope.taskDefinitions[0],
      unassignedTaskDefinitionFilter: $scope.taskDefinitions[0]
    });

    $scope.resetTaskDefinition = function () {
      $scope.model.taskDefinition = $scope.taskDefinitions[0];
      $scope.taskDefinitionsFilterChange();
    };
    $scope.resetStrictTaskDefinition = function () {
      $scope.model.strictTaskDefinition = $scope.strictTaskDefinitions[0];
      $scope.strictTaskDefinitionFilterChange();
    };
    $scope.resetUserProcess = function () {
      $scope.model.userProcess = $scope.userProcesses[0];
      $scope.userProcessFilterChange();
    };
    $scope.resetTaskFilters = function () {
      $scope.model.taskDefinition = $scope.taskDefinitions[0];
      $scope.model.strictTaskDefinition = $scope.strictTaskDefinitions[0];
      resetFieldFilter();
      localStorage.removeItem("fieldFilter");
      $scope.resetUserProcess();
      $rootScope.tasks = [];
      loadNextTasksPage();
    };
    $scope.$on('taskFilter:strictTaskDefinitions:update', function (ev, data) {
      $scope.strictTaskDefinitions = data;
      // check that current model.strictTaskDefinition is present in data
      if (!data.some(function (taskDefinition) {
          if (!taskDefinition || !$scope.model.strictTaskDefinition) {
            return false;
          }
          if (taskDefinition.id == $scope.model.strictTaskDefinition.id
            && taskDefinition.name == $scope.model.strictTaskDefinition.name) {
            return true;
          }
        })) {
        $scope.model.strictTaskDefinition = data[0];
      }
    });

    function restoreUserProcessesFilter() {
      var storedUserProcess = $scope.$storage[$stateParams.type + 'UserProcessFilter'];
      if (!storedUserProcess) {
        return;
      }
      // check if stored userProcess is presented in selected userprocesses
      if ($scope.userProcesses.some(function (process) {
          if (process.sID == storedUserProcess.sID) {
            return true;
          }
        })) {
        $scope.model.userProcess = storedUserProcess;
      } else {
        $scope.model.userProcess = $scope.userProcesses[0];
      }
    }

    restoreUserProcessesFilter();

    function restoreTaskDefinitionFilter() {
      $scope.model.taskDefinition = $scope.$storage[$stateParams.type + 'TaskDefinitionFilter'];
    }

    $scope.startFilter = function () {
      if ($scope.fieldFilter && $scope.fieldFilter.length !== 0) {
        var defer = $q.defer();
        var filters = [];
        angular.forEach($scope.fieldFilter, function (item) {
          var obj = item.select;
          if (item.enum.value) {
            switch (item.enum.value) {
              case 0:
                obj.sValue = item.string + "*";
                break;
              case 1:
                obj.sValue = "*" + item.string + "*";
                break;
              case 2:
                obj.sValue = "*" + item.string;
                break;
              case 3:
                obj.sValue = item.string;
                break;
            }
          } else if (item.select !== "" && item.string !== "") {
            obj.sValue = item.string;
          } else {
            return
          }
          filters.push(obj);
        });
        var data = {};
        data.soaFilterField = JSON.stringify(filters);
        tasks.list($stateParams.type, data)
          .then(function (oResult) {
            try {
              if (oResult.aoTaskDataVO.code) {
                var e = new Error(oResult.aoTaskDataVO.message);
                e.name = oResult.aoTaskDataVO.code;
                throw e;
              }

              if (oResult.aoTaskDataVO !== null && oResult.aoTaskDataVO !== undefined) {
                var aTaskFiltered = _.filter(oResult.aoTaskDataVO, function (oTask) {
                  return oTask.endTime !== null;
                });
                $rootScope.filteredTasks = [];
                for (var i = 0; i < aTaskFiltered.length; i++)
                  $rootScope.filteredTasks.push(aTaskFiltered[i]);
                lastTasksResult = oResult;
                $timeout(function () {
                  $('#tasks-list-holder').trigger('scroll');
                });
                defer.resolve(aTaskFiltered);
              }
            } catch (e) {
              Modal.inform.error()(e);
              defer.reject(e);
            }
          })
          .catch(function (err) {
            //Modal.inform.error()(err);
            defer.reject(err);
          })
          .finally(function () {
            $scope.tasksLoading = false;
          });
        return defer.promise;
      }
    };

    var filterLoadedTasks = function () {
      $rootScope.filteredTasks = $rootScope.tasksList = taskFilterService.getFilteredTasks($rootScope.tasks, $scope.model);

      $timeout(function () {
        // trigger scroll event to load more tasks
        $('#tasks-list-holder').trigger('scroll');
      });
    };

    $scope.$on('task-submitted', function (e, task) {
      $rootScope.tasks = task && task.nID_Task ? $filter('filter')($rootScope.tasks, {id: '!' + task.nID_Task}) : $rootScope.tasks;
      filterLoadedTasks();
    });

    restoreTaskDefinitionFilter();
    $scope.taskDefinitionsFilterChange = function () {
      $scope.$storage[$stateParams.type + 'TaskDefinitionFilter'] = $scope.model.taskDefinition;
      filterLoadedTasks();
    };
    $scope.userProcessFilterChange = function () {
      $scope.$storage[$stateParams.type + 'UserProcessFilter'] = $scope.model.userProcess;
      filterLoadedTasks();
    };
    $scope.userProcessFilterChange();
    $scope.strictTaskDefinitionFilterChange = function () {
      filterLoadedTasks();
    };
    $scope.selectedSortOrder = {
      selected: "datetime_asc"
    };

    $scope.predicate = 'createTime';
    $scope.reverse = false;

    $scope.sortOrderOptions = [{"value": 'datetime_asc', "text": "Від найдавніших"},
      {"value": 'datetime_desc', "text": "Від найновіших"}];

    $scope.selectedSortOrderChanged = function () {
      switch ($scope.selectedSortOrder.selected) {
        case 'datetime_asc':
          $scope.selectedSortOrder.selected = "datetime_desc";
          if ($stateParams.type == tasks.filterTypes.finished) $scope.predicate = 'startTime';
          else $scope.predicate = 'createTime';
          $scope.reverse = true;
          $rootScope.$broadcast("set-sort-order-reverse-true");
          break;
        case 'datetime_desc':
          $scope.selectedSortOrder.selected = "datetime_asc";
          if ($stateParams.type == tasks.filterTypes.finished) $scope.predicate = 'startTime';
          else $scope.predicate = 'createTime';
          $scope.reverse = false;
          $rootScope.$broadcast("set-sort-order-reverse-false");
          break;
      }
    };

    $scope.isTaskFilterActive = function (taskType) {
      var type = $stateParams.type ? $stateParams.type : $location.hash();
      return type === taskType;
    };

    $scope.isFilterActive = function (filterType) {
      return filterType == $rootScope.currentFilter;
    };

    $scope.isTaskSelected = function (task) {
      return tasksStateModel.sID_Order == task.sID_Order;
    };

    var tasksPage = 0;
    var lastTasksResult = null;

    var loadNextTasksPage = function () {
      var defer = $q.defer();
      var data = {
        page: tasksPage
      };
      if ($stateParams.type == 'tickets') {
        data.bEmployeeUnassigned = $scope.ticketsFilter.bEmployeeUnassigned;
        if ($scope.ticketsFilter.dateMode == 'date' && $scope.ticketsFilter.sDate) {
          data.sDate = $filter('date')($scope.ticketsFilter.sDate, 'yyyy-MM-dd');
        }

        // прерываем постраничную загрузку на вкладке с тикетами, т.к. они отдаются все сразу
        if (tasksPage > 0) {
          defer.resolve([]);
          return defer.promise;
        }
      }

      $scope.tasksLoading = true;

      tasks.list($stateParams.type, data, sort)
        .then(function (oResult) {
          try {
            if (oResult.aoTaskDataVO.code) {
              var e = new Error(oResult.aoTaskDataVO.message);
              e.name = oResult.aoTaskDataVO.code;

              throw e;
            }

            if (oResult.aoTaskDataVO !== null && oResult.aoTaskDataVO !== undefined) {
              // build tasks array
              var aTaskFiltered = _.filter(oResult.aoTaskDataVO, function (oTask) {
                return oTask.endTime !== null;
              });
              if (!$rootScope.tasks)
                $rootScope.tasks = [];
              for (var i = 0; i < aTaskFiltered.length; i++)
                $rootScope.tasks.push(aTaskFiltered[i]);
              lastTasksResult = oResult;
              // build filtered tasks array
              filterLoadedTasks();

              defer.resolve(aTaskFiltered);
              tasksPage++;
            }

          } catch (e) {
            Modal.inform.error()(e);
            defer.reject(e);
          }
        })
        .catch(function (err) {
          //Modal.inform.error()(err);
          defer.reject(err);
        })
        .finally(function () {
          $scope.tasksLoading = false;
        });

      return defer.promise;
    };

    $scope.refreshCurrentTab = function (tab) {
      if ($scope.tasksLoading)
        return;

      if ($scope.sSelectedTask === tab) {
        $state.reload();
      }
    };

    $scope.toggleSubFolder = function(menu) {
      iGovNavbarHelper.subfolders[menu].show = !iGovNavbarHelper.subfolders[menu].show;
    };

    $scope.applyTaskFilter = function () {
      tasksPage = 0;
      $rootScope.tasks = $rootScope.filteredTasks = null;
      $scope.sSelectedTask = $stateParams.type;
      $scope.selectedTask = null;
      restoreTaskDefinitionFilter();
      restoreUserProcessesFilter();
      $scope.error = null;

      if ($stateParams.type == tasks.filterTypes.finished) {
        $scope.predicate = 'startTime';
      }

      loadNextTasksPage().then(function (tasks) {
        // загружаем список пока не будет найдена задача из стейта tasks.typeof.view
        // tasksStateModel.taskId устанавливается при резолве этого стейта
        updateTaskSelection(tasks, tasksStateModel.sID_Order);
      });
    };

    $scope.getUserName = function () {
      identityUser
        .getUserInfo($scope.selectedTask.assignee)
        .then(function (userInfo) {
          return "".concat(userInfo.firstName, " ", userInfo.lastName);
        }).catch(function () {
        return $scope.selectedTask.assignee;
      });
    };

    $scope.selectTask = function (task) {
      if(task.id){
        $state.go('tasks.typeof.view', {id: task.id});
      } else if(task.nID_Task){
        $state.go('tasks.typeof.view', {id: task.nID_Task});
      } else {
        $state.go('tasks.typeof.view', {id: task});
      }

    };

    $scope.fillTaskOrderArray = function () {
      var tasksOrderArray = [];
      $scope.showTaskSpinner = true;

      angular.forEach($scope.filteredTasks, function (task) {
        if (task.sID_Order)
          tasksOrderArray.push(task.sID_Order);
      });

      if ($scope.sSelectedTask !== 'ecp') {
        $rootScope.tasksOrders.page = $scope.filteredTasks.length % 50 === 0 ? $scope.filteredTasks.length / 50 : 'last';
      } else {
        $rootScope.tasksOrders.page = $scope.filteredTasks.length % 15 === 0 ? $scope.filteredTasks.length / 15 : 'last';
      }

      $rootScope.tasksOrders.order = tasksOrderArray;
    };

    $scope.sDateShort = function (sDateLong) {
      if (sDateLong !== null) {
        var o = new Date(sDateLong); //'2015-04-27T13:19:44.098+03:00'
        return o.getFullYear() + '-' + ((o.getMonth() + 1) > 9 ? '' : '0') + (o.getMonth() + 1) + '-' + (o.getDate() > 9 ? '' : '0') + o.getDate() + ' ' + (o.getHours() > 9 ? '' : '0') + o.getHours() + ':' + (o.getMinutes() > 9 ? '' : '0') + o.getMinutes();
      }
    };

    function endsWith(s, sSuffix) {
      if (s == null) {
        return false;
      }
      return s.indexOf(sSuffix, s.length - sSuffix.length) !== -1;
    }

    $scope.sTaskClass = function (sUserTask) {
      //"_10" - подкрашивать строку - красным цветом
      //"_5" - подкрашивать строку - желтым цветом
      //"_1" - подкрашивать строку - зеленым цветом
      var sClass = "";
      if (endsWith(sUserTask, "_red")) {
        return "bg_red";
      }
      if (endsWith(sUserTask, "_yellow")) {
        return "bg_yellow";
      }
      if (endsWith(sUserTask, "_green")) {
        return "bg_green";
      }
      if (endsWith(sUserTask, "usertask1")) {
        return "bg_first";
      }
    };

    /**
     * Check if task in status
     * @param {object} task Task data
     * @param {string} status Status to check
     * @returns {boolean} True if task is in status otherwise false
     */
    $scope.hasTaskStatus = function (task, status) {
      var saTaskStatusVarData = getTaskVariable(task.variables, 'saTaskStatus');
      return hasTaskStatus(saTaskStatusVarData, status);
    };

    $scope.getTaskTitle = function (task) {
      return '(' + task.sID_Order + ') ' + task.name;
    };

    $scope.getTaskDateTimeTitle = function (task) {
      var result = task.createTime ? $scope.sDateShort(task.createTime) : $scope.sDateShort(task.startTime);
      if (task.endTime)
        result += ' - ' + $scope.sDateShort(task.endTime);
      return result;
    };

    $scope.ticketsFilter = {
      dateMode: 'date',
      dateModeList: [
        {key: 'all', title: 'Всі дати'},
        {key: 'date', title: 'Обрати дату'}
      ],
      sDate: moment().format('YYYY-MM-DD'),
      options: {
        timePicker: false
      },
      bEmployeeUnassigned: false
    };

    $scope.applyTicketsFilter = function () {
      $scope.applyTaskFilter();
    };

    $scope.setTicketsDateMode = function (mode) {
      $scope.ticketsFilter.dateMode = mode;
      $scope.applyTicketsFilter();
    };

    $scope.lunaService = lunaService;

    var updateTaskSelection = function (tasks, nID_Task) {
      if (nID_Task && tasks && tasks.length > 0) {
        var foundTask = null;
        for (var i = 0; i < tasks.length; i++) {
          var task = tasks[i];
          if (task.id == nID_Task) {
            foundTask = task;
            break;
          }
        }
        if (foundTask)
          $scope.selectTask(foundTask);
        else
          initDefaultTaskSelection();
        //?
        // loadNextTasksPage().then(function (nextTasks) {
        //   updateTaskSelection(nextTasks, nID_Task);
        // });
      } else if ($state.current.name != 'tasks.typeof.view')
        initDefaultTaskSelection();
    };

    var initDefaultTaskSelection = function () {
      if ($scope.selectedTask)
        $scope.selectTask($scope.selectedTask);
      // else if ($scope.filteredTasks && $scope.filteredTasks[0])
      //   $scope.selectTask($scope.filteredTasks[0]);
    };

    var defaultErrorHandler = function (response, msgMapping) {
      defaultSearchHandlerService.handleError(response, msgMapping);
    };

    $scope.whenScrolled = function () {
      if ($scope.tasksLoading === false && $scope.isLoadMoreAvailable())
        $scope.loadMoreTasks();
    };

    $scope.isLoadMoreAvailable = function () {
      return lastTasksResult !== null && $scope.sSelectedTask !== 'tickets' && lastTasksResult.start + lastTasksResult.size < lastTasksResult.total;
    };

    $scope.loadMoreTasks = function () {
      loadNextTasksPage();
    };

    $scope.applyTaskFilter();

    var saveItemToLocalStorage = function (name, item) {
      localStorage.setItem(name, JSON.stringify(item));
    };

    $scope.filterFieldsOptions = [{name: "Починаючи з", value: 0},
      {name: "З присутністю", value: 1},
      {name: "Закінчуючи на", value: 2},
      {name: "Дорівнює", value: 3}];
    $scope.selectedFieldFilterValue = $scope.filterFieldsOptions[0];

    function resetFieldFilter() {
      $scope.fieldFilter = [{select: '', string: '', enum: $scope.selectedFieldFilterValue}];
    }

    var filterFromStorage = localStorage.getItem('fieldFilter');
    if (filterFromStorage !== null) {
      $scope.fieldFilter = JSON.parse(filterFromStorage);
    } else {
      resetFieldFilter()
    }

    var addFilter = function () {
      saveItemToLocalStorage('fieldFilter', $scope.fieldFilter);
      if ($scope.fieldFilter[$scope.fieldFilter.length - 1].select !== "") {
        $scope.fieldFilter.push({select: '', string: '', enum: $scope.selectedFieldFilterValue});
      }
    };

    $scope.onSelectDataList = function ($item, index) {
      $scope.fieldFilter[index].select = $item;
      saveItemToLocalStorage('fieldFilter', $scope.fieldFilter);
      addFilter();
    };

    $scope.onSelectEnumFields = function (val, index) {
      $scope.fieldFilter[index].enum = val;
      saveItemToLocalStorage('fieldFilter', $scope.fieldFilter);
    };

    $scope.removeFieldFilter = function (index) {
      $scope.fieldFilter.splice(index, 1);
      saveItemToLocalStorage('fieldFilter', $scope.fieldFilter);
    };

    function toggleMenu(status) {
      if (typeof status === 'boolean') {
        if (status) {
          $scope.isMenuOpened = true;
          snapRemote.open('left');
        } else {
          $scope.isMenuOpened = false;
          snapRemote.close();
        }
        localStorage.setItem('menu-status', JSON.stringify(status));
      }
    }

    var menuStatus = localStorage.getItem('menu-status');
    if (menuStatus) {
      var status = JSON.parse(menuStatus);
      toggleMenu(status);
    } else {
      $scope.isMenuOpened = false;
      snapRemote.close();
    }

    $rootScope.toggleMenu = function () {
      $scope.isMenuOpened = !$scope.isMenuOpened;
      localStorage.setItem('menu-status', JSON.stringify($scope.isMenuOpened));
    };

    $rootScope.tabMenu = Tab.getCurrentTab($location.url(), $stateParams, $location.hash());

    $scope.tabMenuChange = function (param) {
      $rootScope.tabMenu = param;
      $rootScope.clearAllFilterFields();
    };

    $scope.isVisible = function (menuType) {
      if (menuType === 'all') {
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit');
      }
      if (menuType === 'finished') {
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit', 'supervisor');
      }
      return Auth.isLoggedIn();
    };

    var getFilterList = function () {
      if (localStorage.getItem('listOfFilters')){
        $scope.listOfFilters = JSON.parse(localStorage.getItem('listOfFilters'));
      }
    };
    getFilterList();

    $scope.removeFilterFromList = function (index) {
      $scope.listOfFilters.splice(index, 1);
      localStorage.setItem('listOfFilters', JSON.stringify($scope.listOfFilters));
    };

    $scope.getSelectedFilter = function (filter) {
      var options = filter.options;
      $rootScope.currentFilter = filter;
        $rootScope.spinner = true;
        var data = {
          sLogin: options.author ? options.author.sLogin : Auth.getCurrentUser().id,
          sDateType: options.dateOption,
          sDateFrom: options.startDate,
          sDateTo: options.endDate,
          sProcessDefinitionKey: options.docTemplate ? (options.docTemplate.sID === 'all' ?
            null : options.docTemplate.sID) : null,
          sLoginController: options.controller ? options.controller.sLogin : null,
          sLoginExecutor: options.executor ? options.executor.sLogin : null,
          sFind: options.searchText,
          sFilterStatus: options.tabSelect,
          bIncludeDeleted: false,
          bSearchExternalTasks: false,
        };
        SearchBox.searchBoxTasks(data).then(function (res) {
          $rootScope.filteredTasks = angular.copy(res.aoTaskDataVO);
          $rootScope.tasks = angular.copy(res.aoTaskDataVO);
          $rootScope.spinner = false;
        }).finally(function () {
          $rootScope.spinner = false;
        });
    };

    $scope.currentTab = function () {
      for (var i = 0; i < iGovNavbarHelper.menus.length; i++) {
        if (iGovNavbarHelper.menus[i].tab === $stateParams.type) {
          return iGovNavbarHelper.menus[i].title;
        }
      }

      for (var o = 0; o < iGovNavbarHelper.documentsMenus.length; o++) {
        if (iGovNavbarHelper.documentsMenus[o].tab === $stateParams.type) {
          return iGovNavbarHelper.documentsMenus[o].title;
        }
      }

      for (var el in iGovNavbarHelper.subfolders) {
        if (iGovNavbarHelper.subfolders.hasOwnProperty(el)) {
          var parent = iGovNavbarHelper.subfolders[el];
          for (var e = 0; e < parent.folders.length; e++) {
            if (parent.folders[e].tab === $stateParams.type) {
              return parent.folders[e].title;
            }
          }
        }
      }
    };
    snapRemote.getSnapper().then(function (snapper) {
      snapper.settings({touchToDrag: false, tapToClose: false, minPosition: -266, maxPosition: 266});
      var elem = document.getElementsByClassName('snap-content')[0];
      if (elem)
          elem.style.transform = $scope.isMenuOpened ? 'translate3d(266px, 0px, 0px)' : 'translate3d(0px, 0px, 0px)';
      snapper.on('animated', function () {
        if (snapper.state().state === 'closed') {
          $scope.isMenuOpened = false;
          $scope.$apply();
        } else if (snapper.state().state === 'left') {
          $scope.isMenuOpened = true;
          $scope.$apply();
        }
      });
    });


    function getFilterFieldsList() {
      var user = Auth.getCurrentUser().id;
      tasks.getFilterFieldsList(user).then(function (res) {
        if (Array.isArray(res) && res.length > 0) {
          $scope.fieldList = res;
        }
      });
    }

    // getFilterFieldsList();
  }

  /**
   * Returns task variable data
   * @param {array} variables Task variables
   * @param {string} varName variable name
   */
  function getTaskVariable(variables, varName) {
    if (angular.isDefined(variables)) {
      for (var i = 0; i < variables.length; i++) {
        var v = variables[i];

        if (v.name == varName)
          return v;
      }
    }

    return null;
  }

  /**
   * Check task is in status
   * @param {object} variableData
   * @status {string} Status to check
   */
  function hasTaskStatus(variableData, status) {
    return (variableData && variableData.value) ? variableData.value.indexOf(status) >= 0 : false;
  }
})();
