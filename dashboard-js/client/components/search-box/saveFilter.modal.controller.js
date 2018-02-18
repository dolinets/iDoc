'use strict';

angular.module('dashboardJsApp')
  .controller('SaveFilterModalCtrl', function ($scope, $modalInstance) {

    $scope.save = function () {
      var currentFilter = {
        sName: $scope.filterName,
        options: $scope.options
      }
      var listOfFilters = [];
      if (localStorage.getItem('listOfFilters')){
        listOfFilters = JSON.parse(localStorage.getItem('listOfFilters'));
      }
      listOfFilters.push(currentFilter);
      localStorage.setItem('listOfFilters', JSON.stringify(listOfFilters));
      console.log(localStorage.getItem('listOfFilters'));
      $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  });
