angular.module('dashboardJsApp')
    .filter('fileType', function() {
        return function(file) {
            if(!file)
                return;
            var extension = file.split(".");
            var ext = extension[extension.length - 1].toLowerCase();
            var type;
            // check for image
            switch (ext) {
                case "bmp":
                case "jpg":
                case "jpeg":
                case "gif":
                case "png":
                    type = "img";
                    break;
                default:
                    break;
            }
            // check for pdf
            switch (ext) {
                case "pdf":
                    type = "pdf";
                    break;
                default:
                    break;
            }
            // check for text or html file
            switch (ext) {
                case "txt":
                case "html":
                case "htm":
                    type = "text";
                    break;
                default:
                    break;
            }
            return type ? type : undefined;

        }
    });