var siteiDoc = angular.module('siteiDoc', []);

siteiDoc.controller('mainController', ['$scope',  function ($scope) {
  $scope.presentation = {};
  $scope.message = 'Ваше сообщение успешно отправленно';

}]);
