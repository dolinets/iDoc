angular.module('iGovTable', ['autocompleteService', 'iGovMarkers', 'datepickerService'])
    .service('TableService',
        ['autocompletesDataFactory', 'ValidationService', 'DatepickerFactory', '$injector', '$q', '$http',
            function (autocompletesDataFactory, ValidationService, DatepickerFactory, $injector, $q, $http) {

        var factory = $injector.has('FileFactory') ? $injector.get('FileFactory') : null;
        var serverService = $injector.has('CurrentServer') ? $injector.get('CurrentServer') : null;

        var addTableFieldsProperties = function (formProps) {
        angular.forEach(formProps, function(prop) {
            if (prop.type === 'table') {
                angular.forEach(prop.aRow, function (fields) {
                    if(fields && fields.aField){
                      angular.forEach(fields.aField, function (item, key, obj) {

                        // добавляем примечания к полям, если они есть. проверка по разделителю ";"
                        var sFieldName = item.name || '';
                        var aNameParts = sFieldName.split(';');
                        var sFieldNotes = aNameParts[0].trim();
                        item.sFieldLabel = sFieldNotes;
                        sFieldNotes = null;
                        if (aNameParts.length > 1) {
                          sFieldNotes = aNameParts[1].trim();
                          if (sFieldNotes === '') {
                            sFieldNotes = null;
                          }
                        }
                        item.sFieldNotes = sFieldNotes;

                        var isExecutorSelect = item.name.split(';')[2];

                        if (item.type === 'date') {
                          obj[key].props = DatepickerFactory.prototype.createFactory();
                        }else if(item.type === 'file' && factory !== null) {
                          var temp = obj[key];
                          obj[key] = new factory();
                          for(var k in temp) obj[key][k]=temp[k];
                        }else if (item.type === 'select' || item.type === 'string' || isExecutorSelect && isExecutorSelect.indexOf('sID_SubjectRole=Executor') > -1) {
                          var match;
                          if (((match = item.id.match(/^s(Currency|ObjectCustoms|SubjectOrganJoinTax|ObjectEarthTarget|Country|ID_SubjectActionKVED|ID_ObjectPlace_UA)(_(\d+))?/)))
                            ||(item.type == 'select' && (match = item.id.match(/^s(Country)(_(\d+))?/))) || isExecutorSelect) {
                            if (match && autocompletesDataFactory[match[1]] && !isExecutorSelect) {
                              item.type = 'select';
                              item.selectType = 'autocomplete';
                              item.autocompleteName = match[1];
                              if (match[2])
                                item.autocompleteName += match[2];
                              item.autocompleteData = autocompletesDataFactory[match[1]];
                            } else if (!match && isExecutorSelect.indexOf('SubjectRole') > -1) {
                              var props = isExecutorSelect.split(','), role;
                              item.type = 'select';
                              item.selectType = 'autocomplete';
                              for(var i=0; i<props.length; i++) {
                                if(props[i].indexOf('sID_SubjectRole') > -1) {
                                  role = props[i];
                                  break;
                                }
                              }
                              var roleValue = role ? role.split('=')[1] : null;
                              if(roleValue && roleValue === 'Executor') item.autocompleteName = 'SubjectRole';
                              if(roleValue && roleValue === 'ExecutorDepart') item.autocompleteName = 'SubjectRoleDept';
                              item.autocompleteData = autocompletesDataFactory[item.autocompleteName];
                            } else if (!match && isExecutorSelect.indexOf('Relation') > -1) {
                                var prodProps = isExecutorSelect.split(','), prodValue;
                                item.type= 'select';
                                item.selectType = 'autocomplete';
                                for(var j = 0; j < prodProps.length; j++){
                                    if (prodProps[j].indexOf('sID_Relation') > -1){
                                        prodValue = prodProps[j];
                                        break;
                                    }
                                }
                                if ((prodValue ? prodValue.split('=')[1] : null)==='sID_Relation'){
                                    item.autocompleteName = 'ProductList';
                                }
                                item.autocompleteData = autocompletesDataFactory[item.autocompleteName];
                            }
                          }
                        }
                        if (item.nWidth && item.nWidth.indexOf('%') === -1) {
                          if(item.nWidth.indexOf('px') === -1) item.nWidth = item.nWidth + 'px';
                        }
                      })
                    } else {
                      console.warn('В таблице "' + prop.name.split(';')[0] + '" [id=' + prop.id + '] в массиве строк отсутствуют элементы');
                    }
                })
            }
        });
    };

    // добавление свойства с максимальным к-вом строк таблицы (если лимит задан)
    var checkRowsLimit = function (formProps) {
        angular.forEach(formProps, function(item, key, obj) {
            if(item.type === 'table') {
                var hasOptions = item.name.split(';');
                if(hasOptions.length === 3) {
                    var hasLimit = hasOptions[2].match(/\b(nRowsLimit=(\d+))\b/);
                    if(hasLimit !== null)
                        obj[key].nRowsLimit = hasLimit[2];
                }
            }
        })
    };

    /*
     * инициируем таблицу, передавая массив полей форм в качестве аргумента.
     * checkRowsLimit() - проверяет лимит на к-во строк таблицы.
     * addTableFieldsProperties() - для работы полей типа date, organJoin, select/autocomplete
     */

    var getType = function (field, property) {
        switch (property) {
            case 'type':
                return field.type ? 'type' : 'sType';
            case 'value':
                return field.value ? 'value' : 'oValue';
            case 'id':
                return field.id ? 'id' : (field.sId ? 'sId' : 'sID');
        }
    };

    var wasUploadEarlier = function (param) {
        return !!(param && param.indexOf('sID_StorageType') > -1 && param.indexOf('sKey') > -1);
    };

    var disableTableInputs = function (row) {
        angular.forEach(row, function (fields) {
           angular.forEach(fields.aField, function (field, key, obj) {
               obj[key].writable = false;
           });
        });
    };


    /*
    * Инициализация таблиц (наполнение дефолтных таблиц, наполнение с монги, ранее сохраненной таблицы);
    * Параметры:
    * @param {array} form - массив полей формы, обязательный.
    * @param {string} path - путь к api по загрузке таблиц, обязательный.
    * */
    this.init = function(form, path) {
        var counter = 0,
            tForm = form;
        var deferred = $q.defer(),
            savedTablesDefer = [],
            savedTablesDeferPromises = [];
        var savedTables = [],
            initialTables = [];

        // загрузка контента таблиц с бд
        function getTableContent(counter, tables, defers) {
            if (counter < tables.length) {
                try {
                    var val = getType(tables[counter], 'value');
                    var json = JSON.parse(tables[counter][val]);
                    var params = {
                        sKey: json['sKey'],
                        sID_StorageType: json['sID_StorageType'],
                        sFileNameAndExt: json['sFileNameAndExt']
                    };

                    if (serverService !== null) {
                        var serverData = serverService.getServer();
                        params.taskServer = serverData.another ? serverData.name : null;
                    }
                    $http.get(path, {params: params}).then(function(result) {
                        if (result.data) {
                            var loadedTable = JSON.parse(result.data);
                            if (!tables[counter].aRow)
                                tables[counter]['aRow'] = [];
                            tables[counter].aRow = loadedTable.aRow;
                            defers[counter].resolve(result);
                            getTableContent(counter + 1, tables, defers);
                        }
                    });
                } catch (e) {
                    console.error(e);
                }
            }
        }

        // сортируем таблицы с дефолтным велью (новые) и джейсоном в велью для загрузки
        angular.forEach(tForm, function(element) {
            var type = getType(element, 'type');
            if (element[type] === 'table') {
                var val = getType(element, 'value');
                if (wasUploadEarlier(element[val])) {
                    savedTables.push(element);
                } else {
                    initialTables.push(element);
                }
            }
        });

        // парсим дефолтный джейсон, проверяем лимит строк, настройка ячеек
        if (initialTables.length) {
            angular.forEach(initialTables, function(item, key, obj) {
                if(!item.aRow) {
                    item.aRow = [];
                }
                try {
                    var parsedTable = JSON.parse(item.value);
                    obj[key].aRow.push(parsedTable);
                } catch (e) {
                    console.log('error message: ' + e)
                }
            });
            checkRowsLimit(initialTables);
            addTableFieldsProperties(initialTables);
        }

        if (savedTables.length) {
            angular.forEach(savedTables, function(table, key) {
                savedTablesDefer[key] = $q.defer();
                savedTablesDeferPromises[key] = savedTablesDefer[key].promise;
            });
        }

        // загрузка ранее сохраненных таблиц с бд
        getTableContent(counter, savedTables, savedTablesDefer);

        $q.all(savedTablesDeferPromises).then(function() {
            var allTables = savedTables.concat(initialTables);
            angular.forEach(allTables, function(table) {
                var length = tForm.length;
                for (var i=0; i<length; i++) {
                    var paramIdFirst = getType(table, 'id'),
                        tableID = table[paramIdFirst];
                    var paramIdSecond = getType(tForm[i], 'id'),
                        formID = tForm[i][paramIdSecond];
                    if (tableID === formID) {
                        var id = getType(tForm[i], 'id');
                        tForm[i] = table;
                        // if (id === 'sId') {
                        //     tForm[i].sId = tForm[i].id;
                        //     delete tForm[i].id;
                        //     tForm[i].sType = tForm[i].type;
                        //     delete tForm[i].type;
                        // }
                        // if (!tForm[i].writable) {
                        //     disableTableInputs(tForm[i].aRow);
                        // }
                        break;
                    }
                }
            });
            deferred.resolve(tForm);
        });

        return deferred.promise;
    };

    /*
     * проверка поля на редактирование.
     * иногда передается false/true как строка 'false'/'true', поэтому включил данную проверку
     */

    this.isFieldWritable = function (field) {
        if(field === undefined) {
            return true;
        } else{
            if(typeof field === 'string' || field instanceof String) {
                if(field === 'true') return true;
                if(field === 'false') return false;
            } else if (typeof field === 'boolean') {
                return field;
            }
        }
    };

    /*
    * проверка поля таблицы на видимость. принцип как и isFieldWritable
    * todo обьединить в общую функцию.
     */
    this.isVisible = function (field) {
      if('bVisible' in field) {
          if(typeof field === 'string' || field instanceof String) {
              if(field === 'true') return true;
              if(field === 'false') return false;
          } else if (typeof field === 'boolean') {
              return field;
          }
      } else {
          return true;
      }
    };

    /*
     * добавление новой строки в таблице, посредством копирования дефолтной строки (тк она образцовая),
     * addTableFieldsProperties() - для работы полей типа date, organJoin, select/autocomplete
     */

    this.addRow = function (id, form) {
        angular.forEach(form, function (item, key, obj) {
            if(item.id === id) {
                var rows = obj[key].aRow;
                var defaultCopy = angular.copy(rows[0]);
                angular.forEach(defaultCopy.aField, function (field, k, o) {
                    if(field.type === 'file') {
                        var copy = field;
                        factory !== null ? o[k] = new factory() : o[k] = {};
                        o[k].sFieldLabel = copy.sFieldLabel;
                        o[k].required = copy.required;
                        o[k].type = copy.type;
                        o[k].name = copy.name;
                        o[k].writable = copy.writable;
                        o[k].id = copy.id + '_' + obj[key].aRow.length;
                    }
                    if(field.default) {
                        delete field.default;
                    } else if(field.props) {
                        field.props.value = ""
                    }
                    field.value = "";
                });
                addTableFieldsProperties();
                obj[key].aRow.push(defaultCopy);
            }
        });
    };

    // удаление строки таблицы.
    this.removeRow = function (form, index, id) {
        angular.forEach(form, function (item, key, obj) {
            if (item.id === id) {
              obj[key].aRow.splice(index, 1);
                var rows = item.aRow;
                var fields = rows[rows.length - 1];
            }
        });
    };

    this.clearRow = function (form, index, id) {
        angular.forEach(form, function (item, key, obj) {
            if (item.id === id) {
                for (var n = 0; n < obj[key].aRow[0].aField.length; n++) {
                    obj[key].aRow[0].aField[n].value = null;
                    if (obj[key].aRow[0].aField[n].fileName){
                        obj[key].aRow[0].aField[n].fileName = null;
                    }
                }
            }
        });
    };

    // проверка ограничения на к-во строк в таблице (достиг ли лимит)
    this.rowLengthCheckLimit = function (table) {
        return table.aRow.length >= table.nRowsLimit
    };
}]);