'use strict';

angular.module('dashboardJsApp').directive('printModal', ['$window', '$rootScope', 'snapRemote', 'signDialog', 'generationService', '$base64', function ($window, $rootScope, snapRemote, signDialog, generationService, $base64) {
  return {
    restrict: 'E',
    link: function (scope, element, attrs, ngModel) {
      scope.signedContent = null;
      scope.dialogStyle = {};
      if (attrs.width)
        scope.dialogStyle.width = attrs.width;
      if (attrs.height)
        scope.dialogStyle.height = attrs.height;

      scope.hideModal = function () {
        scope.printModalState.show = false;
        scope.convertDisabledEnumFiedsToReadonlySimpleText();
        scope.signedContent = null;
      };
      scope.printContent = function () {
        var elementToPrint = element[0].getElementsByClassName('ng-modal-dialog-content')[0];
        var printContents = setImgAbsoluteUrl(elementToPrint.innerHTML);
        var icludedStyle = '<style>.col-xs-offset-6{margin-left: 306pt; width: 300px; text-align: left;} .row{width:100%; float:left;} .col-xs-6{float:left; width:46%; padding: 0 2%} .col-xs-offset-6 .col-xs-6{width:auto!important;}</style>';
        var popupWin = window.open('', '_blank');
        popupWin.document.open();
        popupWin.document.write('<html><head><link rel="stylesheet" type="text/css" href="style.css" />'+icludedStyle+'</head><body onload="window.print()">' + printContents + '</html>');
        popupWin.document.close();
        scope.hideModal();
      };

      //scope.model.printTemplate.printFormLinkedToFileField = undefined;
      scope.showSignAndUploadButton = function () {
        if(!scope.model || !scope.model.printTemplate) return false;
        var aFileFields = scope.taskForm.filter(function (field) {
          return field.type === 'file' && field.options.hasOwnProperty('sID_Field_Printform_ForECP');
        });
        for(var j = 0; j < aFileFields.length; j++){
          if(aFileFields[j].options['sID_Field_Printform_ForECP'] === scope.model.printTemplate.id){
            //scope.model.printTemplate.printFormLinkedToFileField = aFileFields[j].id;
            return !(aFileFields[j].value && aFileFields[j].value.length > 0);
          }
        }
        return false;
      };

      scope.signAndUpload = function () {
        var aFiles = [];
        if(scope.model.printTemplate.oEDS && scope.model.printTemplate.oEDS.oSignedContent && scope.model.printTemplate.oEDS.oSignedContent.sign){
          scope.model.printTemplate.oEDS.oSignedFile = generationService.getSignedFile(scope.model.printTemplate.oEDS.oSignedContent.sign, scope.model.printTemplate.printFormLinkedToFileField);
          aFiles.push(scope.model.printTemplate.oEDS.oSignedFile);
          scope.upload(aFiles, scope.model.printTemplate.printFormLinkedToFileField);
        } else {
          var elementToPrint = element[0].getElementsByClassName('ng-modal-dialog-content')[0];
          var printContents = '<html><head><meta charset="utf-8"></head><body>' + elementToPrint.innerHTML + '</body></html>';

          generationService
            .generatePDFFromHTML(printContents)
            .then(function (pdfContent) {
              var toSign = {id: "", content: pdfContent.base64, base64encoded: true};
              signDialog.signContent(toSign,
                function (signedContent) {

                  if(!scope.model.printTemplate.oEDS) scope.model.printTemplate.oEDS = {};
                  scope.model.printTemplate.oEDS.sSignedContentName = "document" + new Date().getMilliseconds();
                  scope.model.printTemplate.oEDS.sSignedContentURL = generationService.getSignedFileLink(signedContent.sign);
                  scope.model.printTemplate.oEDS.oSignedContent = signedContent;
                  scope.model.printTemplate.oEDS.oSignedFile = generationService.getSignedFile(signedContent.sign, scope.model.printTemplate.printFormLinkedToFileField);

                  aFiles.push(scope.model.printTemplate.oEDS.oSignedFile);
                  scope.upload(aFiles, scope.model.printTemplate.printFormLinkedToFileField);
                  scope.model.printTemplate.isPrintFormNeverUploaded = false
                }, function () {
                  console.log('Sign Dismissed');
                  //todo dissmiss sign
                }, function (error) {
                  //todo react on error during sign
                }, 'ng-on-top-of-modal-dialog modal-info');
            });
        }
      };

      scope.signWithEDS = function () {
        if(scope.model.printTemplate.oEDS){
          if(scope.model.printTemplate.oEDS.sSignedContentURL){
            return;
          }
          if(scope.model.printTemplate.oEDS.oSignedContent){
            scope.model.printTemplate.oEDS.sSignedContentURL = generationService.getSignedFileLink(scope.model.printTemplate.oEDS.oSignedContent.sign);
            return;
          }
        }

        var elementToPrint = element[0].getElementsByClassName('ng-modal-dialog-content')[0];
        var printContents = '<html><head><meta charset="utf-8"></head><body>' + elementToPrint.innerHTML + '</body></html>';

        generationService
          .generatePDFFromHTML(printContents)
          .then(function (pdfContent) {
            var toSign = {id: "", content: pdfContent.base64, base64encoded: true};
            signDialog.signContent(toSign,
              function (signedContent) {
                if(!scope.model.printTemplate.oEDS) scope.model.printTemplate.oEDS = {};
                scope.model.printTemplate.oEDS.sSignedContentName = "document" + new Date().getMilliseconds();
                scope.model.printTemplate.oEDS.sSignedContentURL = generationService.getSignedFileLink(signedContent.sign);
                scope.model.printTemplate.oEDS.oSignedContent = signedContent;
              }, function () {
                console.log('Sign Dismissed');
                //todo dissmiss sign
              }, function (error) {
                //todo react on error during sign
              }, 'ng-on-top-of-modal-dialog modal-info');
          });
      };

        function toggleMenu(status) {
          if(typeof status === 'boolean') {
            if(status) {
              scope.isMenuOpened = true;
              snapRemote.open('left');
            } else {
              scope.isMenuOpened = false;
              snapRemote.close();
            }
            localStorage.setItem('menu-status', JSON.stringify(status));
          }
        }

        function setImgAbsoluteUrl(html) {
          var origin = document.location.origin;

          return html.replace(/\<img src=\"/g, '<img src="' + origin + '/');
        }

        var menuStatus = localStorage.getItem('menu-status');
        if(menuStatus) {
          var status = JSON.parse(menuStatus);
          toggleMenu(status);
        } else {
          scope.isMenuOpened = false;
          snapRemote.close();
        }
      },
        templateUrl: 'components/print/PrintModal.html',
        replace: true,
        transclude: true
    };
}]);
