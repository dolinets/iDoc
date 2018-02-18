(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('ReportsCtrl', reportsCtrl);

  reportsCtrl.$inject = ['$scope', 'reports', 'DocumentsService', '$cookies', 'processes'];
  function reportsCtrl($scope, reports, DocumentsService, $cookies, processes) {
    $scope.export = {};
    $scope.exportOld = {};
    $scope.export.bExportAll = false;
    $scope.export.bExportAllOld = false;

    $scope.date = {
        options: {
            timePicker: false
        }
    };

    var user = JSON.parse($cookies.get('user')).id;
    if (user)
      DocumentsService.isUserHasDocuments(user).then(function (res) {
        if (res && res.length) {
          $scope.processesList = res;
          $scope.export.sBP = $scope.processesList[0];

          $scope.initExportUrl();
        }
      });

    processes.getBPs_ForExport().then(
      function (data) {
        $scope.processesListOld = data;

        if (typeof $scope.processesListOld === 'undefined') {
          $scope.processesListOld = "error";
        } else if (typeof $scope.processesListOld !== 'undefined' && $scope.processesListOld.length > 0) {
          $scope.exportOld.sBP = $scope.processesListOld[0].sID_BP;
          $scope.initExportUrlOld();
        }
      }, function () {
        $scope.processesListOld = "error";
    });

    $scope.processesLoaded = function() {
      return !!$scope.processesList;
    };

    $scope.processesLoadError = function() {
      return !!($scope.processesList && $scope.processesList == "error");
    };

    $scope.getExportLink = function () {
      return $scope.export.exportURL;
    };

    $scope.initExportUrl = function () {
      $scope.export.exportURL = reports.getExportLink({ from: $scope.export.from, to: $scope.export.to, sBP: $scope.export.sBP, bExportAll: $scope.export.bExportAll});
    };

    //old stuff
    $scope.getExportLinkOld = function () {
      return $scope.exportOld.exportURL;
    };

    $scope.processesLoadedOld = function() {
      return !!$scope.processesListOld;
    };

    $scope.processesLoadErrorOld = function() {
      return !!($scope.processesListOld && $scope.processesListOld == "error");
    };

    $scope.initExportUrlOld = function () {
      reports.exportLink({ from: $scope.exportOld.from, to: $scope.exportOld.to, sBP: $scope.exportOld.sBP, bExportAll: $scope.exportOld.bExportAllOld},
        function (result) {
          $scope.exportOld.exportURL = result;
      });
    };

  }
})();
