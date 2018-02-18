angular.module('dashboardJsApp').service('SearchBox', ['$state', '$q', 'tasks', 'Auth', '$http',
  function ($state, $q, tasks, Auth, $http) {

    function simpleHttpPromise(req, callback) {
      var cb = callback || angular.noop;
      var deferred = $q.defer();

      $http(req).then(
        function (response) {
          deferred.resolve(response.data);
          return cb();
        },
        function (response) {
          deferred.reject(response);
          return cb(response);
        }.bind(this));
      return deferred.promise;
    }

    return {

      getListOfAllBpForLogin: function (bFilterDoc) {
        var request = {
          method: 'GET',
          url: 'api/processes/getAllBpForLogin',
          params: {
            sLogin: Auth.getCurrentUser().id,
            bFilterDoc: bFilterDoc
          }
        };
        return simpleHttpPromise(request);
      },

      getBpFields: function (sID) {
        var request = {
          method: 'GET',
          url: 'api/processes/getBPFields',
          params: {
            sProcessDefinitionKey: sID
          }
        };
        return simpleHttpPromise(request);
      },

      getCurrentUserCompany: function () {
        var request = {
          method: 'GET',
          url: 'api/staff/getSubjectGroupsTreeUp',
          params: {
            sSubjectType: 'Organ',
            sID_Group_Activiti: Auth.getCurrentUser().id,
            nDeepLevel: 0
          }
        };
        return simpleHttpPromise(request);
      },

      refreshAuthorList: function (sCompany, sFind) {
        var request = {
          method: 'GET',
          url: 'api/subject-role',
          params: {
            sID_Group_Activiti: sCompany,
            sSubjectType: "Human",
            nDeepLevel: 0,
            sFind: sFind
          }
        };
        return simpleHttpPromise(request);
      },

      searchBoxTasks: function (data) {
        var request = {
          method: 'POST',
          url: 'api/tasks/searchTasks',
          params: {},
          data: data
        };
        return simpleHttpPromise(request);
      },
    };
  }]);
