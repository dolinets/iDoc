'use strict';

angular.module('dashboardJsApp')
	.config(function($stateProvider) {
    $stateProvider.state('main',{
      url: "/login",
        templateUrl: 'app/main/main.html',
        controller: 'MainCtrl'
    });
	});
