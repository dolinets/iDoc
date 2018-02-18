angular.module('dashboardJsApp').directive('issueForRead', ['Issue', '$http', '$sce', 'CurrentServer', function (Issue, $http, $sce, CurrentServer) {
  return {
    restrict: 'EA',
    templateUrl: 'components/issueBlock/issueForRead.template.html',
    link: function (scope) {
      scope.html = function (text) {
        return $sce.trustAsHtml(text);
      };

      scope.usersArray = [];
      //scope.lowestOrder = searchMinOrder();
      scope.taskServer = CurrentServer.getServer();

      function fillUsersObject() {
        angular.forEach(scope.taskData.aProcessSubjectTask, function (issue) {
          var tempArr = [];
          angular.forEach(issue.aProcessSubject, function (user) {
            if (user.sLoginRole === 'Controller') {
              tempArr.unshift(user);
            } else {
              if (user.aProcessSubjectChild) {
                var child = angular.copy(user.aProcessSubjectChild),
                  parent = angular.copy(user),
                  controllerKey;

                parent.aProcessSubjectChild = [];
                tempArr.push(parent);

                angular.forEach(child, function (c, key, obj) {
                  if (c.sLoginRole === 'Controller') {
                    if (tempArr[tempArr.length - 1].sLogin === c.sLogin) {
                      if (c.oProcessSubjectStatus && c.oProcessSubjectStatus.sName && c.sText) {
                        tempArr[tempArr.length - 1].oProcessSubjectStatus = c.oProcessSubjectStatus;
                        tempArr[tempArr.length - 1].sText = c.sText;
                      }
                    }
                    controllerKey = key;
                  } else {
                    obj[key].isDelegated = parent.sLogin;
                  }
                });
                if (controllerKey){
                  child.splice(controllerKey, 1);
                }
                tempArr[tempArr.length - 1].aProcessSubjectChild = tempArr[tempArr.length - 1].aProcessSubjectChild.concat(child);
              } else {
                tempArr.push(user);
              }
            }
          });
          scope.usersArray.push(tempArr);
          scope.usersArray[0] = _.sortBy(scope.usersArray[0], 'nOrder');
        });
      }

      scope.isMinOrder = function(currUserOrder, index) {
        var lowestOrder;

        if (scope.taskData.aProcessSubjectTask && scope.taskData.aProcessSubjectTask[index]) {
          angular.forEach(scope.taskData.aProcessSubjectTask[index].aProcessSubject, function (user) {
            if (user.sLoginRole !== 'Controller') {
              if (!lowestOrder) {
                lowestOrder = user;
              } else {
                if (lowestOrder.nOrder > user.nOrder) {
                  lowestOrder = user;
                }
              }
            }
          });

          return lowestOrder.nOrder === currUserOrder;
        }
      }

      fillUsersObject();

      scope.getIssueType = function (type) {
        switch (type) {
          case 'string':
            return 'Документ';
          case 'textArea':
            return 'Текстове повiдомлення';
          case 'file':
            return 'Файл';
        }
      };
      scope.convertDate = function (i) {
        var date = i.split(' ')[0];
        var splittedDate = date.split('-');
        return splittedDate[2] + '.' + splittedDate[1] + '.' + splittedDate[0];
      };
      scope.convertDay = function (day) {
          return day;
      };
      scope.convertInitials = function (init) {
        var split = init.split('');
        var result = [split[0]];
        for (var i=0; i<split.length; i++) {
          if(split[i] === ' '){
            result.push(y[i + 1] + '.');
            break;
          }
        }
        return result.join('.');
      };

      scope.takeTheKeyFromJSON = function (item) {
        return JSON.parse(item).sKey;
      };

      scope.takeTheFileNameFromJSON = function (item) {
        return JSON.parse(item).sFileNameAndExt;
      }
    }
  }
}]);
