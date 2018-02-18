angular.module('dashboardJsApp').directive('dateDivider', [function() {
    return {
      restrict: 'A',
      link: function(scope, elem, attr) {
        var dates = {
          '01': 'січ',
          '02': 'лют',
          '03': 'бер',
          '04': 'квіт',
          '05': 'трав',
          '06': 'черв',
          '07':'лип',
          '08':'серп',
          '09': 'вер',
          '10': 'жовт',
          '11': 'лист',
          '12': 'груд'
        };

        var time = 'sCreateTime';

        if (scope.sSelectedTask === 'control' || scope.sSelectedTask === 'execution')
          time = 'datePlan';

        var prev = scope.$index - 1,
            current = scope.$index;

        if (scope.tasks[current][time]){
          var p = scope.tasks[prev] && scope.tasks[prev][time] ? scope.tasks[prev][time].split('T')[0] : null,
            prevDay = p ? p.split('-')[2] : null;

          var c = scope.tasks[current][time].split('T')[0],
            correctC = c.split('-'),
            currentMonth = correctC[1],
            currentDay = correctC[2],
            currentYear = correctC[0];
        }

        var deviderPrefixText = 'від ';
        if(location.href.indexOf('tasks') > -1 && (location.href.indexOf('execution') > -1 || location.href.indexOf('control') > -1))
          deviderPrefixText = 'до ';


        if( prevDay !== currentDay ){
          angular.element(elem).append('<div style="font-size: 13px; position:relative; display: block;text-align:right;margin-top: 20px;padding-right: 20px;">'+ deviderPrefixText + currentDay + " " + dates[currentMonth]  + ' ' + currentYear + '</div>')
        }
      }
    };
  }
]);
