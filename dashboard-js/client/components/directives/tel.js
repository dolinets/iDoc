angular.module('dashboardJsApp').directive('ngTelField', [function() {
  'use strict';
  return {
    require: '?ngModel',
    restrict: 'A',
    link: function link(scope, element, attrs, ngModel) {
      if (!ngModel) {
        return;
      }

      scope.validate = function(evt) {
        var theEvent = evt || window.event;
        var key = theEvent.keyCode || theEvent.which;
        key = String.fromCharCode( key );
        var regex = /[0-9]|\./;
        if( !regex.test(key) ) {
          theEvent.returnValue = false;
          if(theEvent.preventDefault) theEvent.preventDefault();
        }
      };

      var elmt = angular.element(element);

      ngModel.$validators.tel = function(modelValue, viewValue, markerOptions) {
        if (!modelValue) {
          return false;
        }

        var bValid = elmt.intlTelInput('isValidNumber');

        bValid = bValid && (modelValue.indexOf('380') === 0 ? modelValue.length >= 12 : true);

        return bValid;
      };
      elmt.intlTelInput(scope.$eval(attrs.ngTelField));
    }
  };
}]);
