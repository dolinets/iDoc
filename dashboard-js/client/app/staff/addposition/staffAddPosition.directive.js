/**
 * Created by irish on 10.11.17.
 */
'use strict'

angular.module('dashboardJsApp')
    .directive('modalDialogStaffAddPosition',
        function() {
            return {
                restrict: 'EA',
                scope: {
                    show: '='
                },
                templateUrl: 'app/staff/addposition/staffAddPosition.html',
                controller: 'staffAddPosition',

                transclude: true,
                link: function(scope, elem, attrs) {



                }

            };
        });