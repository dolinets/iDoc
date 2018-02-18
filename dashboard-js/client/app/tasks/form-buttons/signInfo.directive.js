angular.module('dashboardJsApp').directive('signInfo', ['$state', '$templateCache', '$rootScope', '$http', 'Modal', 'CurrentServer',
  'DocumentsService', 'Auth', '$cookies',
  function ($state, $templateCache, $rootScope, $http, Modal, CurrentServer, DocumentsService,  Auth, $cookies) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/signInfo.html',
      link: function (scope) {
        scope.data = {message: ""};
        $rootScope.spinner = false;

        var taskServer = CurrentServer.getServer();

        var user = $cookies.getObject('user');

        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : user.id;

        scope.sendSignInfo = function () {
          scope.modalSpinner = true;
          var options = {
            nID_Process_Activiti: scope.taskData.oProcess.nID,
            sLogin: user.id,
            sBody: scope.data.message,
            sKeyGroup: user.id,
            sLoginReferent: sCurrReferent
          };

          if (taskServer.another){
            options.taskServer = taskServer.name;
          }

          scope.assignAndSubmitDocument(scope.form, false, false, scope.sBody);

          if(scope.form.$invalid){
            scope.modalSpinner = false;
            scope.closeModalByButton();
          }
          else{
            $http.post('api/chat/setProcessChatMessage', options).success(function () {
              scope.modalSpinner = false;
              Modal.inform.success()("Зауваження додано");
              scope.closeModalByButton();
            });
          }
        };

        scope.cancelDocumentSubmit = function() {
          var cancelOptions = {
            snID_Process_Activiti: scope.nID_Process,
            sKey_Step: scope.taskData.mProcessVariable.sKey_Step_Document,
            sKey_Group: Auth.getCurrentUser().id,
            nID_Task: scope.taskId
          };

          var options = {
            nID_Process_Activiti: scope.taskData.oProcess.nID,
            sLogin: user.id,
            sBody: scope.data.message,
            sKeyGroup: user.id,
            sLoginReferent: sCurrReferent
          };

          DocumentsService.cancelDocumentSubmit(cancelOptions).then(function(response) {
            Modal.inform.signed(function() {})('Підпис знято');
            if (scope.data.message){
              $http.post('api/chat/setProcessChatMessage', options).success(function () {
                scope.modalSpinner = false;
                Modal.inform.success()("Зауваження додано");
                scope.closeModalByButton();
              });
            }
          });
        };

        correctHeight(document.getElementById('askMessage'));

        function correctHeight(elem) {
          var stopPos;

          elem.onmousedown = mouseDown;

          function mouseDown() {
            stopPos = document.getElementsByClassName('button-block-pull-right')[0].getBoundingClientRect().top;

            document.onmouseup = closeDragElement;
            document.onmousemove = elementDrag;
          }

          function elementDrag() {
            var bottom = elem.getBoundingClientRect().bottom;
            if (bottom >= stopPos) {
              elem.style.height = (parseInt(elem.style.height, 10)-5) + 'px';
              elem.style.resize = 'none';
            }
          }

          function closeDragElement() {
            document.onmouseup = null;
            document.onmousemove = null;

            elem.style.resize = 'vertical';
          }
        }
        
      }
    };
  }]);
