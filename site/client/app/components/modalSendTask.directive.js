angular.module('siteiDoc')
  .directive('modalSendTask',
    function() {
      return {
        restrict: 'EA',
        templateUrl: '../components/modalSendTask.html',
        replace: true,
        transclude: true,
        controller : "mainController",
        scope: true,
        link: function(scope, elem, attr) {

          jQuery.noConflict();
          jQuery(document).ready(function () {
            jQuery(".but-send").click(function() {

              var res = scope.send_task.$valid;
              var name = jQuery('.present-send-names').val();
              var phone = jQuery('.present-send-phones').val();
              var email = jQuery('.present-send-emails').val();
                if (name  && email && res && phone) {
                scope.message = 'Ваше сообщение успешно отправленно';

                jQuery('#modalSend').modal('show');
                  //jQuery('.send').css('position', 'relative');
              }
            });
          });
        }
      };
    });



