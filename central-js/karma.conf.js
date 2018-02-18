// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

/* global module */

'use strict';

module.exports = function(config) {
  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [

      'client/bower_components/jquery/dist/jquery.js',
'client/bower_components/angular/angular.js',
      'client/bower_components/angular-mocks/angular-mocks.js',
'client/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
'client/bower_components/angular-cookies/angular-cookies.js',
'client/bower_components/angular-sanitize/angular-sanitize.js',
'client/bower_components/angular-translate/angular-translate.js',
'client/bower_components/angular-dialog-service/dist/dialogs.min.js',
'client/bower_components/angular-dialog-service/dist/dialogs-default-translations.min.js',
'client/bower_components/angular-i18n/angular-locale_uk.js',
'client/bower_components/jsoneditor/dist/jsoneditor.min.js',
'client/bower_components/angular-jsoneditor/angular-jsoneditor.js',
'client/bower_components/angular-messages/angular-messages.js',
'client/bower_components/moment/moment.js',
'client/bower_components/moment/locale/uk.js',
'client/bower_components/moment/locale/ru.js',
'client/bower_components/angular-moment/angular-moment.js',
'client/bower_components/angular-resource/angular-resource.js',
'client/bower_components/angular-ui-event/dist/event.js',
'client/bower_components/angular-ui-router/release/angular-ui-router.js',
'client/bower_components/angular-ui-uploader/dist/uploader.js',
'client/bower_components/angular-ui-scroll/dist/ui-scroll.js',
'client/bower_components/ui-select/dist/select.js',
'client/bower_components/intl-tel-input/build/js/intlTelInput.min.js',
'client/bower_components/intl-tel-input/lib/libphonenumber/build/utils.js',
'client/bower_components/jquery.cookie/jquery.cookie.js',
'client/bower_components/lodash/dist/lodash.compat.js',
'client/bower_components/zeroclipboard/dist/ZeroClipboard.js',
'client/bower_components/ng-clip/src/ngClip.js',
'client/bower_components/rangy/rangy-core.js',
'client/bower_components/rangy/rangy-classapplier.js',
'client/bower_components/rangy/rangy-highlighter.js',
'client/bower_components/rangy/rangy-selectionsaverestore.js',
'client/bower_components/rangy/rangy-serializer.js',
'client/bower_components/rangy/rangy-textrange.js',
'client/bower_components/textAngular/dist/textAngular.js',
'client/bower_components/textAngular/dist/textAngular-sanitize.js',
'client/bower_components/textAngular/dist/textAngularSetup.js',
'client/bower_components/papaparse/papaparse.js',
'client/bower_components/eonasdan-bootstrap-datetimepicker/build/js/bootstrap-datetimepicker.min.js',
'client/bower_components/spectrum/spectrum.js',
'client/bower_components/angular-spectrum-colorpicker/dist/angular-spectrum-colorpicker.min.js',
'client/bower_components/bootstrap/dist/js/bootstrap.js',
'client/bower_components/bootstrap/js/dropdown.js',

'../public-js/markers/module.js',
'../public-js/cryptoPlugin/cryptoPlugin.js',
'../public-js/formData/autocompletesDataFactory.service.js',
'../public-js/formData/datepicker.module.js',
'../public-js/formData/datepickerFactory.service.js',
'../public-js/formData/dropdown.js',
'../public-js/formData/dropdownAutocomplete.directive.js',
'../public-js/formData/dropdownAutocompleteCtrl.controller.js',
'../public-js/formData/organlist.factory.js',
'../public-js/formData/table.service.js',
'../public-js/formData/typeahead.js',
'../public-js/markers/defaults.js',
'../public-js/markers/factory.js',
'../public-js/markers/field.attributes.service.js',
'../public-js/markers/field.motion.service.js',
'../public-js/markers/schema.js',
'../public-js/markers/validation.service.js',


      'client/app/app.js',
      'client/app/**/*.js'
    ],

    preprocessors: {
      '**/*.jade': 'ng-jade2js',
      '**/*.html': 'html2js',
      '**/*.coffee': 'coffee'
    },

    ngHtml2JsPreprocessor: {
      stripPrefix: 'client/'
    },

    ngJade2JsPreprocessor: {
      stripPrefix: 'client/'
    },

    // list of files / patterns to exclude
    exclude: [],

    // web server port
    port: 8080,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['PhantomJS'],

    // you can define custom flags
    // customLaunchers: {
    //   Chrome_without_security: {
    //     base: 'Chrome',
    //     flags: ['--disable-web-security']
    //   }
    // }

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
