angular.module('siteiDoc')
  .directive('modal',
    function() {
      return {
        restrict: 'EA',
        templateUrl: '../components/modal.html',
        replace: true,
        transclude: true,
        controller : "mainController",
        scope: true,
        link: function(scope, elem, attr) {


          jQuery.noConflict();
          jQuery(document).ready(function () {
            jQuery(".but-send1").click(function() {

              var res = scope.present_form.$valid;
              var name = jQuery('.present-send-name').val();
              var email = jQuery('.present-send-email').val();
              if (name  && email && res) {
                scope.message = 'Ваше сообщение успешно отправленно';

                jQuery('#modalAdd').modal('show');
              }

            });
          });


        }
      };
    }
  );



