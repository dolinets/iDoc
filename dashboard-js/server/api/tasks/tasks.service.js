// Реализовать открытие по урл-у "расширенного профиля задачи" и ссылку для админа из "обычного профиля" #1015
var isTaskDataAllowedForUser = function (taskData, currentUser) {
  // 4.1) отображать тем, кто входит в группу: admin,super-admin
  if (currentUser.roles.indexOf('admin') || currentUser.roles.indexOf('super_admin'))
    return true;
  // 4.2) а также тем на кого эта таска ассйнута
  if (taskData.sLoginAssigned == currentUser.id)
    return true;
  // 4.3) а так-же тем, кто входит в группу, в которую входит эта таска и одновременно - когда она не ассайнута
  // или когда он входит в группу manager и она ассайнута на другого т.е.
  // (входит в группу, в которую входит эта таска) && (она не ассайнута || (он входит в группу manager && она ассайнута на другого))
  if (typeof taskData.aGroup == 'undefined')
    return true;
  var groups = $.grep(taskData.aGroup || taskData.aGroup, function (group) {
    return currentUser.roles.indexOf(group) > -1;
  });
  if (groups.length > 0 && (!taskData.sLoginAssigned || currentUser.roles.indexOf('manager') > -1)) {
    return true;
  }
  return false;
};

var subFoldersName = {
  agree: 'Agree', accept: 'Accept', seen: 'Seen', execute: 'Execute', direct: 'Direct', watch: 'Watch'
};

var parentFolder = function (sub) {
  var folders = {
    DocumentOpenedUnassignedUnprocessed: ['agree', 'accept', 'seen', 'execute', 'direct', 'watch']
  };

  for (var folder in folders) {
    if (folders.hasOwnProperty(folder) && folders[folder].indexOf(sub) > -1) {
      return folder;
    }
  }
};

var getSubFolder = function (folder) {
  var parent = parentFolder(folder);
  return parent + ':' + subFoldersName[folder];
};

module.exports = {
  isTaskDataAllowedForUser: isTaskDataAllowedForUser,
  getSubfolder: getSubFolder
};
