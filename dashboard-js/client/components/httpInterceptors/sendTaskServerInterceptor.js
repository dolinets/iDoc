// angular.module('dashboardJsApp').factory('sendTaskServer', ['$q', '$rootScope', '$injector',
//   function ($q, $rootScope, $injector) {
//
//     return {
//
//       request: function(config) {
//
//         var CurrentServer = $injector.get('CurrentServer');
//         var apiPattern = /\/api\//;
//         var taskServer = CurrentServer.getServer('check');
//
//         config.params = config.params || {};
//
//         if (apiPattern.test(config.url) && taskServer && taskServer.name) {
//          config.params.taskServer = taskServer.name;
//         }
//
//         return config || $q.when(config);
//       }
//
//     };
//
//   }]);
