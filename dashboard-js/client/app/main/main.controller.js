'use strict';

angular.module('dashboardJsApp')
	.controller('MainCtrl', function($scope, $http, $modal) {
		$scope.weblinks = [{
			name: 'Центральний портал громадян',
			link: 'https://igov.org.ua',
			info: 'Портал громадян'
		}];

		$scope.requestPassword = function () {
		  $modal.open({
        animation: true,
        templateUrl: 'components/modal/modal.getPass.html',
        controller: 'modalGetPassController',
        scope: $scope,
        windowClass: 'modal-success'
      });
    };
	});
