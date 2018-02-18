angular.module('dashboardJsApp').factory('SubFolder', [function () {
  var subFolderObject = {
    title: '',
    type: '',
    count: 0,
    showCount: false,
    tab: '',
    hasSubFolder: false,
    subFolder: true
  };

  var foldersWithSubFolders = ['documents'];

  return {
    /**
     * составление обьекта подпапок
     *
     * Параметры:
     * @param subArray {Object} - список подпапок (с бека);
     * @param {Object[]} docFolder - массив вкладок документов;
     * @param {Object[]} taskFolder - массив вкладок тасок;
     * @param {Object} tabName - обьект наименований вкладок бека;
     */
    subFolderObj: function (subArray, docFolder, taskFolder, tabName) {
      var folders = docFolder.concat(taskFolder);
      var self = this;

      for (var sub in subArray) {
        if (subArray.hasOwnProperty(sub)) {
          for (var i=0; i<folders.length; i++) {
            folders[i].hasSubFolder = folders[i].tab === tabName[sub];
          }

          var aSubFolder = {};

          angular.forEach(subArray[sub], function (subTab) {
            if (!aSubFolder[tabName[sub]]) {
              aSubFolder[tabName[sub]] = {folders: [], show: false, parent: sub};
            }
            aSubFolder[tabName[sub]].folders.push(self.fillSubObj(subTab.sNote, subTab.sName, subTab.bFolder));
          });

          return aSubFolder;
        }
      }
    },

    /**
     * создание обьекта подвкладки
     *
     * Параметры:
     * @param {String} title - название вкладки для отображения;
     * @param {String} tab - название вкладки для урла;
     * @param {Boolean} show - показывать ли данную вкладку (задается на беке);
     */
    fillSubObj: function (title, tab, show) {
      return {
        title: title,
        type: tab.toLowerCase(),
        count: 0,
        showCount: false,
        tab: tab.toLowerCase(),
        subFolder: true,
        showFolder: show
      }
    },

    /**
     * проверка на наличие подвкладок
     *
     * Параметры:
     * @param {String|Array} folder - для проверки на наличие подвкладок передаем или вкладку или массив вкладок;
     */
    hasSubFolder: function (folder) {
      if (Array.isArray(folder)) {
        for (var i=0; i<folder.length; i++) {
          if (foldersWithSubFolders.indexOf(folder[i]) > -1) {
            return true;
          } else if (i === folder.length - 1) {
            return false;
          }
        }
      } else {
        return foldersWithSubFolders.indexOf(folder) > -1;
      }
    }
  }
}]);
