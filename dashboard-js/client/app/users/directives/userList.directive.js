/**
 * Created by ijmac on 23.05.16.
 */
angular.module('dashboardJsApp')
  .directive('userList', function () {
    var controller = function ($scope, $modal, $q, Profile, Modal, $http) {
      var inProgress = false;
      var users = [];
      var groupsToUser = [];
      var groups = [];
      var getFunc = $scope.funcs.getFunc;
      var getGrFunc = $scope.funcs.getGrFunc;
      var setFunc = $scope.funcs.setFunc;
      var deleteFunc = $scope.funcs.deleteFunc;
      var addToGroupFunc = $scope.funcs.addFunc;
      var removeFromGroup = $scope.funcs.removeFunc;
      var usersList = [];
      $scope.isEdit = false;

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
              .then(function (data) {
                users = data;
              }),
            getGrFunc().then(function (data) {
              groups = data;
            })
          ])
          .then(function () {
            inProgress = false;
            $scope.model.inProgress = false;
          });

      };

      var openModal = function (user, groups, allGroups, editMode) {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'app/users/modal/modal.html',
          controller: 'UserModalController',
          resolve: {
            userToEdit: function () {
              return angular.copy(user);
            },
            userGroups: function () {
              return angular.copy(groups);
            },
            allGroups: function () {
              return angular.copy(allGroups);
            },
            allUsers: function () {
              return angular.copy(users);
            },
            editModes: function () {
              return angular.copy(editModes);
            },
            editMode: function () {
              return angular.copy(editMode);
            }
          },
          size: 'lg'
        });

        function operationWithUser(data, currentUser) {
          var userToAdd = {
            sLogin: currentUser.sLogin,
            sPassword: data.userToSave.sPassword || (data.userToSave.changePassword ? data.userToSave.password : user.sPassword),
            sName: currentUser.sName,
            sDescription: currentUser.sDescription,
            sEmail: currentUser.sEmail,
            FirstName: currentUser.sName,
            LastName: currentUser.sDescription,
            Email: currentUser.sEmail
          };


          addUserToGroup(data, userToAdd);

          removeUserFromGropup(data, userToAdd);

          changePassword(data, user);

          updateUserList(userToAdd);
        }

        modalInstance.result.then(function (editedData) {
          if (!editedData.userToSave.isNew) {
            operationWithUser(editedData, editedData.userToSave)
          } else {
            setFunc(editedData.userToSave.sLogin
              , editedData.userToSave.sPassword || user.sPassword
              , editedData.userToSave.sName
              , editedData.userToSave.sDescription
              , editedData.userToSave.sEmail).then(function (createdUser) {

              operationWithUser(editedData, createdUser)

            }, function (err) {
              //Modal.inform.error()(JSON.parse(err).message);
            });
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

      function updateUserList(user) {
        for (var i = 0; i < users.length; i++) {
          if (users[i].sLogin === user.sLogin) {
            users[i] = user;
            return;
          }
        }
        user ? users.unshift(user) : null;
      }

      function removeUserFromGropup(data, user) {
        if (data.groupsToRemove.length) {
          for (var i = 0; i < data.groupsToRemove.length; i++) {
            removeFromGroup(data.groupsToRemove[i].id, user.sLogin).then(
              function (removedUser) {
                //Modal.inform.info()();
              }, function (err) {
                //Modal.inform.error()(JSON.parse(err).message);
              }
            );
          }
        }
      }

      function addUserToGroup(data, user) {
        if (data.groupsToAdd.length) {
          for (var i = 0; i < data.groupsToAdd.length; i++) {
            addToGroupFunc(data.groupsToAdd[i].id, user.sLogin).then(
              function (addedUser) {
                //Modal.inform.info()(addedUser.sID_Group);
              }, function (err) {
                //Modal.inform.error()(JSON.parse(err).message);
              }
            );
          }
        }
      }

      function changePassword(data, currUser){
        if (data.userToSave.changePassword) {
          Profile.changePassword(
            data.userToSave.sLogin,
            data.userToSave.oldPassword,
            data.userToSave.password)
            .then(function (outData) {
              Modal.inform.info()("Пароль змінено");
            }, function (err) {
              //Modal.inform.error()(JSON.parse(err).message);
            });
        }
      }

      $scope.add = function () {
        openModal(null, null, groups, editModes.CREATE);
      };

      $scope.edit = function (user) {
        user.sName = user.sName || user.FirstName;
        user.sDescription = user.sDescription || user.LastName;

        getGrFunc(user.sLogin).then(function (data) {
          groupsToUser = data;
        }).finally(function () {
          $scope.isEdit = true;
          openModal(user, groupsToUser, groups, editModes.EDIT);
        });

      };

      $scope.delete = function (user) {
        deleteFunc(user.sLogin).then(fillData);
      };

      $scope.init = function () {
        fillData();
      };

      $scope.usersSearch = function (sFind) {
        $http({
          method: 'GET',
          url: '/api/users/getUsers',
          headers: {'Content-Type': 'application/json;charset=utf-8'},
          params: sFind ? {sFind: sFind} : {}
        }).then(function (response) {
          usersList = response.data;
        });
      };

      $scope.get = function () {
        identifyHeight(); //For issue #1970
        return usersList;
      };
    };
    return {
      restrict: 'EA',
      scope: {
        funcs: '='
      },
      controller: controller,
      templateUrl: 'app/users/directives/userList.html'
    }
  });
