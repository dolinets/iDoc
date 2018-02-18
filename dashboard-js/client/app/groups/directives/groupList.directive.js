/**
 * Created by ijmac on 19.05.16.
 */
angular.module('dashboardJsApp')
  .directive('groupList', function () {
    var controller = function ($scope, $modal, $q, Modal, $http) {

      var inProgress = false;

      var groups = [];
      var users = [];
      var userInGroup = [];
      var groupsList = [];

      var getFunc = $scope.funcs.getFunc;
      var getUsFunc = $scope.funcs.getUsFunc;
      var setFunc = $scope.funcs.setFunc;
      var deleteFunc = $scope.funcs.deleteFunc;
      var addUsFunc = $scope.funcs.addUsFunc;
      var removeUsFunc = $scope.funcs.removeUsFunc;

      var editModes = {
        CREATE: 1,
        EDIT: 2
      };

      $scope.model = {inProgress: false};

      var fillData = function () {
        inProgress = true;
        $scope.model.inProgress = true;

        $q.all([
            getFunc()
              .then(function (list) {
                groups = list;
              }),
            getUsFunc()
              .then(function(list){
                users = list;
              })
        ])
          .then(function(){
            inProgress = false;
            $scope.model.inProgress = false;
          });
      };

      var userAddRemoveFunc = function (data, group) {
        if(data.usersToAdd.length){
          for(var i = 0; i<data.usersToAdd.length; i++){
            addUsFunc(group.id, data.usersToAdd[i].sLogin).then(
              function(addedUser){
                console.log('User ' + addedUser.sLogin + ' was added to ' + addedUser.sID_Group + ' group.')
              }, function (err) {console.log('Add User To group Error');}
            );
          }
        }

        if(data.usersToRemove.length){
          for(var u = 0; u<data.usersToRemove.length; u++){
            removeUsFunc(group.id, data.usersToRemove[u].sLogin).then(
              function(removedUser){
                console.log('User ' + removedUser.sLogin + ' was removed from ' + removedUser.sID_Group + ' group.')
              }, function (err) {console.log('Remove User From group Error');}
            );
          }
        }
      };

      var openModal = function (group, userInGroup, editMode) {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'app/groups/modal/modal.html',
          controller: 'GroupModalController',
          resolve: {
            groupToEdit: function () {
              return angular.copy(group);
            },
            getUsersFunc: function () {
              return angular.copy(getUsFunc);
            },
            userInGroup: function(){
              return angular.copy(userInGroup);
            },
            allGroups: function(){
              return angular.copy(groups);
            },
            allUsers: function(){
              return angular.copy(users);
            },
            editModes: function(){
              return angular.copy(editModes);
            },
            editMode: function(){
              return angular.copy(editMode);
            }
          },
          size: 'lg'
        });

        modalInstance.result.then(function (editedData) {

          if (editedData && editedData.groupToSave && editedData.groupToSave.revision) {
            userAddRemoveFunc(editedData, editedData.groupToSave)
          } else {
            setFunc(editedData.groupToSave.id, editedData.groupToSave.name)
              .then(function (createdGroup) {
                userAddRemoveFunc(editedData, createdGroup);

                for (var i = 0; i < groups.length; i++) {
                  if (groups[i].id === createdGroup.id) {
                    groups[i] = createdGroup;
                    return;
                  }
                }
                groups.unshift(createdGroup);
              }, function (err) {console.log('Edit Group Error');});
          }
        });
      };

      //Script Oleg for issue #1970
      function identifyHeight() {
          $(window).click(function () {
              var windowHeight = window.innerHeight, headerHeight = document.getElementById('dropdown-user-menu').clientHeight;
              var height = windowHeight - headerHeight - 250;
              $('.table-fixed tbody').attr('height', (height + 'px'));
          });
      }

      $scope.add = function () {
        openModal(null, null, editModes.CREATE);
      };

      $scope.edit = function (group) {

        getUsFunc(group.id).then(function (data) {
          userInGroup = data;
        }).finally(function () {
          openModal(group, userInGroup, editModes.EDIT);
        });
      };

      $scope.delete = function (group) {
        deleteFunc(group.id).then(function(data){
          if(data.code === '500'){
            //Modal.inform.error()(data.message);
          }else{
            fillData();
          }
        });
      };

      $scope.init = function () {
        fillData();
      };

      $scope.groupsSearch = function (sFind) {
        $http({
          method: 'GET',
          url: '/api/users/groups/getGroups',
          headers: {'Content-Type': 'application/json;charset=utf-8'},
          params: sFind ? {sFind: sFind} : {}
        }).then(function (response) {
          groupsList = response.data;
        });
      };

      $scope.get = function () {
        identifyHeight(); //For issue #1970
        return groupsList;
      };
    };

    return {
      restrict: 'EA',
      scope: {
        funcs: '='
      },
      controller: controller,
      templateUrl: 'app/groups/directives/groupList.html'
    }
  });
