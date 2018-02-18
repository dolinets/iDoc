angular.module('dashboardJsApp').directive('headerDestination', function() {
    return {
        restrict: 'E',
        templateUrl: 'app/tasks/headerDestination.html',
        replace: true
    }
});