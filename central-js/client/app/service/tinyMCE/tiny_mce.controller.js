'use strict';

angular.module('app')
    .controller('TinyMceController', function($scope) {

        $scope.tinymceOptions = {
            plugins: ['link image code', 'textcolor', 'lists', 'table', 'paste'],
            min_height: 300,
            language: 'uk_UA',
            language_url : tinyMCE.langURL,
            menubar: "",
            toolbar: 'undo redo | bold italic underline superscript subscript | fontsizeselect | formatselect | fontselect | alignleft aligncenter alignright | link image | paste | forecolor backcolor | numlist bullist | table | removeformat',
            fontsize_formats: '8px 10px 12px 14px 18px 24px 36px',
            paste_data_images: true
        };

    });
