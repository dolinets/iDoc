(function () {
    'use strict';

    angular
      .module('dashboardJsApp')
      .controller('PositionCtrl', positionCtrl);

      positionCtrl.$inject = ['$scope', 'Modal', '$http', '$rootScope', 'Staff'];
        function positionCtrl($scope, Modal, $http, $rootScope, Staff) {

          $scope.bCreate = {
            value: false
          }

          $scope.getCheckStatus = function () {  // clear field if create new dep
            if($scope.bCreate.value === true){
              $scope.currNamePosit = $scope.currPosition.position;
              $scope.currPosition.position = '';
              $scope.temporaryParent = $scope.currPosition.parent;
              $scope.currPosition.parent = $scope.currNamePosit;
              $scope.temporaryStatus = $scope.currPosition.status;
              $scope.currPosition.status = '';
            }else if($scope.bCreate.value === false){
              $scope.currPosition.position = $scope.currNamePosit;
              $scope.currPosition.parent = $scope.temporaryParent;
              $scope.currPosition.status = $scope.temporaryStatus;
            }
           // console.log($scope.currPosition.position, $scope.bCreate.value)
           }


            $scope.saveStaffDepart = function () {
              var depart = {};
                 if($rootScope.newComp === true){
                   depart = {params: {sName: $scope.currPosition.position,
                     sID_Group_Activiti: $scope.currPosition.login,
                     bCreate: true}};
                 }else if($scope.bCreate.value === true){
                   depart = {params: {sName: $scope.currPosition.position,
                     //sID_Group_Activiti_Parent: $scope.currPosition.root_login,
                     sID_Group_Activiti_Parent: $scope.currPosition.login,
                     bCreate: $scope.bCreate.value}};
                 }else if($scope.bCreate.value === false && !$scope.currPosition.root_login){
                   depart = {params: {sName: $scope.currPosition.position,
                     sID_Group_Activiti: $scope.currPosition.login,
                     bCreate: $scope.bCreate.value}};
                 }else{
                   depart = {params: {sName: $scope.currPosition.position,
                     sID_Group_Activiti: $scope.currPosition.login,
                     sID_Group_Activiti_Parent: $scope.currPosition.root_login,
                     bCreate: $scope.bCreate.value}};
              }

              depart.params.sStatus = $scope.currPosition.status ? $scope.currPosition.status.sName : '';

              if (!depart.params.sName || depart.params.sName.trim().length === 0) {
                Modal.inform.warning()("Назва організіції чи підрозділу не може бути пустою");
                return;
              }

              if (!depart.params.sStatus) {
                Modal.inform.warning()("Статус організіції чи підрозділу не може бути пустим");
                return;
              }

              Staff.createNewEditDepart(depart).then(function (res) {
                  if(!res.code && !res.message) {
                    var childAdded = false; 

                    angular.forEach($scope.departList[0].aSubjectGroupChilds, function(item, i, obj) {
                      if($scope.currPosition.login === item.sID_Group_Activiti)
                        obj[i].oSubject.oSubjectStatus = $scope.currPosition.status;

                      if ($scope.currPosition.parent === item.sName) {
                        if (!obj[i].aSubjectGroupChilds)
                          obj[i].aSubjectGroupChilds = [];
                        obj[i].aSubjectGroupChilds.push(res);
                        childAdded = true;
                      }
                    });

                    if (!childAdded)
                      $scope.departList[0].aSubjectGroupChilds.push(res);

                    Modal.inform.info()("Операція виконана успішно");
                  }
                    //console.log(res);
                   // $scope.clearDepart();
                }, function (err) {
                   // console.log(login);
                    Modal.inform.error()(JSON.parse(err).message);


                });
            };
            // $scope.clearDepart = function(){
            //     $scope.currPosition = {
            //         "position": "",
            //         "parent": "",
            //         "login": ""
            //
            //     };
            //
            // };
        }

})();
