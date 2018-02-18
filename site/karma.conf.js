module.exports = function(config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine'],
    files: [
      //solve PhantomJs .bind problem
      './node_modules/phantomjs-polyfill/bind-polyfill.js',

      "/jquery/jquery.js",
      "client/app/bower_components/angular/angular.js",
      "client/app/bower_components/angular-mocks/angular-mocks.js",
      "client/app/bower_components/angular-base64/angular-base64.js",
      "client/app/bower_components/angular-md5/angular-md5.js",
      "client/app/bower_components/angular-resource/angular-resource.js",
      "client/app/bower_components/angular-cookies/angular-cookies.js",
      "client/app/bower_components/angular-sanitize/angular-sanitize.js",
      "client/app/bower_components/angular-route/angular-route.js",
      "client/app/bower_components/angular-messages/angular-messages.js",
      "client/app/bower_components/angular-bootstrap/ui-bootstrap-tpls.js",
      "client/app/bower_components/ng-idle/angular-idle.js",
      "client/app/bower_components/lodash/dist/lodash.compat.js",
      "client/app/bower_components/moment/min/moment.min.js",
      "client/app/bower_components/moment/locale/uk.js",
      "client/app/bower_components/angular-moment/angular-moment.js",
      "client/app/bower_components/ngstorage/ngStorage.js",
      "client/app/bower_components/angular-ui-utils/ui-utils.js",
      "client/app/bower_components/zeroclipboard/dist/ZeroClipboard.js",
      "client/app/bower_components/ng-clip/src/ngClip.js",
      "client/app/bower_components/angular-ui-router/release/angular-ui-router.js",
      "client/app/bower_components/angular-smart-table/dist/smart-table.js",
      "client/app/bower_components/angular-ui-validate/dist/validate.js",
      "client/app/bower_components/ui-select/dist/select.js",
      "client/app/bower_components/eonasdan-bootstrap-datetimepicker/build/js/bootstrap-datetimepicker.min.js",
      "client/app/bower_components/rangy/rangy-core.js",
      "client/app/bower_components/rangy/rangy-classapplier.js",
      "client/app/bower_components/rangy/rangy-highlighter.js",
      "client/app/bower_components/rangy/rangy-selectionsaverestore.js",
      "client/app/bower_components/rangy/rangy-serializer.js",
      "client/app/bower_components/rangy/rangy-textrange.js",
      "client/app/bower_components/textAngular/dist/textAngular.js",
      "client/app/bower_components/textAngular/dist/textAngular-sanitize.js",
      "client/app/bower_components/textAngular/dist/textAngularSetup.js",
      "client/app/bower_components/spectrum/spectrum.js",
      "client/app/bower_components/angular-spectrum-colorpicker/dist/angular-spectrum-colorpicker.min.js",
      "client/app/bower_components/snapjs/snap.js",
      "client/app/bower_components/angular-snap/angular-snap.js",
      "client/app/bower_components/intl-tel-input/build/js/intlTelInput.js",

/*       '../public-js/markers/module.js',
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
      '../public-js/markers/validation.service.js', */



      'client/app.js',
      'client/app/**/*.js',
      'client/app/**/*.ejs'
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
