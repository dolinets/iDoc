angular.module('iGovMarkers')
  .service('FieldMotionService', ['iGovMarkers', '$rootScope', FieldMotionService]);

function FieldMotionService(MarkersFactory, $rootScope) {
  this.FieldMentioned = {
    'in': function (fieldId, prefix) {
      return grepByPrefix(prefix).some(function (entry) {
        return _.contains(entry.aField_ID, fieldId);
      });
    },
    inShow: function (fieldId) {
      return this.in(fieldId, 'ShowFieldsOn');
    },
    inRequired: function (fieldId) {
      return this.in(fieldId, 'RequiredFieldsOn');
    },
    inWritable: function (fieldId) {
      return this.in(fieldId, "WritableFieldsOnCondition_");
    },
    inPrintForm: function (fieldId) {
      return this.in(fieldId, "PrintForm_");
    },
  };

  this.getCalcFieldsIds = function () {
    return grepByPrefix('ValuesFieldsOnCondition')
      .map(function (e) {
        return e.aField_ID;
      })
      .reduce(function (prev, curr) {
        return prev.concat(curr);
      }, []);
  };

  this.getTargetFieldsIds = function (prefix) {
    return grepByPrefix(prefix + 'FieldsOnCondition')
      .map(function (e) {
        if (e.aField_ID) {
          return e.aField_ID;
        } else {
          return [];
        }
      })
      .reduce(function (prev, curr) {
        return prev.concat(curr);
      }, []);
  };

  this.getPrintForms = function (fieldId) {

    var printForms = grepByPrefix("PrintForm_");

    if (fieldId) {
      printForms = printForms.some(function (entry) {
        return _.contains(entry.aField_ID, fieldId);
      });
    }

    return printForms;
  };

  this.getPrintFormsById = function (fieldId) {
    return getPrintForms(fieldId);
  };

  this.isFieldWritable = function (fieldId, formData) {
    return grepByPrefix('WritableFieldsOnCondition_').some(function (entry) {
      return evalCondition(entry, fieldId, formData);
    });
  };

  this.isFieldVisible = function (fieldId, formData) {
    var showOnNotEmpty = grepByPrefix('ShowFieldsOnNotEmpty_');
    var showOnCondition = grepByPrefix('ShowFieldsOnCondition_');
    var b1 = showOnNotEmpty.some(function (e) {
      if (formData[e.sField_ID_s]) {
        return formData[e.sField_ID_s]
          && $.trim(formData[e.sField_ID_s].value)
          && _.contains(e.aField_ID, fieldId);
      } else {
        angular.forEach(formData, function (item) {
          if (item.id === [e.sField_ID_s]) {
            return item
              && $.trim(item.value)
              && _.contains(e.aField_ID, fieldId)
          }
        })
      }
    });
    return b1 || showOnCondition.some(function (entry) {
      return evalCondition(entry, fieldId, formData);
    });
  };

  this.isFieldRequired = function (fieldId, formData) {
    var b = grepByPrefix('RequiredFieldsOnCondition').some(function (entry) {
      return evalCondition(entry, fieldId, formData);
    });
    return b;
  };

  var fieldId_entryTriggered = {};
  var aFieldIDs = [];
    this.calcFieldValue = function (fieldId, formData, formProperties) {
        var entry = _.find(grepByPrefix('ValuesFieldsOnCondition'), function (entry) {
            return evalCondition(entry, fieldId, formData)
        });
        var result = {value: '', differentTriggered: false};
        if (entry) {
            if (aFieldIDs.length == 0) {
                for (var key in formData) if (formData.hasOwnProperty(key)) {
                    aFieldIDs.push(key);
                }
                /* Добираемся до колонок таблицы
                  и добавляем id колонки в массив aFieldIDs
                  для того, чтобы данные колонки были
                  замечены маркером.
                 */
                // for(var key in formProperties) {
                //     if(formProperties.hasOwnProperty(key) && formProperties[key].type === 'table') {
                //         var rows = formProperties[key].aRow;
                //         for (var row in rows) {
                //             if(rows.hasOwnProperty(row) && row.length !== 0) {
                //                 var fields = rows[row];
                //                 for(var field in fields) {
                //                     if(fields.hasOwnProperty(field) && field.length !== 0) {
                //                         var tables = fields[field];
                //                         for(var table in tables) {
                //                             if(table.id !== null && !table.id && table.id !== '') {
                //                                 aFieldIDs.push(tables[table].id);
                //                             }
                //                         }
                //                     }
                //                 }
                //             }
                //         }
                //     }
                // }
            }
            result.differentTriggered = fieldId_entryTriggered[fieldId] ? (fieldId_entryTriggered[fieldId] != entry) : true;
            entry.asID_Field_sValue_Interpolated = [];

            angular.forEach(entry.asID_Field_sValue, function (sValue) {
                var interpolatedEntry = MarkersFactory.interpolateString(sValue, formData, '[', ']', aFieldIDs, formProperties);
                entry.asID_Field_sValue_Interpolated.push(interpolatedEntry.value);
                if (interpolatedEntry.differentTriggered) {
                    result.differentTriggered = true;
                }
            });
            var sPreparedValue = entry.sValue ? entry.sValue : entry.asID_Field_sValue_Interpolated[$.inArray(fieldId, entry.aField_ID)];
            if(angular.isString(sPreparedValue) && (sPreparedValue.indexOf('+')>0 || sPreparedValue.indexOf('-')>0 || sPreparedValue.indexOf('*')>0 || sPreparedValue.indexOf('/')>0)){
                try{
                    var value = sPreparedValue.split(/([()\+\-\*\/])/).map(function(a){
                        return parseFloat(a) || a;
                    }).join('');
                    // Проверяем является ли число датой
                    if(value.split('/').length === 3 /*(value.match(/\//g)||[]).length > 1*/) {
                        var aDateValue = value.split('/');
                        var nDays = parseInt(aDateValue[0].replace(/[()\+\-\*\/]/g, ""));
                        var nMonths = parseInt(aDateValue[1].replace(/[()\+\-\*\/]/g, "").split(' ')[0]);
                        var nYear = parseInt(aDateValue[2].replace(/[()\+\-\*\/]/g, ""));
                        if( nDays >= 1 && nDays <= 31 &&
                            nMonths >= 1 && nMonths <= 12 &&
                            nYear >= 1900 && nYear <= 3000) {
                            result.value = sPreparedValue;
                        } else {
                            var evaluated = eval(value);
                        }
                    } else {
                        var evaluated = eval(value);
                    }

                    if(angular.isNumber(evaluated)){
                        result.value = evaluated;
                    } else {
                        result.value = sPreparedValue;
                    }
                }catch (e){
                    result.value = sPreparedValue;
                }
            } else {
                result.value = sPreparedValue;
            }

          if (typeof result.value === 'number' && entry.sRounding) {
            var trunc = parseInt(entry.sRounding.replace(/'/g, ''));
            if (!isNaN(trunc))
              result.value = truncator(result.value, trunc);
          }
        }
        fieldId_entryTriggered[fieldId] = entry;
        return result;
    };

  this.getElementIds = function () {
    return grepByPrefix('ShowElementsOnTrue_')
      .map(function (e) {
        return e.aElement_ID;
      })
      .reduce(function (p, c) {
        return p.concat(c);
      }, []);
  };

  this.isElementVisible = function (elementId, formData) {
    var mentioned = {val: false};
    var bval = grepByPrefix('ShowElementsOnTrue_').some(function (entry) {
      return evalCondition(entry, elementId, formData, mentioned);
    });
    return mentioned.val ? bval : true;
  };

  this.getSplittingRules = function () {
    return grepByPrefix('SplitTextHalf_').reduce(function (p, c) {
      p[c.sID_Field] = {splitter: c.sSpliter, el_id1: c.sID_Element_sValue1, el_id2: c.sID_Element_sValue2};
      return p;
    }, {});
  };

  this.getReplacingRules = function () {
    return grepByPrefix('ReplaceTextSymbols_').reduce(function (p, c) {
      p[c.sID_Field] = {symbols: c.nSymbols, valueNew: c.sValueNew, el_id1: c.sID_Field, el_id2: c.sID_Element_sValue};
      return p;
    }, {});
  };
  /**
   *  function isPrintFormVisible
   *   Evaluates if specified PrintForm_ item sCondition
   *
   * @returns true - if PrintForm_ sCondition is true for tableRow
   * @author Sysprog
   */
  this.isPrintFormVisible = function (printForm, fieldObject, formData, tableRow) {

    var isVisible = false;

    if ((printForm.sCondition == null || printForm.sCondition.length < 2 )) {
      isVisible = true;
    } else {
      isVisible = evalCondition(printForm, fieldObject.id, formData, false, tableRow);
    }

    return isVisible;
  };

  function evalCondition(entry, fieldId, formData, mentioned, tableRow) {

    if (!_.contains(entry.aField_ID || entry.aElement_ID, fieldId)) {
      return false;
    } else if (mentioned) {
      mentioned.val = true;
    }

    var toEval = entry.sCondition.replace(/\[(\w+)]/g, function (str, alias) {
      var fId = 0;
      if (entry.asID_Field != null) {
        fId = entry.asID_Field[alias];
      } else if (entry.asEnumField_ID != null) {
        fId = entry.asEnumField_ID[alias];
      }

      if (fId == 0)
        var result = '';
      if (formData[fId]) {
        if (formData[fId] && (formData[fId].type != "enum") && (typeof formData[fId].value === 'string' || formData[fId].value instanceof String)) {
          result = formData[fId].value.replace(/'/g, "\\'");
        } else if (formData[fId] && (formData[fId].type === "enum" )) {
          var enumItem = MarkersFactory.getEnumItemById(formData[fId], formData[fId].value);
          if (enumItem != null) {
            result = enumItem.id;
          } else {
            result = formData[fId].value;
          }
        } else if (formData.hasOwnProperty(fId)) {
          result = formData[fId].value;
        } else {
          //console.log('can\'t find field [',fId,'] in ' + JSON.stringify(formData));
        }
      } else {
        angular.forEach(formData, function (item) {
          if (item.id === fId) {
            if (item && (item.type != "enum") && (typeof item.value === 'string' || item.value instanceof String)) {
              result = item.value.replace(/'/g, "\\'");
            } else if (item && ( item.type === "enum" )) {
              var enumItem = MarkersFactory.getEnumItemById(item, item.value);
              if (enumItem != null) {
                result = enumItem.id;
              } else {
                result = item.value;
              }
            } else if (item.hasOwnProperty(fId)) {
              result = item.value;
            } else {
              //console.log('can\'t find field [',fId,'] in ' + JSON.stringify(formData));
            }
          } else if (item.type === 'table' && tableRow !== null && (tableRow && tableRow.aField)) { // search for table value
            angular.forEach(tableRow.aField, function (rowCell, rowCellKey, rowItem) {
              if (rowCell.id === fId) {
                result = rowCell.value;
                return;
              }
            });
          }

        })
      }

      if (result && result.indexOf('Enum') === -1 && $rootScope.historyTaskData) {
        result = $rootScope.historyTaskData.mProcessVariable[fId];
      }

      switch (alias.charAt(0)) {
        case 'b':
          result = result.toString();
          break;
        case 's':
          result = "'" + result + "'";
          break;
        case 'n':
          result = result ? parseFloat(result) : 0;
          break;
        default:
          console.log('invalid alias format, alias:' + alias);
      }
      return result;
    });

    try {
      return eval(toEval);
    } catch (e) {
      console.log('OnCondition expression error\n' + e.name + '\n' + e.message
        + '\nexpression:' + entry.sCondition
        + '\nresolved expression:' + toEval);
      throw e;
    }
  }

  /**
   * function getEnumItemById
   *  Returns for field enum by Id
   *
   * @returns enumItem for enumValue or null
   * @author Sysprog
   */
  this.getEnumItemById = function (field, enumValue) {
    return MarkersFactory.getEnumItemById(field, enumValue);
  };

  function grepByPrefix(prefix) {
    return MarkersFactory.grepByPrefix('motion', prefix);
  }

  function truncator(numToTruncate, intDecimalPlaces) {    
    var numPower = Math.pow(10, intDecimalPlaces);
    return ~~(numToTruncate * numPower)/numPower;
  }

  this.reset = function () {
    fieldId_entryTriggered = {};
    aFieldIDs = [];
  }
}
