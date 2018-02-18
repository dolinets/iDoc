angular.module('dashboardJsApp').service('fieldsService', ['FieldMotionService', function (FieldMotionService) {
  return {
    isFieldVisible : function(item, taskForm, createTask) {
      if (item.sId && item.oValue) { // for history tab;
        return true;
      } else {
        var bVisible = item.id !== 'processName' && (FieldMotionService.FieldMentioned.inShow(item.id) ?
          FieldMotionService.isFieldVisible(item.id, taskForm) : true);
        if(item.options && item.options.hasOwnProperty('bVisible')){
          bVisible = bVisible && item.options['bVisible'];
        } else if (createTask && item.name && item.name.indexOf('bVisible=false') > -1) {
          bVisible = false;
        }
        return bVisible;
      }
    },

    creationDateFormatted: function (date) {
      if (date){
        var unformatted = date.split(' ')[0];
        var splittedDate = unformatted.split('-');
        return splittedDate[2] + '.' + splittedDate[1] + '.' + splittedDate[0];
      }
    }
  }
}]);
