angular.module('dashboardJsApp').controller('historyCtrl', ['$scope', 'taskData', 'lunaService', 'fieldsService', '$location', '$stateParams', 'eventService', '$state',
  function ($scope, taskData, lunaService, fieldsService, $location, $stateParams, eventService, $state) {
    $scope.taskData = taskData;
    $scope.tabHistoryAppeal = 'history';
    $scope.currentType = $location.hash();
    $scope.orderID = $stateParams.sID_Order ? $stateParams.sID_Order : null;
    var lastTasksResult;
    var historyPage = 0;
    $scope.historyMessageList = [];

    $scope.getMessageFileUrl = function (oMessage, oFile) {
      if(oMessage && oFile)
        return './api/tasks/' + $scope.nID_Process + '/getMessageFile/' + oMessage.nID + '/' + oFile.sFileName;
    };

    $scope.tabHistoryAppealChange = function (param) {
      $scope.tabHistoryAppeal = param;
    };

    $scope.creationDateFormatted = function (date) {
      return fieldsService.creationDateFormatted(date);
    };

    $scope.isLoadMoreMessagesAvailable = function () {
      return lastTasksResult !== null && lastTasksResult.nStart + lastTasksResult.nSize < lastTasksResult.nTotalCount;
    };

    $scope.loadMoreHistoryMessages = function () {
      $scope.messageLoading = true;
      eventService.getHistoryEvents(historyPage, $scope.orderID).then(function (res) {
        if (res) {
          $scope.messageLoading = false;
          lastTasksResult = res;
          angular.forEach(res.aoHistoryEvent, function (value) {
            $scope.historyMessageList.push(value);
          });
          historyPage++;
        }
      }).finally(function () {
        $scope.messageLoading = false;
      });
    };

    $scope.whenScrolledHistory = function () {
      if ($scope.messageLoading === false && $scope.isLoadMoreMessagesAvailable()) {
        $scope.loadMoreHistoryMessages();
      }
    };

    $scope.loadMoreHistoryMessages();

    $scope.historyBack = function() {
      $state.go('tasks.typeof', { tab: 'tasks', type: $stateParams.type || $location.hash() });
    };
  }]);
