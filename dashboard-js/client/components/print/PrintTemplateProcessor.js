'use strict';

//angular.module('dashboardJsApp').factory('PrintTemplateProcessor', ['$sce', 'Auth', '$filter', 'FieldMotionService', '$lunaService', function ($sce, Auth, $filter, FieldMotionService, lunaService) {
angular.module('dashboardJsApp').factory('PrintTemplateProcessor', ['$sce', 'Auth', '$filter', 'FieldMotionService', '$stateParams', function ($sce, Auth, $filter, FieldMotionService, $stateParams) {
  function processMotion(printTemplate, form, fieldGetter) {

    function convertObjKeys(objArray) {
      if (!objArray[0].sId) return;

      var tmpArray = [];
      for (var i = 0; i < objArray.length; i++) {
        var objStr =  JSON.stringify(objArray[i]);

        objStr = objStr.replace(/sId/g, 'id').replace(/sName/g, 'name').replace(/oValue/g, 'value').replace(/sType/g, 'type').replace(/bReadable/g, 'readable').replace(/bWritable/g, 'writable');

        tmpArray.push( JSON.parse(objStr) );
      }

      return tmpArray;
    }

    if ($stateParams.type === 'docHistory')
      form = convertObjKeys(form)

    var formData = form.reduce(function(prev, curr) {
      prev[curr.id] = curr;
      return prev;
    }, {});
    var template = $('<div/>').append(printTemplate);
    FieldMotionService.getElementIds().forEach(function(id) {
      var el = template.find('#' + id);
      if (el.length > 0 && !FieldMotionService.isElementVisible(id, formData))
        el.remove();
    });
    var splittingRules = FieldMotionService.getSplittingRules();
    var replacingRules = FieldMotionService.getReplacingRules();
    form.forEach(function(e) {
      var val = fieldGetter(e);
      if (val && _.has(splittingRules, e.id)) {
        var rule = splittingRules[e.id];
        var a = val.split(rule.splitter);
        template.find('#' + rule.el_id1).html(a[0]);
        a.shift();
        template.find('#' + rule.el_id2).html(a.join(rule.splitter));
      }
      if (val && _.has(replacingRules, e.id)) {
        rule = replacingRules[e.id];
        //a = val.slice(0, val.length - rule.nSymbols) + rule.sValueNew;
        // a = val.replace(rule.sFrom, rule.sTo);
        // template.find('#' + rule.sID_Element_sValue).html(a);
        var a = val.split(' ');
        var b = a[0].split('');
        b.splice(b.length - rule.symbols, rule.symbols, rule.valueNew);
        var c = b.join("");
        a.splice(0, 1, c);
        var result = a.join(' ');
        template.find('.' + rule.el_id2).html(result);
      }
    });
    return template.html();
  }

  return {
    processPrintTemplate: function (form, printTemplate, reg, fieldGetter, signersPrint, issuesPrint) {
      var _printTemplate = printTemplate;
      var templates = [], ids = [], found;
      while (found = reg.exec(_printTemplate)) {
        templates.push(found[1]);
        ids.push(found[2]);
      }
      if (templates.length > 0 && ids.length > 0) {
        templates.forEach(function (templateID, i) {
          var id = ids[i];
          if (id) {
            if(id === 'header_signers' || id === 'footer_signers'){
              var sVal = signersPrint(id);
              _printTemplate = _printTemplate.replace(templateID, sVal);
            }

            if (id === 'Issue_print'){
              var sVal = issuesPrint();
              _printTemplate = _printTemplate.replace(templateID, sVal);
            }
            var item = form.filter(function (item) {
              if ($stateParams.type === 'docHistory'){
                return item.sId === id;
              } else {
                return item.id === id;
              }
            })[0];
            if (item) {
              var sValue = fieldGetter(item);
              if (sValue === null || sValue === undefined){
                sValue = "";
              }
              _printTemplate = _printTemplate.replace(templateID, sValue);//fieldGetter(item)
            }
          }
        });
      }
      return _printTemplate;
    },
    //наполнение принтформы данными из таблицы. поиск по ид таблицы маркера, при необходмости клонирование, сразу же наполняем.
    fillPrintTable: function (form, printTemplate, reg) {
      var self = this;
      var _printTemplate = printTemplate;
      var templates = [], ids = [], found, idArray = [];
      while (found = reg.exec(_printTemplate)) {
        templates.push(found[1]);
        ids.push(found[2]);
      }
      var matchesIds = [];
      // ищем маркеры что определяют в принтформе таблицы
      angular.forEach(templates, function (template) {
        var comment = template.match(/<!--[\s\S]*?-->/g);
        if(Array.isArray(comment)) {
          for(var i=0; i<comment.length; i++) {
            comment[i] = comment[i].match(/\w+/)[0];
          }
        }
        if(comment) matchesIds.push(comment);
      });

      // формируем массив из ид маркеров таблиц что встретились в принтформе
      angular.forEach(matchesIds, function (ids) {
        var arr = ids.filter(function(item, pos, self) {
          return self.indexOf(item) == pos;
        });
        idArray.push(arr);
      });

      // когда ид маркера принтформы совпало с ид таблицы - заменяем теги на поля таблицы. проверяем к-во строк таблицы
      // если больше одной - клонируем до нужного к-ва, после наполняем таблицей.
      angular.forEach(idArray, function(id) {
        angular.forEach(form.taskData.aTable, function(item) {
          if(item.id === id[0]) {
            angular.forEach(templates, function (template) {
              var commentedField = template.match(/<!--.*?-->/)[0];
              var uncommentedField = commentedField.split('--')[1];
              var result = uncommentedField.slice(1);
              if(result === id[0]){
                var withAddedRowsTemplate = template.repeat(item.aRow.length);
                angular.forEach(item.aRow, function (row) {
                  angular.forEach(row.aField, function (field) {
                    var fieldId = function () {
                      if(field.type === 'enum' && field.value) {
                        for (var j = 0; j < field.a.length; j++) {
                          if (field.a[j].name === field.value) {
                            return field.a[j].name ? field.a[j].name : ' ';
                          }
                        }
                      } else if(field.type === 'enum' && !field.value) {
                        return ' ';
                      } else if(field.type === 'date' && !field.value) {
                        return field.props && field.props.value ? field.props.value.split('T')[0] : ' ';
                      } else if (field.value) return field.value;
                        else if (field.default) return field.default;
                        else if (field.props) return field.props.value;
                        else return ' ';
                    };
                    withAddedRowsTemplate = self.populateSystemTag(withAddedRowsTemplate, '['+ field.id +']', fieldId, true);
                  })
                });
                _printTemplate = _printTemplate.replace(template, withAddedRowsTemplate);
              }
            })
          }
        });
      });
      return _printTemplate
    },
    populateSystemTag: function (printTemplate, tag, replaceWith, table) {
      var replacement;
      if (replaceWith instanceof Function) {
        replacement = replaceWith();
      } else {
        replacement = replaceWith;
      }
      if(table) {
        return printTemplate.replace(new RegExp(this.escapeRegExp(tag)), replacement);
      } else {
        return printTemplate.replace(new RegExp(this.escapeRegExp(tag), 'g'), replacement);
      }
    },

    /**
     * function populateTableField (printTemplate, printFormTableObject)
     *  Searches printTemplate for [oTableName.sTableField] and replaces
     *   with data of specified row of printFormTableObject.nRowIndex
     *
     * @returns original template with replaced values
     * @author Sysprog
     */
    populateTableField: function( printTemplate, printFormTableObject ) {

      var replacement;
    	var tag;
      var templateString = "";

      if( printTemplate.length ) {
         templateString = printTemplate;
      }
      else {
         templateString = $sce.getTrustedHtml( printTemplate );
      }

      if(printFormTableObject.oRow) {

        for ( var fieldIndex in printFormTableObject.oRow.aField ) {

          var field = printFormTableObject.oRow.aField[fieldIndex];

          if( field.type === "enum") {
             var enumItem = FieldMotionService.getEnumItemById( field, field.value );
             if( enumItem != null) {
               replacement = enumItem.name;
             }
          }
          else {
            replacement = field.value;
          }

    		  tag = "["+ printFormTableObject.sTableName + "." + field.id + "]";

      	  templateString = templateString.replace(new RegExp(this.escapeRegExp(tag)), replacement);

        }
      }

      if( printTemplate.length ) {
         return templateString;
      }
      else {
         return $sce.trustAsHtml( templateString );
      }
    },
    escapeRegExp: function (str) {
      return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
    },
    getPrintTemplate: function (form, originalPrintTemplate, signers, issues) {

      var usersArray = [];

      function fillUsersObject(issues) {
        angular.forEach(issues, function (issue) {
          var tempArr = [];
          angular.forEach(issue.aProcessSubject, function (user) {
            if (user.sLoginRole === 'Controller') {
              tempArr.unshift(user);
            } else {
              if (user.aProcessSubjectChild) {
                var child = angular.copy(user.aProcessSubjectChild),
                  parent = angular.copy(user),
                  controllerKey;

                parent.aProcessSubjectChild = [];
                tempArr.push(parent);

                angular.forEach(child, function (c, key, obj) {
                  if (c.sLoginRole === 'Controller') {
                    if (tempArr[tempArr.length - 1].sLogin === c.sLogin) {
                      if (c.oProcessSubjectStatus && c.oProcessSubjectStatus.sName && c.sText) {
                        tempArr[tempArr.length - 1].oProcessSubjectStatus = c.oProcessSubjectStatus;
                        tempArr[tempArr.length - 1].sText = c.sText;
                      }
                    }
                    controllerKey = key;
                  } else {
                    obj[key].isDelegated = parent.sLogin;
                  }
                });
                if (controllerKey){
                  child.splice(controllerKey, 1);
                }
                tempArr[tempArr.length - 1].aProcessSubjectChild = tempArr[tempArr.length - 1].aProcessSubjectChild.concat(child);
              } else {
                tempArr.push(user);
              }
            }
          });
          usersArray.push(tempArr);
          usersArray[0] = _.sortBy(usersArray[0], 'nOrder');
        });
      }


      function convertDate(i) {
        var date = i.split(' ')[0];
        var splittedDate = date.split('-');
        return splittedDate[2] + '.' + splittedDate[1] + '.' + splittedDate[0];
      }
      function convertDay(day) {
        return day;
      }
      function getIssueType(type) {
        switch (type) {
          case 'string':
            return 'Документ';
          case 'textArea':
            return 'Текстове повiдомлення';
          case 'file':
            return 'Файл';
        }
      }

      function getInitials (name) {
        if (name) {
          var nameAndMiddleName = name.split(' ');
          if (nameAndMiddleName.length === 2) {
            return nameAndMiddleName[0][0] + '.' + nameAndMiddleName[1][0] + '.';
          }
        } else {
          return name;
        }
      };
      function searchMinOrder(issue) {
        var lowestOrder;

        angular.forEach(issue.aProcessSubject, function (user) {
          if (user.sLoginRole !== 'Controller') {
            if (!lowestOrder) {
              lowestOrder = user;
            } else {
              if (lowestOrder.nOrder > user.nOrder) {
                lowestOrder = user;
              }
            }
          }
        });

        return lowestOrder.nOrder;
      }

      function issuesPrint() {
        fillUsersObject(issues);
        var wrapper = createTag('div');
        issues.forEach(function (issue, index) {
          var taskDivider = createTag('div', 'addition-hr');
          var hr1 = createTag('hr', ['hr-divider', 'side-left']);
          var hr2 = createTag('hr', ['hr-divider', 'side-right']);
          var taskNumber = createTag('span', null, 'max-width: 18%; display: inline-block;', 'Завдання №' + (index + 1));
          taskDivider.append(hr1);
          taskDivider.append(taskNumber);
          taskDivider.append(hr2);
          wrapper.append(taskDivider);

          var issueHeader = createTag('div', null, 'font-weight: bold', issue.sHead);
          wrapper.append(issueHeader);
          var issueText = createTag('div', null, null, issue.visibleBody);
          wrapper.append(issueText);

          var execution_date;
          if (issue.aProcessSubject[0].sDatePlan){
            execution_date = convertDate(issue.aProcessSubject[0].sDatePlan);
          } else if (issue.aProcessSubject[0].nDayPlan) {
            execution_date = convertDay(issue.aProcessSubject[0].nDayPlan);
          }

          var issueTable = createTag('table', null, 'width: 100%');
          var tHead =  '<tr style="font-weight: normal">' +
            '<th style="width: 20%">' +
            '<span>Контроль</span>' +
            '</th>' +
            '<th style="width: 40%; border-left: 5px solid white; border-right: 5px solid white;">' +
            '<span>Виконання </span>' + '<span style="font-weight: normal">' + execution_date + '</span>' +
            '</th><th style="width: 40%;"><div><span>Звiт </span>' +
            '<span style="font-weight: normal"> (' + getIssueType(issue.aProcessSubject[0].sTextType) + ')</span></div>' +
            '</th>' +
            '</tr>';
          var tableHead = createTag('thead', null, 'background: #F2F2F2;', tHead);
          issueTable.append(tableHead);
          wrapper.append(issueTable);

          var currentIssue = index;
          var executors = createTag('td', null, 'width: 40%');
          var controller;
          var reportBlock = createTag('td', null, 'width: 40%');
          usersArray[currentIssue].forEach(function (user, index) {
            if (user.sLoginRole !== 'Controller'){
              if (index === 1) {
                usersArray[currentIssue][0].aUser.forEach(function (u) {
                  if (usersArray[currentIssue][0].sLogin === u.sLogin){
                    controller = createTag('td', null, 'width: 20%', u.sFirstName + ' ' + getInitials(u.sLastName));
                  }
                })
              }

              user.aUser.forEach(function (u) {
                if (user.sLogin === u.sLogin){
                  if (user.nOrder === searchMinOrder(issue) && !user.isDelegated) {
                    var executor = createTag('div', null, null, u.sFirstName + ' ' + getInitials(u.sLastName) + ' (Головний виконавець)');
                    executors.append(executor);
                  } else {
                    var executor = createTag('div', null, null, u.sFirstName + ' ' + getInitials(u.sLastName));
                    executors.append(executor);
                  }
                }
              });

              if (user.aProcessSubjectChild && user.aProcessSubjectChild.length > 0) {
                var delegateBlock= createTag('div');
                user.aProcessSubjectChild.forEach(function(delegators) {
                  delegators.aUser.forEach(function (u) {
                    if (delegators.sLogin === u.sLogin){
                      var delegator = createTag('span', null, null, '&#8618' + u.sFirstName + ' ' + getInitials(u.sLastName));
                      delegateBlock.append(delegator);
                    }
                  });
                });
                executors.append(delegateBlock);
              }

              if (user.oProcessSubjectStatus.sName && user.oProcessSubjectStatus.sID !== 'new') {
                var name = createTag('span', null, 'font-weight: bold', user.oProcessSubjectStatus.sName + ' ');
                reportBlock.append(name);
              }

              if (user.sText) {
                if (user.sText.indexOf('sKey') > -1 && user.sText.indexOf('sFileNameAndExt') > -1){
                  var fileName = JSON.parse(user.sText).sFileNameAndExt;
                  var fileNameAndExt = createTag('span', null, null, 'Було додано файл ' + fileName);
                  reportBlock.append(fileNameAndExt);
                } else {
                  var sText = createTag('span', null, null, user.sText);
                  reportBlock.append(sText);
                }
              }
            }
          });

          var tableBody = createTag('tbody');
          tableBody.append(controller);
          tableBody.append(executors);
          tableBody.append(reportBlock);


          issueTable.append(tableBody);
          wrapper.append(issueTable);



        });
        return htmlObjToString(wrapper);
      }


      function signersPrint(tag) {
        var wrapper = createTag('div', 'row');
          if(tag === 'header_signers'){
            signers.forEach(function (item) {
            var colMainOffset = createTag('div', ['col-xs-6', 'col-xs-offset-6']);
              if(/step_4/i.test(item.sKeyStep) && item.aUser.length && item.bWrite!==null && item.bWrite)
                angular.forEach(item.aUser, function(user) {
                  if (user.sLogin === user.sID_Group) {
                    var colLeft = createTag('div', 'col-xs-6');
                    var colRight = createTag('div', 'col-xs-6');
                    for (var i = 0; i < 3; i++) {
                      var styleCss = i===0 ?'font-weight:bold' : (i===1 ? 'font-size:85%' : '');
                      var content = i===0 ? user.sCompany : (i===1 ? user.sSubjectHumanPosition : shortName(user.sFIO));
                      var tmpTag = createTag('span', '', styleCss+';display:block', content);
                      colLeft.append(tmpTag);
                    }
                    var sign = (item.sDate && (item.oDocumentStepSubjectSignType && item.oDocumentStepSubjectSignType.sID  === 'sign' || !item.oDocumentStepSubjectSignType))? item.oDocumentStepType.sSing : ((item.sDate && item.oDocumentStepSubjectSignType.sID !== 'sign')? item.oDocumentStepSubjectSignType.sName : '');
                    var sDate = item.sDate ? ' ('+item.sDate+')' : 'Немає підпису';
                    colRight.append(sign);
                    colRight.append(sDate);
                    colMainOffset.append(colLeft);
                    colMainOffset.append(colRight);
                    wrapper.append(colMainOffset);
                  }
                });

            });
            return htmlObjToString(wrapper);
          } else {
            signers.forEach(function (item) {
              if(/step_\d/i.test(item.sKeyStep) && item.aUser.length && item.sKeyStep !== 'step_6_Edit' && item.bWrite!==null){
                var row = createTag('div', 'row');
                var colLeft = createTag('div', 'col-xs-6');
                var colRight = createTag('div', 'col-xs-6');
                var fio = createTag('span', '', 'display: block; text-align: right', item.aUser[0].sFIO);
                var role = createTag('span', '', 'font-size: 11px; display: block; color: dimgrey; text-align: right', item.aUser[0].sSubjectHumanPosition);
                var company = createTag('b', null, null, '&nbsp;'+item.aUser[0].sCompany);
                role.append(company);
                var sign = createTag('span', '', 'font-weignt: bold', (item.sDate && (item.oDocumentStepSubjectSignType && item.oDocumentStepSubjectSignType.sID  === 'sign' || !item.oDocumentStepSubjectSignType))? '<b>'+item.oDocumentStepType.sSing+'</b>' : ((item.sDate && item.oDocumentStepSubjectSignType.sID !== 'sign')? '<b>'+item.oDocumentStepSubjectSignType.sName+'</b>' : ''));
                var sDate = createTag('span', '', '', item.sDate ? '&nbsp;('+item.sDate+')' : 'Немає підпису');
                colLeft.append(fio);
                colLeft.append(role);
                if(item.sLogin_Referent !== item.aUser[0].sLogin && item.sLogin_Referent){
                  var referent = createTag('span', '', 'display: block; text-align: right', '('+item.sFIO_Referent+')');
                  colLeft.append(referent);
                }
                colRight.append(sign);
                colRight.append(sDate);
                if (signers.indexOf(item) > 0 && signers[signers.indexOf(item)-1].oDocumentStepType.sNote !== item.oDocumentStepType.sNote)
                  addDivider(row, item.oDocumentStepType.sNote);
                row.append(colLeft);
                row.append(colRight);
                wrapper.append(row);
              }
            });
            return htmlObjToString(wrapper);
          }
      }
        function addDivider(parentNode, text) {
          if (!parentNode) return;

          var divider = createTag('div', 'addition-hr');
          var hr1 = createTag('hr', ['hr-divider', 'side-left']);
          var hr2 = createTag('hr', ['hr-divider', 'side-right']);
          var span  = createTag('span', null, 'max-width: 18%; display: inline-block;', text)

          divider.append(hr1);
          divider.append(span);
          divider.append(hr2);

          parentNode.append(divider);
        }

        function shortName(name) {
          var _name = name.split(' ');
          // _name.unshift(_name[_name.length - 1]);
          // _name.pop(_name[_name.length - 1]);
          for (var i = 1; i < _name.length; i++) {
            _name[i] = _name[i].slice(0, 1).concat(".");
          }
          return _name.join(" ");
        }
        function createTag(tag, cssClass, style, text) {
          var elem = document.createElement(tag);
          if(cssClass) {
            if(Array.isArray(cssClass)){
              for(var i=0;i<cssClass.length;i++){ elem.classList.add(cssClass[i]); };
            }else{ elem.classList.add(cssClass); }
          }
          if(style) elem.style.cssText = style;
          if(text) elem.innerHTML = text;
          return elem;
        }

        function htmlObjToString(node) {
          // convert html Object to string it usefull when we paste into _printTemplate
          var temp = document.createElement('div');
          temp.appendChild(node.cloneNode(true));
          var string = temp.innerHTML;
          temp = node = null;
          return string;
        }

      // helper function for getting field value for different types of fields
      function fieldGetter(item) {
        if ($stateParams.type === 'docHistory'){
          if (item.sType === 'enum') {
            var enumID = item.oValue;
            var enumItem;
            if (item.enumValues){
             enumItem = item.enumValues.filter(function (enumObj) {
                return enumObj.id === enumID;
              })[0];
            }
            if (enumItem && enumItem.sName) {
              var enumItemName = enumItem.sName;
              var enumItemNameArray = enumItemName.split('|');
              if (!_.isEmpty(enumItemNameArray[1])) {
                return enumItemNameArray[1];
              }
              else {
                return enumItemNameArray[0];
              }
            } else
              return '';
          } else if(item.sType === 'fileHTML') {
            if(item.valueVisible) {
              return item.valueVisible;
            }
          }else if (item.sType === 'date'){
            return $filter('checkDateReverse')(item.oValue);
          } else
            return item.oValue;
        } else {
          if (item.type === 'enum') {
            var enumID = item.value;
            var enumItem;
            if (item.enumValues)
              enumItem = item.enumValues.filter(function (enumObj) {
                return enumObj.id === enumID;
              })[0];
            if (enumItem && enumItem.name) {
              var enumItemName = enumItem.name;
              var enumItemNameArray = enumItemName.split('|');
              if (!_.isEmpty(enumItemNameArray[1])) {
                return enumItemNameArray[1];
              }
              else {
                return enumItemNameArray[0];
              }
            } else
              return '';
          } else if(item.type === 'fileHTML') {
            if(item.valueVisible) {
              return item.valueVisible;
            }
          }else if (item.type === 'date'){
            return $filter('checkDateReverse')(item.value);
          } else return item.value;
        } 
      }

      function getLunaValue(id) {

        // Number 2187501 must give CRC=3
        // Check: http://planetcalc.ru/2464/
        if(id===null || id === 0){
          return null;
        }
        var n = parseInt(id);
        var nFactor = 1;
        var nCRC = 0;
        var nAddend;

        while (n !== 0) {
          nAddend = Math.round(nFactor * (n % 10));
          nFactor = (nFactor === 2) ? 1 : 2;
          nAddend = nAddend > 9 ? nAddend - 9 : nAddend;
          nCRC += nAddend;
          n = parseInt(n / 10);
        }

        nCRC = nCRC % 10;
        return nCRC;
      }

      var printTemplate = this.processPrintTemplate(form, originalPrintTemplate, /(\[(\w+)])/g, fieldGetter, signersPrint, issuesPrint);
      // What is this for? // Sergey P
      printTemplate = this.processPrintTemplate(form, printTemplate, /(\[label=(\w+)])/g, function (item) {
        return item.name;
      });
      printTemplate = this.fillPrintTable(form, printTemplate, /(?=<!--\[)([\s\S]*?]-->)/g);
      printTemplate = this.populateSystemTag(printTemplate, "[sUserInfo]", function () {
        var user = Auth.getCurrentUser();
        return user.lastName + ' ' + user.firstName ;
      });
      printTemplate = this.populateSystemTag(printTemplate, "[sCurrentDateTime]", $filter('date')(new Date(), 'yyyy-MM-dd HH:mm'));
      //printTemplate = this.populateSystemTag(printTemplate, "[sDateCreate]", $filter('date')(task.createTime.replace(' ', 'T'), 'yyyy-MM-dd HH:mm'));
      printTemplate = this.populateSystemTag(printTemplate, "[sDateCreate]", $filter('date')(form.taskData.sDateTimeCreate, 'yyyy-MM-dd HH:mm'));

      //№{{task.processInstanceId}}{{lunaService.getLunaValue(task.processInstanceId)}}
      //$scope.lunaService = lunaService;
      //lunaService.getLunaValue(
      printTemplate = this.populateSystemTag(printTemplate, "[sID_Order]", form.taskData.oProcess.nID+getLunaValue(form.taskData.oProcess.nID)+"");

      // #998 реализовать поддержку системного тэга [sDateTimeCreateProcess], [sDateCreateProcess] и [sTimeCreateProcess]
      // в принтформе, вместо которого будет подставляться Дата создания процесса
      // (в формате "YYYY-MM-DD hh:mm", "YYYY-MM-DD" и "hh:mm")
      try {
        if (angular.isDefined(form.taskData) && angular.isDefined(form.taskData.oProcess)) {
          printTemplate = this.populateSystemTag(printTemplate, "[sDateTimeCreateProcess]", function () {
            return $filter('date')(form.taskData.oProcess.sDateCreate.replace(' ', 'T'), 'yyyy-MM-dd HH:mm');
          });
          printTemplate = this.populateSystemTag(printTemplate, "[sDateCreateProcess]", function () {
            return $filter('date')(form.taskData.oProcess.sDateCreate.replace(' ', 'T'), 'yyyy-MM-dd');
          });
          printTemplate = this.populateSystemTag(printTemplate, "[sTimeCreateProcess]", function () {
            return $filter('date')(form.taskData.oProcess.sDateCreate.replace(' ', 'T'), 'HH:mm');
          });
        }
      } catch (e) {
        Modal.inform.error()(form.taskData.message)
      }
      return $sce.trustAsHtml(processMotion(printTemplate, form, fieldGetter));
    }
  }
}]);
