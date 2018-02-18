angular.module('siteiDoc')
  .directive('mainPage',
    function() {
        return {
            restrict: 'EA',
            templateUrl: '../components/mainPage.html',
            replace: true,
            transclude: true,
            controller : "mainController",
            scope: true,
            link: function(scope, elem, attr) {

console.log(scope);

            }
        };
    }
);
