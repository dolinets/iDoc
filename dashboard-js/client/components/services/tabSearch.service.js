angular.module('dashboardJsApp').service('Tab', ['$state', '$q', 'tasks', 'Auth', 'iGovNavbarHelper', '$cookies',
  function ($state, $q, tasks, Auth, iGovNavbarHelper, $cookies) {
  var taskTabs = 'control | selfAssigned | tickets | unassigned | all | finished | execution | executed | controled';

  return {
    getCurrentTab: function (hashOrType, params, url) {
      var currentTab = params && params.type ? params.type : hashOrType;

      if (url && url.indexOf('create-') > -1 && params.tab) {
        if (params.tab === 'documents') {
          return 'documents';
        } else {
          return 'tasks';
        }
      } else {
        if (taskTabs.indexOf(currentTab) > -1) {
          return 'tasks';
        } else {
          return 'documents';
        }
      }
    },

    navigateTask: function (list, current, tab, type) {
      var deferred = $q.defer();

      if (list && list.order) {
        for (var i = 0; i < list.order.length; i++) {
          if (current === list.order[i]) {
            if (!list.order[i + 1] && list.order[i - 1]) {
              if (list.page !== 'last') {
                getNextPage(list.order[i - 1]);
                break;
              } else {
                deferred.resolve(getStructure(list.order[i - 1], null, null));
                break;
              }
            } else if (!list.order[i - 1] && list.order[i + 1]) {
              deferred.resolve(getStructure(null, list.order[i + 1], null));
              break;
            } else if (!list.order[i - 1] && !list.order[i + 1]) {
              deferred.resolve(null);
              break;
            } else {
              deferred.resolve(getStructure(list.order[i - 1], list.order[i + 1], null));
              break;
            }
          }
        }

        function getNextPage(previous) {
          tasks.list(type, {page: list.page}).then(function (response) {
            deferred.resolve(getStructure(previous, response.aoTaskDataVO[0].sID_Order, response.aoTaskDataVO));
          });
        }

        function getStructure(prev, next, nextPage) {
          var obj = {
            data: {
              tab: tab,
              type: type,
              task: {
                next: next,
                previous: prev
              },
              '#': type
            }
          };

          if (nextPage) {
            var newPageOrders = [];
            angular.forEach(nextPage, function (page) {
              newPageOrders.push(page.sID_Order);
            });
            obj.nextPage = newPageOrders;
          }

          return obj;
        }
      } else {
        deferred.resolve(null);
      }

      return deferred.promise;
    },

    checkSubFolder: function (aStepLogin, step, parent) {
      var currentUser = Auth.getCurrentUser();
      var subFoldersNameList = iGovNavbarHelper.getSubFolders(true);
      var referent;
      if ($cookies.get('referent')){
        referent = JSON.parse($cookies.get('referent'));
      }
      for (var i=0; i<aStepLogin.length; i++) {
        if (aStepLogin[i].aUser && aStepLogin[i].aUser.length !== 0) {
          var userArray = aStepLogin[i].aUser;
          for (var u=0; u<userArray.length; u++) {
            if ((userArray[u].sLogin === currentUser.id || (referent && userArray[u].sLogin === referent.sLogin)) && aStepLogin[i].oDocumentStepType && aStepLogin[i].oDocumentStepType.sName) {
              if (aStepLogin[i].sKeyStep === step) {
                var subName = aStepLogin[i].oDocumentStepType.sName.toLowerCase();
                var visible = !!(aStepLogin[i].oDocumentStepType.bFolder && subFoldersNameList && subFoldersNameList.indexOf(subName) > -1);
                return {
                  name: subName,
                  parent: parent,
                  isVisible: visible
                };
              }
            }
          }
        }
      }
      return false;
    },

    taskFormStructureConvert: function (array) {
      var converted = [];
      angular.forEach(array, function(item) {
        var obj = {};
        for (var name in item) {
          if (item.hasOwnProperty(name)) {
            if (name === 'mEnum') {
              var enumArrayName = 'enumValues';
              var newEnumValuesArray = [];
              for (var e in item[name]) {
                if (item[name].hasOwnProperty(e)) {
                  newEnumValuesArray.push({
                    id: e,
                    name: item[name][e]
                  });
                }
              }
              obj[enumArrayName] = newEnumValuesArray;
            } else if (name !== 'sID') {
              var changed = name.charAt(1).toLowerCase() + name.slice(2);
              obj[changed] = item[name];
            } else if (name === 'sID') {
              var id = name.slice(1).toLowerCase();
              obj[id] = item[name];
            }
          }
        }
        converted.push(obj);
      });
      return converted;
    }
  }
}]);
