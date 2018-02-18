'use strict';

angular.module('dashboardJsApp')
    .controller('TinyMceController', function($scope, CurrentServer) {
        $scope.server = CurrentServer.currentServerId();

        $scope.tinymceOptions = {
            file_browser_callback :function(field_name, url, type, win){
                var input = document.createElement('input');
                input.setAttribute('type', 'file');
                input.onchange = function() {
                    var file = this.files[0];

                    var reader = new FileReader();
                    reader.onload = function () {
                      var fileName = input.files[0].name;

                      $scope.upload(input.files, 'sFileFromHTML', field_name, win);
                        $scope.$watch('headersTiny', function(){
                            var el = win.document.getElementById(field_name);
                            if (el) {
                              if ($scope.headersTiny)
                                el.value = '/api/tasks/download/' + $scope.headersTiny.sKey + '/attachment/' + $scope.headersTiny.sID_StorageType + '/' + $scope.headersTiny.sFileNameAndExt + '/server/' + $scope.server;
                              else
                                el.value = 'Завантаження...';
                            }
                        });
                    };
                    reader.readAsDataURL(file);
                  };

                  input.click();

            },
            plugins: ['link image code', 'textcolor', 'lists', 'table', 'paste'],
            min_height: 300,
            language: 'uk_UA',
            language_url : tinyMCE.langURL,
            menubar: "",
            statusbar: false,
            toolbar: 'undo redo | bold italic underline superscript subscript | fontsizeselect | formatselect | fontselect | alignleft aligncenter alignright | link unlink image | paste | forecolor backcolor | numlist bullist | table | removeformat',
            fontsize_formats: '8px 10px 12px 14px 18px 24px 36px',
            default_link_target: "_blank",
            link_assume_external_targets: true
            // paste_data_images: true
        };
    });
