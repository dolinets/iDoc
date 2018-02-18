(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('EmployeeCtrl', employeeCtrl);

  employeeCtrl.$inject = ['$scope', '$http', 'tasks', 'Auth', '$rootScope', '$timeout', 'DocumentsService', 'CurrentServer', 
  'Modal', 'Staff', '$cookies'];
  function employeeCtrl($scope, $http, tasks, Auth, $rootScope, $timeout, DocumentsService, CurrentServer, 
    Modal, Staff, $cookies) {
    /*$scope.fieldReferentAlreadyExist = function () {

      if ($scope.dataReferent){
        $scope.dataReferent.selected = $scope.$parent.dataReferent.selected;
      }else{
        $scope.dataReferent = {
          selected : null
        };
      }
      console.log($scope.dataReferent.selected);
    }

*/
    $scope.checkBoss = {
      value: false
    };
    $scope.referList = [{
      "id": ++i,
      "box": ""
    }];

    var login = Auth.getCurrentUser();
    var sCurrReferent = $cookies.getObject('referent');
    sCurrReferent = sCurrReferent ? sCurrReferent.id : login.id;

    /*$scope.removeReferentString = function (index) {*/ // delete table string
      /*$scope.currReferent = $scope.referList[$index];
      $scope.currReferent.index = $index;
      console.log($scope.currReferent,  $scope.currReferent.index, $index, $scope.referList);

      for (var j=0; $scope.referList.length >j; j++){
        if(j === $index){
          console.log($index, j)
          $scope.referList.splice($index, 1);
        }
      }
     // $scope.referList.splice($scope.currReferent.index, 1);
      if($scope.dataReferent[$index].selected == undefined) {
        Modal.inform.info()("Додайте референта");
      }else if($scope.dataReferent[$index].selected != undefined){
      var paramsRef = {params: {
        sID_Group_Activiti: $scope.currHuman.sID_Group_Activiti,
        sLogin: $scope.dataReferent[$index].selected.sID_Group_Activiti
      }};
*/
      /*var referentToRemove = $scope.referentList[index];

      if (referentToRemove.bNew) {
        $scope.referentList.splice(index, 1);
        return;
      }

      var paramsRef = {params: {
        sID_Group_Activiti: referentToRemove.sId,
        sLogin: $scope.currHuman.sID_Group_Activiti
      }};

      Staff.removeReferentStaff(paramsRef).then(function (res) {
        if (res.code === "BUSINESS_ERR") {
          Modal.inform.error()(res.message);
        } else {
          Modal.inform.info()("Референта видалено успішно");
        }
      }, function (err) {
        Modal.inform.error()(JSON.parse(err).message);
       }).finally(function() {$scope.referentList.splice(index, 1);});
      
    };*/

 /*   $scope.addReferentString = function () { // add table string
      $scope.modalShownReferents = true;
      //$scope.referentList.push({bNew: true});
    };*/

    /*$scope.onSelectUserReferent = function (referent, showModal) { // add Referent
      if($rootScope.staffRole === true){
        var paramsRef = {params: { sID_Group: referent.sId, sLogin: $scope.currHuman.sID_Group_Activiti}};
        Staff.addReferentStaff(paramsRef).then(function (res) {

          if (res.code === "SYSTEM_ERR") {
            Modal.inform.error()(res.message);
          } else {
            delete referent.bNew;
            if (showModal)
              Modal.inform.info()("Референти додано успішно");
          }
        }, function (err) {
          Modal.inform.error()(JSON.parse(err).message);
        });
      }
    };*/


    /*$scope.foundReferent = function (sFind) {
      $scope.selectFilterHuman.params.sFind = sFind;
      Staff.findForFilter($scope.selectFilterHuman).then(function (res) {

        var response = [];
        if (res && res.length) {
          response = res;
          angular.forEach(response, function (user) {
            user.sName = user.sName;
            user.sPosition = user.oSubjectHumanPositionCustom.sNote;
            user.sId = user.sID_Group_Activiti;
            user.bNew = true;
          })
        }
        $scope.referents = response;

      });

    }*/

    $scope.showChangePass = function() {
      return sCurrReferent === login.id;
    };


    $scope.getPositionList = function (sFind) {
      $scope.selectPosit = {params:{}};
      $scope.selectPosit.params.sFind = sFind;
      $scope.selectPosit.params.sLogin = login.id;
      $scope.selectPosit.params.sLoginStaff = $scope.currHuman.login
      $scope.selectPosit.params.sLoginReferent = sCurrReferent;

      $http.get('./api/staff', $scope.selectPosit).then(function (res) {
        if (typeof res.data === 'object') {
          var response = res.data;
          angular.forEach(response, function (user) {
            user.sNote = user.sNote;
          })
        }
        $scope.curPositionList = response;
      });
    }


    $scope.foundCompanyCurHuman = function (sFind) {// filter for main search to CompanyCurHuman

      $scope.filterCompany.params.sFind = sFind;
      Staff.findForFilter($scope.filterCompany).then(function (res) {

          var response = res;
          angular.forEach(response, function (user) {
            user.sName = user.sName;
            user.sNote = user.sNote;
            user.sCompany = user.sCompany;
            user.sID_Group_Activiti_Organ = user.sID_Group_Activiti;

          })

        $scope.subdivisionList = response;
      });
    }

    $scope.saveEmployee = function () {
      $scope.employeeForm.$setSubmitted();

      if($scope.employeeForm.$valid && $scope.employeeForm.$dirty && !$scope.employeeForm.$pristine) {
        $scope.disableAllInputs(true);

        if($scope.subposition.selected != undefined && $scope.subposition != undefined){

        /*if ($scope.referentList) {
          var referentsToSave = $scope.referentList.filter(function(item) {
            return item.bNew;
          });

          angular.forEach(referentsToSave, function(item, i) {
              if (i === referentsToSave.length-1)
                $scope.onSelectUserReferent(item, true);
              else
                $scope.onSelectUserReferent(item);
          });  
        }*/


        var employee = {
          params: {
            sFamily: $scope.currHuman.lastname,
            sName: $scope.currHuman.firstname,
            sSurname: $scope.currHuman.secondname,
            sEmail: $scope.currHuman.email,
            sPhone: '+380' + $scope.currHuman.phone,
            sStatus: $scope.currHuman.status.sName,
            sLoginStaff: $scope.currHuman.login,
            sLogin: login.id,
            sLoginReferent: sCurrReferent,
            sPosition: $scope.subposition.selected.sName,
            sID_Group_Activiti_Organ: $scope.subdivision.selected.sID_Group_Activiti_Organ,
            isHead: $scope.checkBoss.value
          }
        };

        if ($scope.currHuman.pass === $scope.currHuman.pass2 && $rootScope.newHuman === true) {
          employee.params.sPassword = $scope.currHuman.pass;
          //employee.params.sID_Group_Activiti = $scope.currHuman.login;
          // console.log(employee);
          Staff.createSubjectHuman(employee).then(function (res) {
            //      console.log(res);
            if (res.code === "BUSINESS_ERR") {
              Modal.inform.error()(res.message);
            } else {
              Modal.inform.info()("Співробітника додано успішно");
              $scope.humanList.push(res);
            }
          }, function (err) {
            Modal.inform.error()(JSON.parse(err).message);
          }).finally(function() {$scope.disableAllInputs(false);});

          //$scope.subposition.selected = null;
          //$scope.subdivision.selected = null;
          //$scope.currHuman = {};
          //$scope.checkBoss.value = false;
        } else if ($scope.currHuman.pass !== $scope.currHuman.pass2 && $rootScope.newHuman === true) {
          $scope.disableAllInputs(false);
          Modal.inform.info()("Паролі не збігаються");
        } else {
          //employee.params.sPhone = $scope.currHuman.phone;
          Staff.editHuman( employee).then(function (res) {
            if (res.code === "BUSINESS_ERR") {
              Modal.inform.error()(res.message);
            } else {
              Modal.inform.info()("Данні співробітника відредаговано успішно");

              angular.forEach($scope.humanList, function(user) {
                if (user.sID_Group_Activiti === $scope.currHuman.sID_Group_Activiti)
                  user.sName = $scope.currHuman.lastname+' '+$scope.currHuman.firstname+' '+$scope.currHuman.secondname;
              });
            }
          }, function (err) {
            Modal.inform.error()(JSON.parse(err).message);
          }).finally(function() {$scope.disableAllInputs(false);});

          /*$scope.subposition.selected = null;
          $scope.subdivision.selected = null;*/
          //$scope.currHuman = {};
          //$scope.checkBoss.value = false;
         }
        }else{
          $scope.disableAllInputs(false);
          Modal.inform.info()('Заповніть поля: підрозділ та посада');
        }
      } else if (!$scope.employeeForm.$valid) {
        if ($scope.employeeForm.pass.$invalid)
          Modal.inform.warning()('Пароль повинен складатися мінімум з 8 символів та містити хоча б 1 цифру та 1 літеру'); 
        else
          Modal.inform.warning()('Поля заповнені неправильно');
      } else
        Modal.inform.info()('Немає змін для збереження');
    };


    $scope.modalShown = false;
    $scope.toggleModalStaff = function() {
      $scope.modalShown = !$scope.modalShown;
      $scope.login = $scope.selectHuman
    };


    $scope.modalShownContact = false;
    $scope.toggleModalStaffContact = function() {
      $scope.modalShownContact = !$scope.modalShownContact;
      if($rootScope.newHuman == false) {
        var contact = {params: {sID_Group_Activiti: $scope.currHuman.login}};
        Staff.getContact(contact).then(function (res) {
          if (res.code === "BUSINESS_ERR") {
            Modal.inform.error()(res.message);
          } else {
            var response = res;
            $scope.contanctList = response;

          }
          $rootScope.showContact($scope.contanctList);
        //  console.log($scope.contanctList);
        }, function (err) {
          Modal.inform.error()(JSON.parse(err).message);
        });

      }
    };


    $scope.modalShownPosition = false;
    $scope.toggleModalStaffPosition = function() {
      $scope.modalShownPosition = !$scope.modalShownPosition;
    };

    /*$scope.modalShownReferents = false;
    $scope.toggleModalStaffReferents = function() {
      $scope.modalShownReferents = !$scope.modalShownReferents;
    };*/
  }
})();
