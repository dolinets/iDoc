'use strict';

angular.module('dashboardJsApp')
  .factory('DocumentsService', ['$http', '$q', 'Auth', 'CurrentServer', '$cookies', function tasks($http, $q, Auth, CurrentServer, $cookies) {
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

      downloadDocument: function (taskId, callback) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + taskId + '/document'
        }, callback);
      },

      getProcessSubjectTree: function (id) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getProcessSubjectTree',
          params: {
            snID_Process_Activiti: id,
            nDeepLevel: 0
          }
        })
      },

      isUserHasDocuments: function (login) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getBPs_ForReferent',
          params: {
            sLogin: login
          }
        })
      },

      createNewDocument: function (bpID) {
        return simpleHttpPromise({
          method: 'GET',
          url: 'api/documents/setDocument',
          params: {
            sID_BP: bpID
          }
        })
      },

      delegateDocToUser : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/delegateDocument',
            params: params
          })
      },
      addAcceptorToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addAcceptor',
            params: params
          });
      },
      addVisorToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addVisor',
            params: params
          });
      },
      addViewerToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addViewer',
            params: params
          });
      },
      getUnsignedDocsList: function () {
        var currentUser = Auth.getCurrentUser();
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getDocumentSubmittedUnsigned',
          params: {
            sLogin: currentUser.id
          }
        })
      },

      removeDocumentSteps: function (nID_Process) {
        var currentUser = Auth.getCurrentUser();
        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : currentUser.id;

        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/removeDocumentSteps',
          params: {
            snID_Process_Activiti: nID_Process,
            sLogin: currentUser.id,
            sLoginReferent: sCurrReferent
          }
        })
      },

      setProcessStatus: function (params) {
        var taskServer = CurrentServer.getServer();
        var options = {
          method: 'POST',
          url: '/api/processes/setProcessSubjectStatus',
          data: params
        };

        if (taskServer.another)
          options.data.taskServer = taskServer.name;

        return simpleHttpPromise(options)
      },

      getLoginExecutorOrController: function (form, taskID, type) {
        var currentUser = Auth.getCurrentUser(),
            params = {},
            deferred = $q.defer(),
            referent;

        if ($cookies.get('referent')) {
          referent = JSON.parse($cookies.get('referent'));
        }

        switch(type) {
          case 'executor':
            for ( var e=0; e<form.length; e++ ) {
              if ( currentUser.id === form[e].sLogin || (referent && referent.sLogin === form[e].sLogin)) {
                params.sLoginExecutor = form[e].sLogin;
                params.snID_Task_Activiti = taskID;
                deferred.resolve(params);
                break;
              }
            }
            break;

          case 'controller':
            for ( var c=0; c<form.length; c++ ) {
              if ( currentUser.id === form[c].sLogin && form[c].sLoginRole === 'Controller'
                || (referent && referent.sLogin === form[c].sLogin && form[c].sLoginRole === 'Controller')) {
                params.sLoginController = form[c].sLogin;
                params.snID_Task_Activiti = taskID;
                deferred.resolve(params);
                break;
              }
            }
            break;

          case 'both':
            for ( var b=0; b<form.length; b++ ) {
              if ( currentUser.id === form[b].sLogin || (referent && referent.sLogin === form[b].sLogin)) {
                params.sLoginController = form[b].sLogin;
                if ( params.sLoginExecutor ) {
                  params.snID_Task_Activiti = taskID;
                  deferred.resolve(params);
                  break;
                }
              } else if( form[b].id === 'sLoginExecutor' && currentUser.id === form[b].id || (referent && form[b].id === 'sLoginExecutor' && referent.sLogin === form[b].id )) {
                params.sLoginExecutor = form[b].sLogin;
                if ( params.sLoginController ) {
                  params.snID_Task_Activiti = taskID;
                  deferred.resolve(params);
                  break;
                }
              }
            }
            break;
        }

        return deferred.promise;
      },

      cancelDocumentSubmit: function (params) {
        var taskServer = CurrentServer.getServer();
        if (taskServer.another)
          params.taskServer = taskServer.name;

        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/cancelDocumentSubmit',
          params: params
        });
      },

      removeDocumentStepSubject: function (params) {
        var taskServer = CurrentServer.getServer();

        params.taskServer = taskServer.another ? taskServer.name : null;
        
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/removeDocumentStepSubject',
          params: params
        });
      },

      getProcessChat: function (nID_Process) {
        var taskServer = CurrentServer.getServer();

        return simpleHttpPromise({
          method: 'GET',
          url: '/api/chat/getProcessChat',
          params: {
            nID_Process_Activiti: nID_Process,
            taskServer: taskServer.another ? taskServer.name : null
          }
        });
      }
  };
  }]);
