'use strict';

var config = require('./config/config');
var multer = require('multer');
var log = require('./libs/log')(module);
var upload = multer();
const nodemailer = require('nodemailer');


  nodemailer.StartSendEmail = function (message) {

  nodemailer.createTestAccount(function(err, account){

    // create reusable transporter object using the default SMTP transport

   var transporter = nodemailer.createTransport({
        host: 'smtp.ethereal.email',
        port: 587,
        secure: false, // true for 465, false for other ports
        service:config.service,
        auth: config.auth
    });

//setup email data with unicode symbols
      if(message.phone) {
          var mailOptions = {
              from: 'Новая заявка с сайта  👻 <' + message.email + '>', // sender address
              to: config.list_recipients, // list of receivers
              subject: 'Клиент - ' + message.name, // Subject line
              html: '<h4>Подана заявка с сайта:</h4>' +
              '<p><b>ФИО клиента: </b>' + message.name + '</p>' +
              '<p><b>Телефон клиента: </b>' + message.phone + '</p>' +
              '<p><b>Почтовый адресс клиента: </b>' + message.email + '</p><br>'+
              '<p><b>Необходимо связаться с клиентом </b></p>'
          };
      }else{
          var mailOptions = {
              from: '"Презентация iDoc" <start.idoc@gmail.com>', // sender address
              to: message.email, // list of receivers
              subject: 'Презентация iDoc', // Subject line
              html: '<h4>Данная презентация поможет Вам познакомиться немного ближе с работой iDoc</h4>' +
              '<p>По всем вопросам обращайтесь к:</p>' +
              '<p>Менеджеру проекта <b>Антон Швачка </b>email: anton.shvachka@gmail.com, телефон: 0..</p>' +
              '<p>Директору проекта <b>Белявцев Владимир </b>email: bvv4ik@gmail.com, телефон: 0..</p>',
              attachments: [
                         {
                      filename: config.filename,
                           path: (function(){
                             try{
                               config.path;
                             }
                             catch (e) {
                               config.path_dist;
                             }
                           })(),

                      content: 'Send e-mail for client',
                      contentType: config.contentType  // optional, would be detected from the filename
                        }
                    ]
          };
      }

    transporter.sendMail(mailOptions, function (error, info) {
      if (error) {
        return log.info(error);
      }
     var result = info.messageId;
       log.info('Message sent: %s', info.messageId);
       log.info('Preview URL: %s', nodemailer.getTestMessageUrl(info));
    });
  });
 }

module.exports = nodemailer;


