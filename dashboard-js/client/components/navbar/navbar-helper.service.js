(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .factory('iGovNavbarHelper', iGovNavbarHelperFactory);

  iGovNavbarHelperFactory.$inject = ['Auth', 'tasks', '$location', '$rootScope', '$http', 'SubFolder'];
  function iGovNavbarHelperFactory(Auth, tasks, $location, $rootScope, $http, SubFolder) {
    $rootScope.tasksSearch = {
        value: null,
        count: 0,
        archive: false,
        loading: false,
        submited: false
    };

    function getSubFolders(names) {
      var self = this;

      function getSubNames() {
        var subFolderNames = [];
        for (var sFolders in self.subfolders) {
          if (self.subfolders.hasOwnProperty(sFolders)) {
            angular.forEach(self.subfolders[sFolders].folders, function (sub) {
              subFolderNames.push(sub.tab);
            });
          }
        }
        return subFolderNames;
      }

      if (Object.keys(self.subfolders).length === 0 && !self.subfolders.subFoldersAreEmpty) {
        $http.get('/api/tasks/getSubFolders').then(function (response) {
          if (response && response.data) {
            self.subfolders = SubFolder.subFolderObj(response.data, self.documentsMenus, self.menus, oTabName);
            if (names) {
              return getSubNames();
            } else if (Object.keys(self.subfolders).length === 0) {
              self.subfolders = {subFoldersAreEmpty: true};
            }
          } else {
            console.error('error in getSubFolder service.');
          }
        });
      } else {
        if (names) return getSubNames();
      }
    }

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
      Execution: 'execution',
      ControlFinished: 'controled',
      ExecutionFinished: 'executed'
    };

    var service = {
      areInstrumentsVisible: false,
      auth: Auth,
      getCurrentTab: getCurrentTab,
      isCollapsed: true,
      isTest: false,
      load: load,
      loadTaskCounters: loadTaskCounters,
      instrumentsMenus: [],
      isCountersLoaded: false,
      sPreviousTab: '',
      tasksSearch: $rootScope.tasksSearch,
      tabName: oTabName,
      getSubFolders: getSubFolders
    };

    service.menus = [{
      title: 'На виконанні',
      type: tasks.filterTypes.execution,
      count: 0,
      showCount: true,
      tab: 'execution',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'На контролі',
      type: tasks.filterTypes.control,
      count: 0,
      showCount: true,
      tab: 'control',
      hasSubFolder: false,
      subFolder: false
    }, 
    {
    	title: 'Проконтрольовані',
		type: tasks.filterTypes.controled,
		count: 0,
		showCount: true,
		tab: 'controled',
		hasSubFolder: false,
		subFolder: false
    },
    {
    	title: 'Виконані',
		type: tasks.filterTypes.executed,
		count: 0,
		showCount: true,
		tab: 'executed',
		hasSubFolder: false,
		subFolder: false
    },
    {
      title: 'Необроблені',
      type: tasks.filterTypes.unassigned,
      count: 0,
      showCount: true,
      tab: 'unassigned',
      hasSubFolder: false,
      subFolder: false,
      hideItem : $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE'
    }, {
      title: 'В роботі',
      type: tasks.filterTypes.selfAssigned,
      count: 0,
      showCount: true,
      tab: 'selfAssigned',
      hasSubFolder: false,
      subFolder: false,
      hideItem : $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE'
    }, {
      title: 'Мій розклад',
      type: tasks.filterTypes.tickets,
      count: 0,
      showCount: true,
      tab: 'tickets',
      hasSubFolder: false,
      subFolder: false,
      hideItem : $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE'
    }, {
      title: 'Усі',
      type: tasks.filterTypes.all,
      count: 0,
      showCount: false,
      tab: 'all',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'Історія',
      type: tasks.filterTypes.finished,
      count: 0,
      showCount: true,
      tab: 'finished',
      hasSubFolder: false,
      subFolder: false
    }];

    service.documentsMenus = [{
      title: 'Нерозглянутi',
      type: tasks.filterTypes.documents,
      count: 0,
      showCount: true,
      tab: 'documents',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'Мої документи',
      type: tasks.filterTypes.myDocuments,
      count: 0,
      showCount: true,
      tab: 'myDocuments',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'Очiкують мого ЕЦП',
      type: tasks.filterTypes.ecp,
      count: 0,
      showCount: true,
      tab: 'ecp',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'Переглянутi',
      type: tasks.filterTypes.viewed,
      count: 0,
      showCount: true,
      tab: 'viewed',
      hasSubFolder: false,
      subFolder: false
    }, {
      title: 'Історія',
      type: tasks.filterTypes.docHistory,
      count: 0,
      showCount: true,
      tab: 'docHistory',
      hasSubFolder: false,
      subFolder: false
    }];

    service.subfolders = {};

    service.instrumentsMenus = [
      {state: 'tools.users', title: 'Користувачі'},
      {state: 'tools.groups', title: 'Групи'},
      {state: 'tools.escalations', title: 'Ескалації', hideItem: $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE'},
      {state: 'tools.reports', title: 'Звіт'},
      {state: 'tools.services', title: 'Розклад', hideItem: $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE'},
      {state: 'tools.deploy', title: 'Розгортання', 
        hideItem: $rootScope.ProjectRegion_MainPage_biDocOnly === 'FALSE' && !Auth.hasOneOfRoles('superadmin')}
    ];

    return service;

    function getCurrentTab() {
      var path = $location.path();
      if (path.indexOf('/tasks') === 0) {
        service.areInstrumentsVisible = false;
        var matches = path.match(/^\/tasks\/(\w+)(\/\d+)?$/);
        if (matches)
          service.currentTab = matches[1];
        else
          service.currentTab = 'tickets';
      }
      else {
        if(path.indexOf('/profile') === 0){
          service.areInstrumentsVisible = false;
        } else {
          service.areInstrumentsVisible = true;
        }
        service.currentTab = path;
      }
    }

    function load() {
      service.previousTab = service.currentTab;
      service.getCurrentTab();
      service.loadTaskCounters();
    }

    function loadTaskCounters(type, isFullReload) {
      var objForLoadCounter = [];
      if(service.currentUser && service.currentUser.id){
        var user = service.auth.getCurrentUser();
        if(user.id !== service.currentUser.id){
          service.isCountersLoaded = false;
        }
      }
      if(!service.isCountersLoaded || isFullReload){
        service.currentUser = service.auth.getCurrentUser();
        objForLoadCounter = type === 'documents' ? service.documentsMenus : service.menus;
      } else {
        _.each(service.menus, function (menu) {
          if(menu.tab === service.previousTab || menu.tab === service.currentTab){
            objForLoadCounter.push(menu);
          }
        })
      }
      _.each(objForLoadCounter, function (menu) {
        if (menu.showCount) {
          tasks.list(menu.type)
              .then(function(result) {
                try {
                  result = JSON.parse(result);
                } catch (e) {
                  result = result;
                }
                menu.count = result.total;
                service.isCountersLoaded = true;
              });
        }
      });
    }
  }
})();
