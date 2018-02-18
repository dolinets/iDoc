'use strict';

angular.module('dashboardJsApp')
  .factory('eventService', function events($http, $q, CurrentServer) {
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
      getHistoryEvents: function (page, sID_Order) {
        var taskServer = CurrentServer.getServer();
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/events/getHistoryEvents',
          params: {
            page: page ? page : 0,
            sID_Order: sID_Order,
            taskServer: taskServer.another ? taskServer.name : null
          }
        });
      }
    };
  });
