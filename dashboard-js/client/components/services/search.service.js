angular.module('dashboardJsApp').service('searchService', ['$state', 'tasks', 'Tab', 'iGovNavbarHelper', 'Modal', '$rootScope', 'CurrentServer', '$http', '$q',
  function($state, tasks, Tab, iGovNavbarHelper, Modal, $rootScope, CurrentServer, $http, $q) {
    return {
      searchByOrder: function (order) {
        var self = this;
        CurrentServer.setServer(order).then(function () {
          self.searchTabByOrder(order).then(function (result) {
            if (result && result.oTab && result.oTab.sDocumentStatus) {
              var backTabName = result.oTab.sDocumentStatus;
              var type = result.oTab.sSubTab ? result.oTab.sSubTab.toLowerCase() : iGovNavbarHelper.tabName[backTabName];
              var params = {
                tab: Tab.getCurrentTab(backTabName),
                type: type,
                sID_Order: order,
                '#': type
              };
              $state.go('tasks.typeof.view', params)
            } else {
              Modal.inform.warning()('Документ/Завдання за даним користувачем не знайдено');
              $rootScope.spinner = false;
              return;
            }
          }).catch(function (e) {
            Modal.inform.warning()(e);
            CurrentServer.init();
            $rootScope.spinner = false;
            return;
          })
        });
      },

      searchTabByOrder: function (order) {
        var deferred = $q.defer();
        var options = {order: order};
        var serverData = CurrentServer.getServer();

        if (serverData && serverData.another) {
          options.taskServer = serverData.name;
        }

        $http.get('/api/tasks/getTabByOrder', {params: options}).then(function (result) {
          if (result && result.data) {
            deferred.resolve(result.data);
          } else {
            deferred.reject()
          }
        });

        return deferred.promise;
      }
    }
}]);
