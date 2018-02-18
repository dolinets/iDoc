// Generated on 2015-04-13 using generator-angular-fullstack 2.0.13
'use strict';

module.exports = function (grunt) {
  // var localConfig;
  // try {
  //   localConfig = require('./server/config/local.env');
  // } catch(e) {
  //   localConfig = {};
  // }

  // Load grunt tasks automatically, when needed
  require('jit-grunt')(grunt, {
    express: 'grunt-express-server',
    useminPrepare: 'grunt-usemin',
    usemincss: 'grunt-usemin',
    useminjs: 'grunt-usemin',
    ngtemplates: 'grunt-angular-templates',
    cdnify: 'grunt-google-cdn',
    protractor: 'grunt-protractor-runner',
    injector: 'grunt-asset-injector',
    buildcontrol: 'grunt-build-control'
  });

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project settings
    pkg: grunt.file.readJSON('package.json'),
    yeoman: {
      // configurable paths
      client: require('./bower.json').appPath || 'client',
      root: '..',
      dist: 'dist'
    },
    express: {
      options: {
        port: process.env.PORT || 9002
      },
      dev: {
        options: {
          script: 'server/index.js',
          debug: true
        }
      },
      prod: {
        options: {
          script: 'dist/server/index.js'
        }
      }
    },
    open: {
      server: {
        url: 'http://localhost:<%= express.options.port %>'
      }
    },
    watch: {
      options: {
        livereload: 1337
      },
      injectJS: {
        files: [
          '<%= yeoman.client %>/app/components/**/*.js'],
        tasks: ['injector:scripts']
      },
      injectCss: {
        files: [
          '<%= yeoman.client %>/app/css/**/*.css'
        ],
        tasks: ['injector:css']
      },
      mochaTest: {
        files: ['server/**/*.spec.js'],
        tasks: ['env:test', 'mochaTest']
      },
      jsTest: {
        files: [
          '<%= yeoman.client %>/{app,components}/**/*.spec.js',
          '<%= yeoman.client %>/{app,components}/**/*.mock.js'
        ],
        tasks: ['newer:jshint:all', 'karma']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        files: [
          '{.tmp,<%= yeoman.client %>}/app/css}/**/*.css',
          '{.tmp,<%= yeoman.client %>}/app/components/**/*.html',
          '{.tmp,<%= yeoman.client %>}/app/components/**/*.js',
          '{.tmp,<%= yeoman.client %>}/app/js/**/*.js',
          '{.tmp,<%= yeoman.client %>}/app.js',
          '!{.tmp,<%= yeoman.client %>}{app,components}/**/*.spec.js',
          '!{.tmp,<%= yeoman.client %>}/{app,components}/**/*.mock.js',
          '<%= yeoman.client %>app/assets/images/{,*//*}*.{png,jpg,jpeg,gif,webp,svg}'
        ],
        options: {
          livereload: 1337
        }
      },
      express: {
        files: [
          'server/**/*.{js,json}',
          'client/app/**/*.{js,json}'
        ],
        tasks: ['express:dev', 'wait'],
        options: {
          livereload: 1337,
          nospawn: true //Without this option specified express won't be reloaded
        }
      }
    },

    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '<%= yeoman.client %>/.jshintrc',
        reporter: require('jshint-stylish')
      },
      server: {
        options: {
          jshintrc: 'server/.jshintrc'
        },
        src: [
          'server/**/*.js',
          'client/app/**/*.js',
          'client/app.js',
          '!server/**/*.spec.js'
        ]
      },
      serverTest: {
        options: {
          jshintrc: 'server/.jshintrc-spec'
        },
        src: ['server/**/*.spec.js']
      },
      all: [
        '<%= yeoman.client %>/app/components/**/*.js',
        '{.tmp,<%= yeoman.client %>}/app/js/**/*.js',
        '<%= yeoman.root %>/public-js/*/*.js',
        '!<%= yeoman.client %>/{app,components}/**/*.spec.js',
        '!<%= yeoman.client %>/{app,components}/**/*.mock.js'
      ],
      test: {
        src: [
          '<%= yeoman.client %>/{app,components}/**/*.spec.js',
          '<%= yeoman.client %>/{app,components}/**/*.mock.js',
          '<%= yeoman.root %>/public-js/*/*.spec.js',
          '<%= yeoman.root %>/public-js/*/*.mock.js'
        ]
      }
    },

    // Empties folders to start fresh
    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yeoman.dist %>/*',
            '!<%= yeoman.dist %>/.git*',
            '!<%= yeoman.dist %>/.openshift',
            '!<%= yeoman.dist %>/Procfile'
          ]
        }]
      },
      server: '.tmp'
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 1 version']
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/',
          src: '{,*/}*.css',
          dest: '.tmp/'
        }]
      }
    },

    // Debugging with node inspector
    'node-inspector': {
      custom: {
        options: {
          'web-host': 'localhost'
        }
      }
    },

    // Use nodemon to run server in debug mode with an initial breakpoint
    nodemon: {
      debug: {
        script: 'server/index.js',
        options: {
          nodeArgs: ['--debug-brk'],
          env: {
            PORT: process.env.PORT || 9002
          },
          callback: function (nodemon) {
            nodemon.on('log', function (event) {
              console.log(event.colour);
            });

            // opens browser on initial server start
            nodemon.on('config:update', function () {
              setTimeout(function () {
                require('open')('http://localhost:8080/debug?port=5858');
              }, 500);
            });
          }
        }
      }
    },

    // Automatically inject Bower components into the app

    wiredep: {
      target: {
        src: '<%= yeoman.client %>/index.ejs',
        ignorePath: '<%= yeoman.client %>/',
        exclude: [/bootstrap-sass-official/, /bootstrap.js/, '/json3/', '/es5-shim/']
      }
    },


    // Renames files for browser caching purposes
    // rev: {
    //   dist: {
    //     files: {
    //       src: [
    //         '<%= yeoman.dist %>/public/app/components/{,*/}*.js',
    //         '<%= yeoman.dist %>/public/app.js',
    //         '<%= yeoman.dist %>/public/app/css/{,*/}*.css',
    //         //'<%= yeoman.dist %>/public/assets/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
    //         '<%= yeoman.dist %>/public/assets/fonts/*'
    //       ]
    //     }
    //   }
    // },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them

    useminPrepare: {
      html: ['<%= yeoman.client %>/index.ejs'],
      options: {
        dest: '<%= yeoman.dist %>/public'
      }
    },
    usemincss: {
      css: ['<%= yeoman.client %>/app/css/**/*.css'],
      options: {
        dest: '<%= yeoman.dist %>/public/app/css'
      }
    },

    useminjs: {
      js: ['<%= yeoman.client %>/app/components/*.js'],
      options: {
        dest: '<%= yeoman.dist %>/public/app/components'
      }
    },

    // Performs rewrites based on rev and the useminPrepare configuration
    usemin: {
      html: ['<%= yeoman.dist %>/public/app/components/{,*/}*.html'],
      css: ['<%= yeoman.dist %>/public/app/css/{,*/}*.css'],
      js: ['<%= yeoman.dist %>/public/app/components/{,*/}*.js'],
      options: {
        assetsDirs: [
          '<%= yeoman.dist %>/public',
          // '<%= yeoman.dist %>/public/assets/images'
        ],
        // This is so we update image references in our ng-templates
        patterns: {
          js: [
            [/(assets\/images\/.*?\.(?:gif|jpeg|jpg|png|webp|svg))/gm, 'Update the JS to reference our revved images']
          ]
        }
      }
    },

    // The following *-min tasks produce minified files in the dist folder
    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.client %>/app/assets/images',
          src: '{,*/}*.{png,jpg,jpeg,gif}',
          dest: '<%= yeoman.dist %>/public/assets/images'
        }]
      }
    },

    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.client %>/app/assets/images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/public/assets/images'
        }]
      }
    },

    // Allow the use of non-minsafe AngularJS files. Automatically makes it
    // minsafe compatible so Uglify does not destroy the ng references
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/concat',
          src: '*/**.js',
          dest: '.tmp/concat'
        }]
      }
    },

    // Package all the html partials into a single javascript payload
    ngtemplates: {
      options: {
        // This should be the name of your apps angular module
        module: 'siteiDoc',
        htmlmin: {
          collapseBooleanAttributes: true,
          collapseWhitespace: true,
          removeAttributeQuotes: true,
          removeEmptyAttributes: true,
          removeRedundantAttributes: true,
          removeScriptTypeAttributes: true,
          removeStyleLinkTypeAttributes: true
        },
        usemin: 'app/app.js'
      },
      main: {
        cwd: '<%= yeoman.client %>',
        src: ['app/components/**/*.html'],
        dest: '.tmp/templates.js'
      },
      tmp: {
        cwd: '.tmp',
        src: ['{app,components}**/*.html'],
        dest: '.tmp/tmp-templates.js'
      }
    },

    // Replace Google CDN references
    cdnify: {
      dist: {
        html: ['<%= yeoman.dist %>/public/app/components/*.html']
      }
    },
    uglify: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.client %>',
          src: '*.js',
          dest: '<%= yeoman.public %>',
        }]
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.client %>',
          dest: '<%= yeoman.dist %>/public',
          src: [
            '*.{ico,png,txt}',
            '.htaccess',
            // FIXME Maybe node_modules should be copied below as well?
            'bower_components/**/*',
            'app/assets/images/{,*/}*.{webp}',
            'app/assets/fonts/**/*',
            'app/**/*',
            // 'app/components/**/*.html',
            'app.js',
            'index.ejs'
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/public/app/assets/images',
          src: ['generated/*']
        }, {
          expand: true,
          dest: '<%= yeoman.dist %>',
          src: [
            'package.json',
            'process.json',
            'server/**/*',
            'app/**/*',
            'app.js',
            '!server/**/*.spec.js',
            '!server/**/*.test.js',
            '!server/**/*.nock.js'
          ]
        }, {
          expand: true,
          cwd: '<%= yeoman.root %>',
          dest: '<%= yeoman.dist %>/public',
          src: [
            'public-js/*/*.js'
          ]
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= yeoman.client %>',
        dest: '.tmp/',
        src: ['app/css/**/*.css']
      }
    },

    buildcontrol: {
      options: {
        dir: 'dist',
        commit: true,
        push: true,
        connectCommits: false,
        message: 'Built %sourceName% from commit %sourceCommit% on branch %sourceBranch%'
      },
      heroku: {
        options: {
          remote: 'heroku',
          branch: 'master'
        }
      },
      openshift: {
        options: {
          remote: 'openshift',
          branch: 'master'
        }
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
      ],
      test: [
      ],
      debug: {
        tasks: [
          'nodemon',
          'node-inspector'
        ],
        options: {
          logConcurrentOutput: true
        }
      },
      dist: [
        'imagemin',
        'svgmin'
      ]
    },

    // Test settings
    karma: {
      unit: {
        configFile: 'karma.conf.js',
        singleRun: true
      }
    },

    mochaTest: {
      options: {
        reporter: 'spec'
      },
      src: ['server/**/*.spec.js']
    },

    protractor: {
      options: {
        configFile: 'protractor.conf.js'
      },
      chrome: {
        options: {
          args: {
            browser: 'chrome'
          }
        }
      }
    },

    env: {
      test: {
        NODE_ENV: 'test'
      },
      prod: {
        NODE_ENV: 'production'
      }
    },

    injector: {
      options: {

      },
      // Inject application script files into index.html (doesn't include bower)
      scripts: {
        options: {
          transform: function (filePath) {
            console.log('before ' + filePath);

            filePath = filePath.replace('/client/app', '');
            filePath = filePath.replace('/.tmp/', '');
            filePath = filePath.replace('/../', '');

            console.log('after ' + filePath);
            return '<script src="' + filePath + '"></script>';
          },
          starttag: '<!-- injector:js -->',
          endtag: '<!-- endinjector -->'
        },
        files: {
          '<%= yeoman.client %>/index.ejs': [
            ['{.tmp,<%= yeoman.client %>}/app/components/**/*.js',
              '!{.tmp,<%= yeoman.client %>}/{app,components}/**/*.spec.js',
              '!{.tmp,<%= yeoman.client %>}/{app,components}/**/*.mock.js']
          ]
        }
      },

      // Inject component css into index.html
      css: {
        options: {
          transform: function(filePath) {
            filePath = filePath.replace('/client/app', '');
            filePath = filePath.replace('/.tmp/', '');
            return '<link rel="stylesheet" href="' + filePath + '">';
          },
          starttag: '<!-- injector:css -->',
          endtag: '<!-- endinjector -->'
        },
        files: {
          '<%= yeoman.client %>/index.ejs': [
            '<%= yeoman.client %>/app/css/**/*.css'
          ]
        }
      }
    },
  });

  // Used for delaying livereload until after server has restarted
  grunt.registerTask('wait', function () {
    grunt.log.ok('Waiting for server reload...');

    var done = this.async();

    setTimeout(function () {
      grunt.log.writeln('Done waiting!');
      done();
    }, 1500);
  });

  grunt.registerTask('express-keepalive', 'Keep grunt running', function() {
    this.async();
  });


  grunt.registerTask('client', function() {
    grunt.task.run([
      'clean:server',
      'env:all',
      'concurrent:server',
      'injector',
      'wiredep',
      'autoprefixer',
      'open',
      'watch'
    ]);
  });

  grunt.registerTask('serve', function (target) {
    if (target === 'dist') {
      return grunt.task.run([
        'build',
        // 'test:client',
        'env:all',
        'env:prod',
        'express:prod',
        'wait',
        'open',
        'express-keepalive']);
    }

    if (target === 'debug') {
      return grunt.task.run([
        'clean:server',
        'env:all',
        'concurrent:server',
        'injector',
        'wiredep',
        'autoprefixer',
        'concurrent:debug'
      ]);
    }

    grunt.task.run([
      'clean:server',
      'env:all',
      'concurrent:server',
      'injector',
      'wiredep',
      'autoprefixer',
      'express:dev',
      'wait',
      'open',
      'watch'
    ]);
  });

  grunt.registerTask('server', function () {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve']);
  });

  grunt.registerTask('test', function(target) {
    if (target === 'server') {
      return grunt.task.run([
        'env:all',
        'env:test',
        'mochaTest'
      ]);
    }

    else if (target === 'client') {
      return grunt.task.run([
        'clean:server',
        'env:all',
        'concurrent:test',
        'injector',
        'autoprefixer',
        'karma'
      ]);
    }


    else grunt.task.run([
        'test:server',
        'test:client'
      ]);
  });

  grunt.registerTask('build', [
    'clean:dist',
    'concurrent:dist',
    'injector',
    'wiredep',
    'useminPrepare',
    'autoprefixer',
    'ngtemplates',
    'concat',
    'ngAnnotate',
    'copy:dist',
   // 'rev',

    // 'htmlmin',
    'usemin',
    // 'usemincss',
    // 'useminjs',
    'cdnify',
    'uglify'
  ]);

  grunt.registerTask('default', [
    'newer:jshint',
    'test',
    'build'
  ]);


};