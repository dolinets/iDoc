/**
 * Created by irish on 10.11.17.
 */
'use strict'

'use strict'

angular.module('dashboardJsApp')
    .directive('modalDialogStaff',
        function() {
            return {
                restrict: 'EA',
                scope: {
                    show: '='
                },
                templateUrl: 'app/staff/password/staffChangePass.html',
                controller: 'staffPassword',
                transclude: true,
                link: function(scope, elem, attrs) {


                }

            };
        });