'use strict';
angular.module('dashboardJsApp', [
    'base64',
    'angular-md5',
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ui.router',
    'ngRoute',
    'ngIdle',
    'ngStorage',
    'ui.bootstrap',
    'ui.uploader',
    'ui.event',
    'angularMoment',
    'ngClipboard',
    'iGovMarkers',
    'ngMessages',
    'smart-table',
    'ui.validate',
    'ui.select',
    'iGovTable',
    'datepickerService',
    'autocompleteService',
    'datetimepicker',
    'ea.treeview',
    'cryptoPlugin',
    'textAngular',
    'angularSpectrumColorpicker',
    'snap',
    'ui.tinymce',
    'ui.tree',
    'signModule',
    'ui.utils',
    'pdfjsViewer'
]).config(function($urlRouterProvider, $locationProvider, $compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);
    $urlRouterProvider
        .otherwise('/login');
    $locationProvider.html5Mode(true);
}).run(function(amMoment, $rootScope, Modal) {
    amMoment.changeLocale('uk');
    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
        var message;
        if (error.status == 403)
            message = error.data.message;
        Modal.inform.error()(message || 'Виникла помилка. Зверніться будь ласка у технічну підтримку.');
        console.warn('Виникла помилка. Інформація для технічної підтримки: ', arguments);
    })
}).config([
    'datetimepickerProvider',
    function(datetimepickerProvider) {
        datetimepickerProvider.setOptions({
            locale: 'uk',
            toolbarPlacement: 'bottom',
            showClear: true,
            format: 'DD/MM/YYYY',
            tooltips: {
                clear: 'Очистити',
                selectMonth: 'Обрати мiсяць',
                prevMonth: 'Попереднiй мiсяць',
                nextMonth: 'Наступний мiсяць',
                selectYear: 'Обрати рiк',
                prevYear: 'Попереднiй рiк',
                nextYear: 'Наступний рiк',
                selectDecade: 'Обрати десятиліття',
                prevDecade: 'Попереднє десятиліття',
                nextDecade: 'Наступне десятиліття',
                prevCentury: 'Попереднє століття',
                nextCentury: 'Наступне століття'
            }
        });
    }
]).config(['snapRemoteProvider',
    function(snapRemoteProvider) {
        if (window.innerWidth >= 992) {
            snapRemoteProvider.globalOptions = { touchToDrag: false, tapToClose: false }
        } else {
            snapRemoteProvider.globalOptions = { touchToDrag: true, tapToClose: true }
        }

    }
]).config(function() {
    tinyMCE.baseURL = '/bower_components/tinymce';
    tinyMCE.langURL = '/bower_components/tiny-mce-UA/uk_UA.js';
});
