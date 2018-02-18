module.exports = function(config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine'],
    files: [
      //solve PhantomJs .bind problem
      './node_modules/phantomjs-polyfill/bind-polyfill.js',

      "client/bower_components/jquery/jquery.js",
      "client/bower_components/angular/angular.js",
      "client/bower_components/angular-mocks/angular-mocks.js",
      "client/bower_components/angular-base64/angular-base64.js",
      "client/bower_components/angular-md5/angular-md5.js",
      "client/bower_components/angular-resource/angular-resource.js",
      "client/bower_components/angular-cookies/angular-cookies.js",
      "client/bower_components/angular-sanitize/angular-sanitize.js",
      "client/bower_components/angular-route/angular-route.js",
      "client/bower_components/angular-messages/angular-messages.js",
      "client/bower_components/angular-bootstrap/ui-bootstrap-tpls.js",
      "client/bower_components/ng-idle/angular-idle.js",
      "client/bower_components/lodash/dist/lodash.compat.js",
      "client/bower_components/moment/min/moment.min.js",
      "client/bower_components/moment/locale/uk.js",
      "client/bower_components/angular-moment/angular-moment.js",
      "client/bower_components/ngstorage/ngStorage.js",
      "client/bower_components/angular-ui-utils/ui-utils.js",
      "client/bower_components/zeroclipboard/dist/ZeroClipboard.js",
      "client/bower_components/ng-clip/src/ngClip.js",
      "client/bower_components/angular-ui-router/release/angular-ui-router.js",
      "client/bower_components/angular-smart-table/dist/smart-table.js",
      "client/bower_components/angular-ui-validate/dist/validate.js",
      "client/bower_components/ui-select/dist/select.js",
      "client/bower_components/eonasdan-bootstrap-datetimepicker/build/js/bootstrap-datetimepicker.min.js",
      "client/bower_components/rangy/rangy-core.js",
      "client/bower_components/rangy/rangy-classapplier.js",
      "client/bower_components/rangy/rangy-highlighter.js",
      "client/bower_components/rangy/rangy-selectionsaverestore.js",
      "client/bower_components/rangy/rangy-serializer.js",
      "client/bower_components/rangy/rangy-textrange.js",
      "client/bower_components/textAngular/dist/textAngular.js",
      "client/bower_components/textAngular/dist/textAngular-sanitize.js",
      "client/bower_components/textAngular/dist/textAngularSetup.js",
      "client/bower_components/spectrum/spectrum.js",
      "client/bower_components/angular-spectrum-colorpicker/dist/angular-spectrum-colorpicker.min.js",
      "client/bower_components/snapjs/snap.js",
      "client/bower_components/angular-snap/angular-snap.js",
      "client/bower_components/intl-tel-input/build/js/intlTelInput.js",

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
      'client/app/**/*.js',
      'client/app/**/*.coffee',
      'client/components/**/*.js',
      'client/components/**/*.coffee',
      'client/app/**/*.jade',
      'client/components/**/*.jade',
      'client/app/**/*.html',
      'client/components/**/*.html'
    ],

    preprocessors: {
      '**/*.jade': 'ng-jade2js',
      '**/*.html': 'html2js'
    },

    ngHtml2JsPreprocessor: {
      stripPrefix: 'client/'
    },

    ngJade2JsPreprocessor: {
      stripPrefix: 'client/'
    },
    exclude: [],
    port: 8080,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['PhantomJS'],
    singleRun: false
  });
};
