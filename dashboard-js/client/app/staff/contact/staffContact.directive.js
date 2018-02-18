/**
 * Created by irish on 10.11.17.
 */
'use strict'


angular.module('dashboardJsApp')
    .directive('modalDialogStaffContact',
        function() {
            return {
                restrict: 'EA',
                scope: {
                    show: '='
                },
                templateUrl: 'app/staff/contact/staffContact.html',
                controller: 'staffContact',

                transclude: true,
                link: function(scope, elem, attrs) {


                }

            };
        });