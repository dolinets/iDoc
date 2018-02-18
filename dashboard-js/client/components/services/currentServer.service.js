angular.module('dashboardJsApp').service('CurrentServer', ['$http', '$q', function($http, $q) {
  var selectedTaskServer = {id: null, name: null},
      oServer = {};

  function getCurrentServer() {
    var deferred = $q.defer();
    $http.get('/api/server').then(function (res) {
      if (res && res.data) {
        oServer = {
          isAnother: false,
          hostAndID: res.data
        };
       deferred.resolve();
      } else {
        deferred.reject();
      }
    });
    return deferred.promise;
  }

  return {
    init: function () {
      if (!oServer.hostAndID || typeof oServer.hostAndID !== 'object') {
        getCurrentServer();
        selectedTaskServer = {id: null, name: null};
      }
    },

    currentServerId: function () {
      return oServer.hostAndID && oServer.hostAndID.host ? oServer.hostAndID.host : null;
    },

    setServer: function (sID_Order) {
      var deferred = $q.defer();
      var self = this;

      function setServ() {
        if (sID_Order) {
          var sId = sID_Order.split('-')[0];
          if ( +sId !== oServer.hostAndID.nID_Server ) {
            $http.get('/api/server/updateServerInfo', {params: {id: sId}}).then(function (res) {
              oServer.isAnother = true;
              selectedTaskServer = {id: sId, name: res.data};
              deferred.resolve();
            });
          } else {
            oServer.isAnother = false;
            deferred.resolve(null);
          }
        } else {
          deferred.reject();
        }
      }

      if (!oServer.hostAndID) {
        getCurrentServer().then(function () {
          setServ();
        })
      } else {
        setServ();
      }

      return deferred.promise;
    },

    getServer: function (status) {
      var self = this;

      if (status === 'check') {
        return selectedTaskServer;
      }
      if (!oServer.hostAndID) {
        getCurrentServer().then(function () {
          self.setServer();
        })
      } else {
        return {another: oServer.isAnother, id: selectedTaskServer.id, name: selectedTaskServer.name};
      }
    },

    resetServer: function () {
      selectedTaskServer = {id: null, name: null};
      oServer.isAnother = false;
    }
  }
}]);
