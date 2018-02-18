angular.module('dashboardJsApp')
  .filter('orderByLogin', function () {
    
    function getCookie(name) {
      var matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
      ));
      return matches ? decodeURIComponent(matches[1]) : undefined;
    }

    return function (items) {
      if (!items || items.length < 2) return items;

      var prefix = getCookie('user');
      if (prefix) {
        prefix = JSON.parse(prefix);
        prefix = prefix.id.split('_')[0];
      } else
        return items;
      
      var sorted = [];
      angular.forEach(items, function(item) {
        if (item && item.sLogin && item.sLogin.indexOf(prefix) > -1)
          sorted.unshift(item);
        else 
          sorted.push(item);
      });

      return sorted;
    };
  });