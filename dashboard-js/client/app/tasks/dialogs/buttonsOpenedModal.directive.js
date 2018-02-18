angular.module('dashboardJsApp').directive('buttonsOpenedModal', ['ExecAndCtrlService', function (ExecAndCtrlService) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/dialogs/buttonsOpenedModal.template.html',
    link: function (scope, element, attrs) {
      for (var choice in scope.execCtrlModals) {
        if (scope.execCtrlModals.hasOwnProperty(choice) && scope.execCtrlModals[choice]) {
          scope.selectedIssue = ExecAndCtrlService.getIssueName(choice);
        }
      }

      function getStyle(elem) {
        return window.getComputedStyle ? getComputedStyle(elem, "") : elem.currentStyle;
      }

      if (attrs['draggableoff'] && attrs['draggableoff'] === 'true') {
        angular.element('#draggable-dialog').removeAttr('id');
        angular.element('#dragger').css('cursor', 'default');
      }

      var dragWindow = document.getElementById('draggable-dialog');

      if (dragWindow) {
        dragElement(dragWindow);
      }

      function dragElement(elmnt) {
        var shiftX, shiftY, rect, lastPos = {};

        elmnt.onmousedown = dragMouseDown;

        function dragMouseDown(e) {
          e = e || window.event;

          if (e.target.id !== 'dragger')
            return;

          document.body.style.userSelect = 'none';

          elmnt.style.position = 'absolute';
          var style = getStyle(elmnt);

          rect = elmnt.getBoundingClientRect();
          shiftX = e.clientX - rect.x + parseInt(style.marginLeft, 10);
          shiftY = e.clientY - rect.y + parseInt(style.marginTop, 10);

          document.onmouseup = closeDragElement;
          // call a function whenever the cursor moves:
          document.onmousemove = elementDrag;
        }

        function elementDrag(e) {
          e = e || window.event;

          lastPos.y = elmnt.style.top;
          lastPos.x = elmnt.style.left;

          elmnt.style.top = e.clientY - shiftY -63 + "px";
          elmnt.style.left = e.clientX - shiftX - 266 + "px";

          if (parseInt(elmnt.style.top, 10) <= -145)
            elmnt.style.top = lastPos.y;

          if (scope.isMenuOpened && parseInt(elmnt.style.left, 10) <= -136)
            elmnt.style.left = lastPos.x;
        }

        function closeDragElement() {
          /* stop moving when mouse button is released:*/
          document.onmouseup = null;
          document.onmousemove = null;
          document.body.style.userSelect = 'auto';
        }

      }

    }
  }
}]);
