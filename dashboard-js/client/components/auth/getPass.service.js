'use strict';

angular.module('dashboardJsApp')
  .factory('GetPassService', function events($http, $q, CurrentServer) {
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
      sendPasswordOfUser: function (userInfo) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/subject/sendPasswordOfUser',
          params: {
            sUserLogin: userInfo.login,
            sUserMail: userInfo.email
          }
        });
      },

      sendPasswordOfUserCustom: function (sUserLogin) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/subject/sendPasswordOfUserCustom',
          params: {
            sUserLogin: sUserLogin
          }
        });
      },

      sendPasswordsOfUsersOnServer: function (allUsersData) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/subject/sendPasswordsOfUsersOnServer',
          params: {
            bSend: allUsersData.bSend,
            bSendAdmin: allUsersData.bSendAdmin
          }
        });
      }
    };
  });
