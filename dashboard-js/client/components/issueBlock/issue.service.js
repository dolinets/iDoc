angular.module('dashboardJsApp').service('Issue', ['tasks', '$q', 'ExecAndCtrlService', '$filter', function (tasks, $q, ExecAndCtrlService, $filter) {
  this.issues = [];

  this.clearIssues = function () {
    this.issues = [];
  };

  this.getIssues = function () {
    return this.issues;
  };

  this.checkIsSavedIssueEdit = function (createdIssues, savedIssues, canCreate) {
    var isChanged = false;
    if (canCreate && createdIssues && savedIssues)
      angular.forEach(savedIssues, function (sIssue) {
        angular.forEach(createdIssues, function (cIssue) {
          if (sIssue.action === 'edit' && sIssue.rootID === cIssue.snID_Process_Activiti_Root)
            isChanged = true;
        });
      });

    return isChanged;
  };

  this.isIssue = function (fields, tempTable) {
    var deferred = $q.defer();

    function searchSelectParams(param) {
      for(var i=0; i<fields.length; i++) {
        if(fields[i].id === param) {
          return fields[i].value;
        }
      }
    }

    function searching(temp) {

      if(!temp) {
        for( var i=0; i<fields.length; i++ ) {
          if(fields[i].type === 'table' && fields[i].id.indexOf('oProcessSubject_Executor') === 0 && fields[i].aRow) {
            search(fields[i].aRow);
          }
        }
      } else {
        search(temp.aRow);
      }

      function search(tableRows) {
        var params = {selectExecutors: {}};
        angular.forEach(tableRows, function (e) {
          angular.forEach(e.aField, function (field) {
            if(field.type === 'select' && field.name.indexOf('sID_BP') > -1) {
              var split = field.name.split(';'), options = split[2].split(',');
              params.name  = split[0];

              for(var j=0; j<options.length; j++) {
                var val = options[j].split('=');
                if(val[0].indexOf('sID_BP') === 0) {
                  params.bp = val[1];
                } else if(val[0].indexOf('sID_Group_Activiti') === 0) {
                  params.selectExecutors.activiti = searchSelectParams(val[1]);
                } else if(val[0].indexOf('nDeepLevel') === 0) {
                  params.selectExecutors.deep = searchSelectParams(val[1]);
                }
              }

              for( var o=0; o<fields.length; o++ ) {
                if (fields[o].type === 'select' && fields[o].id.indexOf('oProcessSubject_Controller') === 0) {
                  var splitted = fields[o].name.split(';'), ctrlOptions = splitted[2].split(',');
                  params.controllerSelect = {};

                  for (var p = 0; p < ctrlOptions.length; p++) {
                    var item = ctrlOptions[p].split('=');
                    if (item[0].indexOf('sID_Group_Activiti') === 0) {
                      params.controllerSelect.activiti = searchSelectParams(item[1]);
                    } else if (item[0].indexOf('nDeepLevel') === 0) {
                      params.controllerSelect.deep = searchSelectParams(item[1]);
                    }
                  }
                  deferred.resolve(params);
                }
              }
            }
          })
        })
      }
    }

    if (tempTable) {
      if(tempTable.value.indexOf('sKey') > -1) {
        var parsed = JSON.parse(tempTable.value);
        tasks.getTableOrFileAttachment(parsed.sKey, parsed.sID_StorageType, true).then(function (temp) {
          searching(temp);
        });
      } else {
        searching();
      }
    } else {
      searching();
    }

    return deferred.promise;
  };

  //Script Oleg for issue #2008
  var testFIO = function () {
    window.onclick = function () {
      var errorText = 'Користувач не може бути одночасно і контролюючим, і виконавцем!';
      if ($("span[name='taskExecutor0']").text().trim() !== '' && $("span[name='taskController0']").text().trim() !== '') {
        ($("span[name='taskController0']").text().trim() === $("span[name='taskExecutor0']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor1']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor2']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor3']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor4']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor5']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor6']").text().trim() || $("span[name='taskController0']").text().trim() === $("span[name='taskExecutor7']").text().trim()) ? ($('span#textError').css({ 'padding': '5px', 'color': '#a94442' }), document.getElementById('textError').innerHTML = (errorText === '' ? '' : 'Користувач не може бути одночасно і контролюючим, і виконавцем!'), $('#signId').attr('disabled', true), $('#createId').attr('disabled', true), $('#createTaskId').attr('disabled', true)) : ($('span#textError').css({ 'padding': '5px', 'color': 'blue' }), document.getElementById('textError').innerHTML = '', $('#signId').attr('disabled', false), $('#createId').attr('disabled', false), $('#createTaskId').attr('disabled', false));
      }
    };
  }

  var isEnyLocalIssue = function() { //huck for geting count of issues
    return document.getElementsByTagName('issue-for-read').length || 0;
  };

  //Script Oleg for issue #1949
  //$(window).click(function () {
  //    var array = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
  //    for (var i = 0; i < array.length; ++i) {
  //        var text = $("input[name='taskDay" + array[i] + "']").val();
  //        $("input[name='taskDate" + array[i] + "']").val() === undefined ? (text.length === 10 ? $("input[name='taskDay" + array[i] + "']").val('') : 1) : 1;
  //    }
  //});

  //Script Oleg definition of current date
  Date.prototype.yyyymmdd = function () {
    var dd = this.getDate();
    return [dd].join('');
  };

  this.addIssue = function (creator) {
    testFIO();
    if (creator && this.issues.length > 0)
      this.clearIssues();
    var isValid = this.validate();

    if(isValid) {
      if(this.issues.length > 0) {
        var copy = angular.copy(this.issues[0]);
        for(var field in copy) {
          if(copy.hasOwnProperty(field)) {
            switch(field) {
              case 'task':
                copy[field] = this.issues.length + 1;
                break;
              case 'taskTerm':
                copy[field].property = 'calendar';
                copy[field].value = '';
                break;
              case 'taskExecutor':
                copy[field] = [{value: '', isMain: true}];
                break;
              default:
                copy[field] = '';
                break;
            }
          }
        }
        this.issues.push(copy);
        return true;
      } else {
        this.issues.push({task: 1+isEnyLocalIssue(), taskName: '', taskContents: '', taskTerm: {property: 'calendar', value: ''},
          taskForm: '', taskController: angular.isObject(creator) ? creator : '', taskExecutor: [{value: '', isMain: true}]});
        return true;
      }
    } else {
      return false;
    }
  };

  this.editIssue = function (editor) {
    testFIO();
    var day = $('#days0').text(), data = $('#data0').text(), header = $('#header0').text(), content = $('#text0').text(), tasks = $('#task0').text();
    if (editor && this.issues.length > 0)
      this.clearIssues();
    var isValid = this.validate();
    if (isValid && day != '') {
      this.issues.push({
        task: tasks,
        taskName: header,
        taskContents: content,
        taskTerm: { property: 'days', value: day },
        taskForm: '',
        taskController: '',
        taskExecutor: { value: { sLogin: '' } }
      });
    } else {
      this.issues.push({
        task: tasks,
        taskName: header,
        taskContents: content,
        taskTerm: { property: 'calendar', value: data },
        taskForm: '',
        taskController: '',
        taskExecutor: { value: { sLogin: '' } }
      });
    }
  };

  this.validate = function () {
    var isValid = true;

    for(var i=0; i<this.issues.length; i++) {
      for(var elem in this.issues[i]) {
        if(this.issues[i].hasOwnProperty(elem) && elem !== 'taskTerm' && elem !== 'rootID' && !this.issues[i][elem] && elem !== 'processID' && elem !== 'action') {
          isValid = false;
        } else if(this.issues[i].hasOwnProperty(elem) && elem === 'taskTerm') {
          angular.forEach(this.issues[i][elem], function (param) {
            if(!param)
              isValid = false;
          })
        } else if (elem === 'taskExecutor') {
          var checkArray = [];
          for (var item=0; item<this.issues[i][elem].length; item++) {
            var executor = this.issues[i][elem][item];
            if (checkArray.indexOf(executor.value.sLogin) > -1) {
              this.issues[i][elem][item].duplicate = true;
              isValid = false;
            } else {
              this.issues[i][elem][item].duplicate = false;
              checkArray.push(executor.value.sLogin);
            }
          }
        }
      }
    }

    return isValid;
  };

  this.removeIssue = function (index) {
    this.issues.splice(index, 1);
  };

  this.addExecutor = function (index) {
    var isValid = true;

    for( var i=0; i<this.issues[index].taskExecutor.length; i++ ) {
      if(!this.issues[index].taskExecutor[i].value) {
        isValid = false;
        break;
      }
    }

    if(isValid) {
      this.issues[index].taskExecutor.push({value: '', isMain: false});
    }

    return isValid;
  };

  this.removeExecutor = function (issue, index) {
    if(this.issues[issue].taskExecutor.length > 1) {
      this.issues[issue].taskExecutor.splice(index, 1);
      for( var i=0; i<this.issues[issue].taskExecutor.length; i++ ){
        if(!this.issues[issue].taskExecutor[i].isMain && i + 1 === this.issues[issue].taskExecutor.length) {
          this.issues[issue].taskExecutor[0].isMain = true;
        }
      }
    }
  };

  this.convertDate = function (date) {
    date = '' + date;
    var splitDate = date.split('/');
    return splitDate[2] + '-' + splitDate[1] + '-' + splitDate[0];
  };

  //Script Oleg convert day
  this.convertDay = function (day) {
    day = '' + day;
    var splitDay = day.split(/[A-zА-я]/g);
    var result = Math.floor(splitDay);
    return result;
  };



  function getTerm(issue) {
    function reverseCDate(date) {
      date = '' + date;
      var splitDate = date.split('-');
      return splitDate[2] + '/' + splitDate[1] + '/' + splitDate[0];
    }

    var obj = {};
    if (issue.aProcessSubject[0].sDatePlan === null)
      obj = {
        property: 'days',
        value: issue.aProcessSubject[0].nDayPlan
      };
    else
      obj = {
        property: 'calendar',
        value: reverseCDate(issue.aProcessSubject[0].sDatePlan)
      };

    return obj;
  };

  this.fillIssueForEdit = function (data, createdByDoc) {
    var self = this;

    if (self.issues)
      self.clearIssues();

    angular.forEach(data, function (issue, key) {
      var preparedObj = {
        task: key + 1,
        taskName: issue.sHead,
        taskContents: issue.visibleBody,
        action: 'edit',
        processID: issue.nID,
        rootID: createdByDoc ? issue.snID_Process_Activiti_Root : null,
        taskTerm: getTerm(issue),
        taskForm: issue.aProcessSubject[0].sTextType,
        taskController: '',
        taskExecutor: []
      };

      var controller = issue.aProcessSubject.filter(function (user) {
        return user.sLoginRole === 'Controller';
      });

      var executors = issue.aProcessSubject.filter(function (user) {
        return user.sLoginRole === 'Executor';
      });

      for (var c=0; c<controller[0].aUser.length; c++) {
        if (controller[0].aUser[c].sLogin === controller[0].sLogin) {
          preparedObj.taskController = {
            name: controller[0].aUser[c].sFirstName + ' ' + controller[0].aUser[c].sLastName,
            sLogin: controller[0].sLogin
          };
        }
      }

      angular.forEach(executors, function(exec) {
        angular.forEach(exec.aUser, function(user) {
          if (exec.sLogin === user.sLogin)
            preparedObj.taskExecutor.push({
              value: {sLogin: exec.sLogin},
              name: user.sFirstName + ' ' + user.sLastName,
              nOrder: exec.nOrder,
              isMain: false
            });
        });
      });
      /*angular.forEach(executors, function (user) {
        preparedObj.taskExecutor.push({
          value: { sLogin: user.sLogin },
          name: user.aUser[0].sFirstName + ' ' + user.aUser[0].sLastName,
          nOrder: user.nOrder,
          isMain: false
        });
      });*/

      preparedObj.taskExecutor = _.sortBy(preparedObj.taskExecutor, 'nOrder');
      preparedObj.taskExecutor[0].isMain = true;

      self.issues.push(preparedObj);
    });
  };

  this.pushIssue = function (issue) {
    this.issues.push(issue);
  };

  this.buildIssueObject = function (issue, taskData) {
    var deferred = $q.defer();
    var isIssueValid = this.validate();
    var that = this;

    if(isIssueValid) {
      var items = this.getIssues();
      var filledArray = [], itemPromises = [], itemDeferred = [];

      for(var i=0; i<items.length; i++) {
        itemDeferred[i] = $q.defer();
        itemPromises[i] = itemDeferred[i].promise;
      }

      function getId(item) {
        if (taskData && (taskData.mProcessVariable && taskData.mProcessVariable.sID_Order_Document || taskData.oProcess.sBP && taskData.oProcess.sBP.indexOf('_doc_') === 0)) {
          if (item.rootID) {
            return item.rootID;
          } else if (taskData.oProcess && taskData.oProcess.nID) {
            return taskData.oProcess.nID.toString();
          } else {
            return null;
          }
        } else {
          return null;
        }
      }

      angular.forEach(items, function (item, key) {
        tasks.uploadFileHtml('issue content', item.taskContents).then(function (res) {

          var exeCopy = angular.copy(item.taskExecutor);

          if (item.taskTerm.property === 'days') {
            var obj = {
              id: item.task,
              sID_BP: issue.bp,
              snID_Process_Activiti_Root: getId(item),
              sHead: item.taskName,
              sActionType: item.action ? item.action : 'set',
              sBody: res,
              sReportType: item.taskForm,
              aProcessSubject: [{
                sLogin: item.taskController.sLogin,
                sLoginRole: 'Controller',
                nDayPlan: that.convertDay(item.taskTerm.value),
                sDatePlan: null
              }]
            };
          } else {
            var obj = {
              id: item.task,
              sID_BP: issue.bp,
              snID_Process_Activiti_Root: getId(item),
              sHead: item.taskName,
              sActionType: item.action ? item.action : 'set',
              sBody: res,
              sReportType: item.taskForm,
              aProcessSubject: [{
                sLogin: item.taskController.sLogin,
                sLoginRole: 'Controller',
                sDatePlan: that.convertDate(item.taskTerm.value),
                nDayPlan: null
              }]
            };
          }

          if (item.processID) {
            obj.snID_ProcessSubjectTask = item.processID.toString();
          }

          for( var exec in exeCopy) {
            if(exeCopy.hasOwnProperty(exec)) {
              if (item.taskTerm.property === 'days') {
                var execObj = {
                  sLogin: exeCopy[exec].value.sLogin,
                  sLoginRole: 'Executor',
                  nDayPlan: that.convertDay(item.taskTerm.value),
                  sDatePlan: null
                };
              } else {
                var execObj = {
                  sLogin: exeCopy[exec].value.sLogin,
                  sLoginRole: 'Executor',
                  sDatePlan: that.convertDate(item.taskTerm.value),
                  nDayPlan: null
                };
              }

              if (exeCopy[exec].isMain) {
                obj.aProcessSubject.splice(1, 0, execObj);
              } else {
                obj.aProcessSubject.push(execObj);
              }
            }
          }
          filledArray.push(obj);
          itemDeferred[key].resolve();
        });
      });

      var Promises = $q.all(itemPromises);
      $q.all([Promises]).then(function () {
        filledArray = $filter('orderBy')(filledArray, 'id');
        angular.forEach(filledArray, function (i) {
          delete i.id;
        });
        deferred.resolve(filledArray);
      });
    } else {
      deferred.resolve(false);
    }

    return deferred.promise;
  };
}]);
