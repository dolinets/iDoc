/**
 * Created by irish on 10.11.17.
 */

(function () {
    'use strict';

    angular
        .module('dashboardJsApp')
        .controller('staffContact', staffContact);

    staffContact.$inject = ['$scope', 'Staff', 'Modal', '$rootScope', '$http'];
    function staffContact($scope, Staff, Modal, $rootScope, $http) {
        $scope.contactTypeList = [];
        $scope.contantTableRow = [];

      Staff.getAllContactType().then(function (res) {
          $scope.contactTypeList = res;
      });

      $rootScope.showContact = function (contacts) {
        $scope.staffRoleContact = $rootScope.staffRole;

        if (contacts.length)
          angular.forEach(contacts, function(item) {
            $scope.contantTableRow.push({sType: item.subjectContactType.sName_UA, sValue: item.sValue});
          });
       }

        $scope.removeContactString = function (index) { // delete table string
          $scope.contactLoading = 'Видалення...';

          $scope.Employeespinner = true;
          var contactToRemove = [];
          contactToRemove.push( $scope.contantTableRow[index]);

          Staff.removeContact($scope.$parent.currHuman.login, contactToRemove).then(function (data) {
            Modal.inform.info()("Контакт видалено успішно");
            $scope.contantTableRow.splice(index, 1);
          }, function (err) {
            Modal.inform.error()(err.message);
          }).finally(function() {
            $scope.Employeespinner = false;
            $scope.contactLoading = false;
          });
        };

        $scope.addContactString = function () { // add table string
           $scope.contantTableRow.push({sType: '', sValue: '', bNew: true});
        };

      $scope.modalShownContact = false;
      $scope.saveStaffContact = function() {
        var contactsToSave = $scope.contantTableRow.filter(function(item) {
          return item.bNew;
        });

        var bValid = true;
        if (contactsToSave.length) {
          $scope.contactLoading = 'Збереження...';

          angular.forEach(contactsToSave, function(item) {
            if (!item.sType) {
              bValid = false;
              Modal.inform.warning()("Виберіть тип контакту");
              return;
            } else if (!item.sValue) {
               bValid = false;
               Modal.inform.warning()("Заповніть поле контакту");
               return;
            }

            delete item.bNew;

            if (item.sType === 'Телефон')
              item.sValue = '+380' + item.sValue;
          }); 

          if (bValid)
            Staff.setContact($scope.$parent.currHuman.login, contactsToSave).then(function(data){
              Modal.inform.info()("Контакти успішно додано");

              $scope.cancelStaffContact();
            }, function (err) {
              Modal.inform.error()(err.message);
            }).finally(function() {$scope.contactLoading = false;});
        } else {
          $scope.contactLoading = false;
          Modal.inform.info()('Немає нових контактів для збереження');
        }

      }

        $scope.cancelStaffContact = function() {
            $scope.modalShownContact = !$scope.modalShownContact;
            $scope.login = $scope.selectHuman;
            $scope.contantTableRow = [];
            $scope.show = false;

        };
      }
})();
