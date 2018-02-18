'use strict';
angular.module('dashboardJsApp').directive('fileField', function($modal, $http, generationService, Modal, ScannerService, $base64, CurrentServer) {
  return {
    require: 'ngModel',
    restrict: 'E',
    link: function(scope, element, attrs, ngModel) {
      var fileField = element.find('input');
      var nMaxFileSizeLimit = 10; // max upload file size = 10 MB
      var aAvailableFileExtensions = ["bmp", "gif", "jpeg", "jpg", "png", "tif", "doc", "docx", "odt", "rtf", "pdf", "xls", "xlsx", "xlsm", "xml", "ods", "sxc", "wks", "csv", "zip", "rar", "7z", "p7s"];
      scope.taskServer = CurrentServer.getServer();

      fileField.bind('change', function(event) {
        scope.$apply(function() {
          if (event.target.files && event.target.files.length > 0) {
            var aFilteredFiles = [];
            for(var nFileIndex = 0; nFileIndex < event.target.files.length; nFileIndex++){
              var sFileName = event.target.files[nFileIndex].name;
              var nFileSize = event.target.files[nFileIndex].size;
              var message = null;
              if (nFileSize > (nMaxFileSizeLimit * 1024 * 1024)){
                console.warn("File " + sFileName + " is very big for upload");
                message = "Розмір завантажуємого файлу " + sFileName + " " + (nFileSize / (1024 * 1024)).toFixed(1) + " МБайт перевищує допустимий.\n " +
                  "Для завантаження дозволяються файли розміром не більше " + nMaxFileSizeLimit + " МБайт.";
                Modal.inform.warning()(message);
              } else if (!verifyExtension(sFileName)) {
                console.warn("File " + sFileName + "is not supported");
                var extList = convertAvailableExtensionArrayToString();
                message = "Не підтримуємий тип файлу. Для завантаження допускаються файли лише наступних типів: " + extList;
                Modal.inform.warning()(message);
              } else {
                console.log("File " + sFileName + " validation successfully");
                aFilteredFiles.push(event.target.files[nFileIndex]);
              }
            }
            if(aFilteredFiles.length > 0){
              scope.upload(event.target.files, attrs.name);
            }
          }
        });
      });

      function verifyExtension (sFileNameForCheck){
        var ext = sFileNameForCheck.split('.').pop().toLowerCase();
        for (var i = 0; i < aAvailableFileExtensions.length; i++){
          if (ext === aAvailableFileExtensions[i]){
            return true;
          }
        }
        return false;
      };

      function convertAvailableExtensionArrayToString (){
        var resultString = null;
        for(var i = 0; i < aAvailableFileExtensions.length; i++){
          if (i === 0){
            resultString = aAvailableFileExtensions[i];
            if (aAvailableFileExtensions.length > 1){
              resultString = resultString + ", ";
            } else {
              resultString = resultString + ".";
            }
          } else if (i === aAvailableFileExtensions.length - 1){
            resultString = resultString + aAvailableFileExtensions[i] + ".";
          } else {
            resultString = resultString + aAvailableFileExtensions[i] + ", ";
          }
        }
        return resultString;
      };

      fileField.bind('click', function(e) {
        e.stopPropagation();
      });
      element.find('#upload-button').bind('click', function(e) {
        e.preventDefault();
        fileField[0].click();
      });



      scope.openScanModal = function (item) {
        $http.get(ScannerService.getTwainServerUrl()).success(function (data) {
          if(data){
            scanDocument();
          }
        }).error(function (err) {
          Modal.inform.error()('Сталася помилка при намаганні перевірити підключення до служби TWAIN@Web: ' + JSON.toString(err));
        });

      };

      function scanDocument() {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'components/scanner/scan-modal.html',
          controller: 'ScannerModalCtrl',
          resolve: {}
        });

        modalInstance.result.then(function (oScanResult) {

          $http.get(oScanResult.downloadUrl+'&asBase64=true').success(function (data) {
            uploadFile(data.base64, data.file, getImagesMymeType(data.file));
          })

        });
      }

      function getImagesMymeType(sFileName) {
        var ext = sFileName.split('.').pop().toLowerCase();
        if(ext === 'jpg'){
          return 'image/jpeg';
        } else if (ext === 'bmp'){
          return 'image/bmp';
        } else if (ext === 'tiff'){
          return 'image/tiff'
        } else if (ext === 'pdf'){
          return 'application/pdf'
        }
        return undefined;
      }

      function uploadFile(base64content, fileName, sMimeType) {
        var aFiles = [];
        var oScannedFile = generationService.getFileFromBase64(base64content, fileName, sMimeType);
        aFiles.push(oScannedFile);
        scope.upload(aFiles, attrs.name);
      }

      function IsJsonString(str) {
        try {
          JSON.parse(str);
        } catch (e) {
          return false;
        }
        return true;
      }

      if (scope.field && !scope.field.sKey && scope.sSelectedTask === 'docHistory') {
        if (IsJsonString(scope.field.value)){
          var result = JSON.parse(scope.field.value);
          scope.field.sKey = result.sKey;
          scope.field.storageType = result.sID_StorageType;
        }
      }
    },
    templateUrl: 'app/tasks/form-fields/file-field.html',
    replace: true,
    transclude: true
  };
});
