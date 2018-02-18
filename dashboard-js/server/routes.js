/**
 * Main application routes
 */

'use strict';

var errors = require('./components/errors');

module.exports = function(app) {

  // Insert routes below
  app.use('/api/processes', require('./api/process'));
  app.use('/api/tasks', require('./api/tasks'));
  app.use('/api/reports', require('./api/reports'));
  app.use('/api/schedule', require('./api/schedule'));
  app.use('/api/escalations', require('./api/escalations'));
  app.use('/api/deploy', require('./api/deploy'));
  app.use('/api/env', require('./api/env'));
  app.use('/api/markers', require('./api/markers'));
  app.use('/auth', require('./auth'));
  app.use('/api/profile', require('./api/profile'));
  app.use('/api/users', require('./api/user'));
  app.use('/api/countries', require('./api/countries'));
  app.use('/api/currencies', require('./api/currencies'));
  app.use('/api/object-customs', require('./api/object-customs'));
  app.use('/api/subject', require('./api/subject'));
  app.use('/api/object-earth-target', require('./api/object-earth-target'));
  app.use('/api/subject-action-kved', require('./api/subject-action-kved'));
  app.use('/api/object-place', require('./api/object-place'));
  app.use('/api/subject-role', require('./api/subject-role'));
  app.use('/api/documents', require('./api/documents'));
  app.use('/api/fields-list', require('./api/fields-list'));
  app.use('/api/uploadfile', require('./api/uploadfile'));
  app.use('/api/create-task', require('./api/create-task'));
  app.use('/api/organization-info', require('./api/organization-info'));
  app.use('/api/generate', require('./api/generate'));
  app.use('/api/product-list', require('./api/product-list'));
  app.use('/api/chat', require('./api/chat'));
  app.use('/api/staff', require('./api/staff'));
  app.use('/api/share', require('./api/share'));
  app.use('/api/server', require('./api/server'));
  app.use('/api/access', require('./api/access'));
  app.use('/api/events', require('./api/events'));
  app.use('/api/table', require('./api/table'));

  // All undefined asset or api routes should return a 404
  app.route('/:url(api|auth|components|app|bower_components|assets|public-js)/*')
   .get(errors[404]);

  // All other routes should redirect to the index.html
  app.route('/*')
    .get(function(req, res) {
      res.sendfile(app.get('appPath') + '/index.html');
    });
};
