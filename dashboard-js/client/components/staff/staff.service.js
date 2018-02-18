'use strict';

angular.module('dashboardJsApp')
  .factory('Staff', function Staff($location, $rootScope, $http, Base64, $cookieStore, $q) {
    var sessionSettings;

    if ($cookieStore.get('sessionSettings')) {
      sessionSettings = $cookieStore.get('sessionSettings');
    }

    function currentUser() {
      var user = $cookieStore.get('user');
      return user ? user.id : '';
    }

    function currentReferent() {
      var user = $cookieStore.get('referent');
      return user ? user.id : currentUser();
    }

    return {
      simpleHttpPromise: function (req, callback) {
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
      },

      changeStaffPassword: function (sLoginOwner, sPasswordOld, sPasswordNew, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.post('/api/staff/changeStaffPassword', {
          sLoginOwner: sLoginOwner,
          sPasswordOld: sPasswordOld,
          sPasswordNew: sPasswordNew
        }).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },

      createSubjectHuman: function (employee, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.post('/api/staff/createSubjectHuman', {
          employee: employee
        }).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      createNewEditDepart: function (depart, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/setSubjectOrgan', depart
        ).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      createNewPosition: function (posit, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/setSubjectHumanPositionCustom', posit
        ).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      getAllContactType: function (callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/getSubjectContactType').success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      getReferentStaff: function(login, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/getReferentStaff', {params: {sID_Group_Activiti: login}}).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      removeReferentStaff: function (paramsRef, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.delete('./api/staff/removeReferentStaff', paramsRef).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      addReferentStaff: function (referent, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/setReferentStaff', referent).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      getUserGroupMember: function(login, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/users/getUserGroupMember', {params: {
          sLoginStaff: login,
          sLogin: currentUser(),
          sLoginReferent: currentReferent()
        }}).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      findForFilter: function (param, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/findInCompany', param).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      editHuman: function (employee, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/editSubjectHuman', employee).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      getContact: function (contact, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/getStaffContact', contact).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      setContact: function (login, contacts, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.post('/api/staff/setSubjectContact', {
          sID_Group_Activiti: login,
          contacts: contacts
        }).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      removeContact: function (login, contacts, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.post('/api/staff/deleteSubjectContact', {
          sID_Group_Activiti: login,
          contacts: contacts
        }).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      checkAdmin: function (role, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();
        $http.get('./api/staff/checkIsAdmin', role)
        .success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },

      setSubjectRightBP: function(bp, login, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/setSubjectRightBP', {params: {sID_BP: bp, sID_Group_Referent: login}}).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      removeSubjectRightBP: function(bp, login, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/removeSubjectRightBP', {params: {sID_BP: bp, sID_Group_Referent: login}})
        .success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },
      getSubjectRightBP: function(login, callback) {
        var cb = callback || angular.noop;
        var deferred = $q.defer();

        $http.get('./api/staff/getSubjectRightBP', {params: {
          sLoginStaff: login,
          sLogin: currentUser(),
          sLoginReferent: currentReferent()
        }}).success(function (data) {
          deferred.resolve(data);
          return cb();
        }).error(function (err) {
          deferred.reject(err);
          return cb(err);
        }.bind(this));

        return deferred.promise;
      },

      getAllBP: function() {
        var deferred = $q.defer();

        $http.get('./api/staff/getAllBP').success(function (data) {
          deferred.resolve(data);
        }).error(function (err) {
          deferred.reject(err);
        }.bind(this));

        return deferred.promise;
      },

      getHumanPositions: function(search, login) {
        var selectPosit = {params: {}};
        selectPosit.params.sFind = search;
        selectPosit.params.sLogin = currentUser();
        selectPosit.params.sLoginReferent = currentReferent();
        selectPosit.params.sLoginStaff = login ? login : null;

        var deferred = $q.defer();
        $http.get('./api/staff', selectPosit).success(function (data) {
          deferred.resolve(data);
        }).error(function (err) {
          deferred.reject(err);
        }.bind(this));

        return deferred.promise;
      }

    }
});
