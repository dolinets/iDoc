(function(){
  'use strict';

  angular.module('app')
    .directive('decryptFile', decryptFile);

  function decryptFile(){
    return {
      restrict: 'EA',
      scope: {
        options: '=',
        oServiceData: '=',
        onFileUploadSuccess: '&'
      },
      templateUrl: 'app/common/components/decryptFile/decryptFile.directive.html',
      controller: DecryptFileController,
      controllerAs: 'vm',
      bindToController: true,
      transclude: true
    };
  }

  /* @ngInject */
  function DecryptFileController($scope, $q, $location, $window, ErrorsFactory, uiUploader, ActivitiService, MessagesService){
    var vm = this;
    var nID_Server = -1;

    vm.file = {};
    vm.onSelect = onSelect;
    vm.onDownload = onDownload;

    activate();

    function activate(){
      vm.file.isUploading = false;
    }

    function onSelect($files){
      upload($files, {nID_Server:nID_Server});
    }

    function onDownload(){
      MessagesService.getMessageFile(vm.options.id).then(function (res) {
        if(!res.err){
          var content = new Blob([res], {type: 'application/octet-stream'});
          var arrFiles = [];

          arrFiles.push(content);
          upload(arrFiles, {nID_Server:nID_Server}, vm.options.id);
        }else {
          ErrorsFactory.push({type:"danger", text: "Виникла помилка при отриманні файлу"});
        }
      });
    }

    function upload (files, oServiceData, id) {
      uiUploader.removeAll();
      uiUploader.addFiles(files);

      vm.file.fileName = files[0].name || vm.options.fileName;

      uiUploader.startUpload({
        url: ActivitiService.getUploadFileURL(oServiceData, null, {name :vm.file.fileName}),
        concurrency: 1,
        onProgress: function (file) {
          vm.file.isUploading = true;
          $scope.$apply();
        },
        onCompleted: function (file, fileId) {
          var fileObj;

          try{
            fileObj = JSON.parse(fileId);
          }catch(e){
            fileObj = {};
          }

          if(!fileObj.error){
            vm.file.value = {id : fileId, signInfo: null, fromDocuments: false};
          }else{
            vm.file.error = fileObj.error;
          }
          $scope.$apply();
        },
        onCompletedAll: function () {

          if(!vm.file.error){
            vm.onFileUploadSuccess(vm.file);
          }

          vm.file.isUploading = false;
          $scope.$apply();

          if(vm.file.error){
            ErrorsFactory.push({
              type: "denger",
              oData: {
                sHead: 'Помилка сервера.',
                sBody: 'Файл не завантажено.',
                sFunc: 'DecryptFileController'
              }
            });
          }

          if(!vm.file.error){
            decrypt({file: vm.file, id: id});
          }
        }
      });
    }

    function decrypt(params){
      var hostUrl = $location.protocol() + '://' +
        $location.host() + ':' +
        $location.port();
      var sID_Order = $location.search().sID_Order;
      var path = $location.path();
      var restoreUrl = hostUrl + path + (sID_Order ? '?sID_Order=' + sID_Order : '');
      var ID;

      if(params.file.value.id.indexOf('sKey') > -1) {
        ID = JSON.parse(params.file.value.id).sKey;
      } else {
        ID = params.file.value.id;
      }

      if(vm.options.openModalViewer){
        window.localStorage.setItem("openDfsAnswerModalViewer", angular.toJson(vm.options.openModalViewer));
      }

      $window.location.href = hostUrl + '/api/sign-content/decrypt?formID=' +
        ID + '&nID_Server=' +
        nID_Server + '&sName=' + params.file.fileName + '&nID=' + params.id + '&restoreUrl=' + restoreUrl;
    }
  }
})();
