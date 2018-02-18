// changes full name to short name, example: Кирилл Леонидович Филоненко >>> Филоненко К.Л.

angular.module('dashboardJsApp')
    .filter('getShortName', function() {
        return function(name) {
            var nameArray = name.split(' ');
            // nameArray.unshift(nameArray[nameArray.length - 1]);
            // nameArray.pop(nameArray[nameArray.length - 1]);
            for (var i = 1; i < nameArray.length; i++) {
                nameArray[i] = nameArray[i].slice(0, 1).concat(".");
            }
            return nameArray.join(" ");
        }
    });
