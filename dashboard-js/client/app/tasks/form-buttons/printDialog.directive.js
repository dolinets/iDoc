'use strict';
angular.module('dashboardJsApp').directive('printDialog', [
  'PrintTemplateService', '$stateParams',
  function(PrintTemplateService, $stateParams) {
    return {
      restrict: 'E',
      scope: false,
      templateUrl: 'app/tasks/form-buttons/printDialog.html',
      link: function($scope, $elem, attrs) {
        // console.log('printDialog $scope, attrs', $scope, attrs);
        // dirty hack to allow this directive be parsed on print form upload in task.service.js
        if ($scope.containsPrintTemplate && $scope.containsPrintTemplate()) {
          $scope.processedPrintTemplate = $scope.getPrintTemplate();
        }
        $scope.$watch('printModalState.show', function(newval, oldval) {
          if (oldval != newval) {
            if (newval == true) {
              // load form from api or use default as a fallback
              if (!$scope.model.printTemplate) {
                $scope.processedPrintTemplate = '';
                // if no printTemplate selected - use default print form
                return;
              }
              $scope.processedPrintTemplate = 'Завантаження форми. Зачекайте, будь ласка.';
              //var templatePromise = PrintTemplateService.getPrintTemplate($scope.selectedTask, $scope.taskForm, $scope.model.printTemplate.id, $scope.lunaService);
              var templatePromise = null;
              if(($scope.model.printTemplate.type && $scope.model.printTemplate.type !== "prints") || ($scope.model.printTemplate.sType && $scope.model.printTemplate.sType !== "prints")) {
                if ($stateParams.type == 'docHistory'){
                  templatePromise = PrintTemplateService.getPrintTemplate($scope.taskForm, $scope.model.printTemplate.sId, $scope.documentLogins, $scope.taskData.aProcessSubjectTask);
                } else {
                  templatePromise = PrintTemplateService.getPrintTemplate($scope.taskForm, $scope.model.printTemplate.id, $scope.documentLogins, $scope.taskData.aProcessSubjectTask);
                }
              }
              else {
                 templatePromise = PrintTemplateService.getPrintTemplateByObject($scope.taskForm, $scope.model.printTemplate.value );
              }
              templatePromise.then(function(template) {
                if ($('#sCurrency').val() === 'string:sUAH' || $('#sCurrency').val() === 'string:sUSD' || $('#sCurrency').val() === 'string:sEUR' || $('#sCurrency').val() === 'string:sRUR' || $('#sCurrency').val() === 'string:sGBP' || $('#sCurrency').val() === 'string:sCHF' || $('#sCurrency').val() === 'string:sYEN' || $('#sCurrency').val() === 'string:sSEK') {
                      template = '<div text="#000000"></div><div style="text-align:center"><b>Заява<br />на ' + ($('#sType').val() === '' ? '' : ($('#sType').val() === 'string:sAdd' ? 'перевезення (пiдкрiплення)' : 'перевезення (вивiз)')) + ' готівкових  цінностей</b>' + '</div><br/><br/><br/><div class="layout ta-j w800p"><div class="ta-j pl-paragraph text"><b>Просимо ' + ($('#sDateAction').val()) + ' року здійснити   ' + ($('#sType').val() === '' ? '' : ($('#sType').val() === 'string:sAdd' ? 'перевезення (пiдкрiплення)' : 'перевезення (вивiз)')) + ' готівкових   цінностей у  сумі  ' + ($('#sSum').val().split(/[A-zА-я]/g) === null ? '' : $('#sSum').val().split(/[A-zА-я]/g)) + ' ' + ($('#sCurrency').val() === 'string:sUAH' ? 'ГРН' : ($('#sCurrency').val() === 'string:sUSD' ? 'USD' : ($('#sCurrency').val() === 'string:sEUR' ? 'Евро' : ($('#sCurrency').val() === 'string:sRUR' ? 'РУБ' : ($('#sCurrency').val() === 'string:sGBP' ? 'Фунт стерлiнгiв' : ($('#sCurrency').val() === 'string:sCHF' ? 'Швейцарський франк' : ($('#sCurrency').val() === 'string:sYEN' ? 'Иена' : ($('#sCurrency').val() === 'string:sSEK' ? 'Шведська крона' : '')))))))) + ' з каси відділення № ' + ($('#asNumberOtpravitelja').val() != undefined ? (($('#asNumberOtpravitelja').val() === 'string:enum1a' ? '1' : ($('#asNumberOtpravitelja').val() === 'string:enum2a' ? '37' : ($('#asNumberOtpravitelja').val() === 'string:enum3a' ? '38' : ($('#asNumberOtpravitelja').val() === 'string:enum4a' ? '66' : '')))) + ' ' + ($('#sAdressOtpravitelja').val() + ';')) : '') + ' ' + ($('#sName_OutBrunches0').val() != undefined ? (($('#sName_OutBrunches0').text().split('Відділення')) + ($('#sID_Private_Source_OutBrunches0').val() + ';') + ($('#sName_OutBrunches1').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_OutBrunches1').text().split('Відділення') : '') + ($('#sID_Private_Source_OutBrunches1').val() === undefined ? '' : $('#sID_Private_Source_OutBrunches1').val() + ';') + ($('#sName_OutBrunches2').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_OutBrunches2').text().split('Відділення') : '') + ($('#sID_Private_Source_OutBrunches2').val() === undefined ? '' : $('#sID_Private_Source_OutBrunches2').val() + ';') + ($('#sName_OutBrunches3').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_OutBrunches3').text().split('Відділення') : '') + ($('#sID_Private_Source_OutBrunches3').val() === undefined ? '' : $('#sID_Private_Source_OutBrunches3').val() + ';') + ($('#sName_OutBrunches4').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_OutBrunches4').text().split('Відділення') : '') + ($('#sID_Private_Source_OutBrunches4').val() === undefined ? '' : $('#sID_Private_Source_OutBrunches4').val() + ';')) : '') +
                          ' ПуАТ «КБ» Акордбанк» ' + ' до каси головного банку/відділення № ' + ($('#asNumberOtrymuvacha').val() != undefined ? (($('#asNumberOtrymuvacha').val() === 'string:enum1' ? '1' : ($('#asNumberOtrymuvacha').val() === 'string:enum2' ? '37' : ($('#asNumberOtrymuvacha').val() === 'string:enum3' ? '38' : ($('#asNumberOtrymuvacha').val() === 'string:enum4' ? '66' : '')))) + ' ' + ($('#sAdressOtrymuvacha').val() + ';')) : '') + ' ' + ($('#sName_Brunches0').val() != undefined ? (($('#sName_Brunches0').text().split('Відділення')) + ($('#sID_Private_Source_Brunches0').val() + ';') + ($('#sName_Brunches1').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_Brunches1').text().split('Відділення') : '') + ($('#sID_Private_Source_Brunches1').val() === undefined ? '' : $('#sID_Private_Source_Brunches1').val() + ';') + ($('#sName_Brunches2').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_Brunches2').text().split('Відділення') : '') + ($('#sID_Private_Source_Brunches2').val() === undefined ? '' : $('#sID_Private_Source_Brunches2').val() + ';') + ($('#sName_Brunches3').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_Brunches3').text().split('Відділення') : '') + ($('#sID_Private_Source_Brunches3').val() === undefined ? '' : $('#sID_Private_Source_Brunches3').val() + ';') + ($('#sName_Brunches4').text().split('Відділення') != '' ? ' відділення № ' + $('#sName_Brunches4').text().split('Відділення') : '') + ($('#sID_Private_Source_Brunches4').val() === undefined ? '' : $('#sID_Private_Source_Brunches4').val() + ';')) : '') + ' в м.Київ ПуАТ «КБ» Акордбанк» ' + '<br /><br /><br /> Додаткова інформація: ' + ($('#sInfo').val()) + '<br /><br />' + '</b><b>Відсоткова ставка купівлі/продажу готівкової валюти згідно тарифів Банку, складає ' + ($('#sPercent').val().split(/[A-zА-я]/g) === null ? '' : $('#sPercent').val().split(/[A-zА-я]/g)) + ' %</b>' + '<br /><br /><b>Служба інкасації ' + ($("#sCompOfIncass").val() === undefined ? '' : $("#sCompOfIncass").val()) + '</b><br />' + '<b>Дата доставки ' + ($('#sDateIncass').val() === undefined ? '' : $('#sDateIncass').val()) + '</b><br /><br /><b>Відповідальна особа:  ' + 'Завідувач каси' + ($('#iserId').text()) + ' відділення № ' + ($('#asNumberOtpravitelja').val() != undefined ? (($('#asNumberOtpravitelja').val() === 'string:enum1a' ? '1' : ($('#asNumberOtpravitelja').val() === 'string:enum2a' ? '37' : ($('#asNumberOtpravitelja').val() === 'string:enum3a' ? '38' : ($('#asNumberOtpravitelja').val() === 'string:enum4a' ? '66' : ''))))) : '') + ' ' + ($('#sName_OutBrunches0').val() != undefined ? ($('#sName_OutBrunches0').text().split('Відділення')) : '') + ($('#Executor').text().split('Введіть від 3-х символів') != '' ? $('#Executor').text().split('Введіть від 3-х символів') : $('#accountId').text()) + '</b><br /><br /><br />' + '<b>Керуючий відділенням № ' + ($('#asNumberOtrymuvacha').val() != undefined ? (($('#asNumberOtrymuvacha').val() === 'string:enum1' ? '1' : ($('#asNumberOtrymuvacha').val() === 'string:enum2' ? '37' : ($('#asNumberOtrymuvacha').val() === 'string:enum3' ? '38' : ($('#asNumberOtrymuvacha').val() === 'string:enum4' ? '66' : ''))))) : '') + ' ' + ($('#sName_Brunches0').val() != undefined ? ($('#sName_Brunches0').text().split('Відділення')) : '') + ($('#Controller').text().split('Введіть від 3-х символів') != '' ? $('#Controller').text().split('Введіть від 3-х символів') : 'Зеленська Анастасiя Олексiiвна') + '</b><br /><b>Тел. ____________</b><br /><br /><div class="w33p l ta-c"><span id="spansDateRozp"><b>' + ($('#sDateRegistration').val()) + '</b></span><b>р.</b></div></div></div>';
                      $scope.processedPrintTemplate = template;
                  } else {
                      $scope.processedPrintTemplate = template;
                  }
              }, function(error) {
                $scope.processedPrintTemplate = 'При завантаженні форми сталася помилка';
              });
            }
          }
        });
      }
    };
  }
]);
