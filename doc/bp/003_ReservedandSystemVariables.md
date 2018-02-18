## Переменные BankID
* **bankIdlastName** - string - фамилия гражданина
* **bankIdfirstName** - string - имя гражданина
* **bankIdmiddleName** - string - отчество гражданина
* **bankIdPassport** - string  -паспортные данные гражданина
* **bankId_scan_passport** - file - скан паспорта гражданина
* **bankIdAddressFactual** - string - адрес регистрации гражданина
* **bankIdAddressFactual_country** - string/invisible - страна 
* **bankIdAddressFactual_state** - string/invisible - область
* **bankIdAddressFactual_area** - string/invisible - район
* **bankIdAddressFactual_city** - string/invisible - город
* **bankIdAddressFactual_street** - string/invisible - улица
* **bankIdAddressFactual_houseNo** - string/invisible - дом
* **bankIdAddressFactual_flatNo** - string/invisible - квартира регистрации
* **bankIdinn** - string - инн заявителя
* **bankIdbirthDay** - string - дата рождения гражданина (у форматі ДД.ММ.РРРР)
* **bankIdemail** - string - емейл гражданина
* **bankIdphone** - string -телефон гражданина
* **bankIdsID_Country** - string - гражданство
* **bankId_scan_inn** - file - Скан копия ИНН гражданина
* ![3_0](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_0.JPG)

## Зарезервированные переменные для электронных очередей
* **date_of_visit** - invisible - автоматом принимает значение выбранное  из электронной очереди
* **nID_Department_visitDay** - string/invisible/label - номер органа для электронной очереди, где visitDay это id  электронной очереди, к которой относится текущий департамент
* **nSlots_visitDay** - string/invisible/label - количество слотов очереди , которые резервируются пользователем. (где visitDay это id  электронной очереди, к которой относится текущий размер слота)
* ![3_5](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_5.JPG)

## Зарезервированные переменные атрибутов
* **sNameOrgan** - string/invisible/label - название органа в подписи письма
* **sWorkTime** - string invisible/label - график работы
* **sPhoneOrgan** - string/invisible/label - телефон для справок
* **sAddress** - string/invisible/label - адрес органа
* **sMailClerk** - string/invisible/label - почта чиновника
* **sArea** - string/invisible/label - yазвание нас.пункта/района куда подается заявка
* **nArea** - string/invisible/label - yомер в справочнике нас.пункта/района куда подается заявка
* **sShapka** - string/invisible/label - шапка принтформы
* ![3_1](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_1.JPG)


## Переменные принтформ
* **[sID_Order]** - системный тег для принтформы, смс или емейла  для размещения ИД заявки. [Issue 1023](https://github.com/e-government-ua/i/issues/1023).  
* **[sDateCreate]** - Системный тег даты. Возвращает значение системного времени на момент срабатывания таски. Можно использовать как время начала обработки обращения (взятия в работу чиновником).
* ![3_6](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_6.JPG)
* **[sDateTimeCreateProcess]** - Возвращает значение системной даты-времени на момент сохранения\подачи заявки гражданином.
* **[sDateCreateProcess]**- Возвращает значение системной даты на момент сохранения\подачи заявки гражданином.
* **[sTimeCreateProcess]** - Возвращает значение системного времени на момент сохранения\подачи заявки гражданином.
* **[sCurrentDateTime]** - Возвращает значение системной даты и времени на текущий момент.
* **sBody** - invisible - задать печатную форму.  
Прописывается в юзертаске. Для корректной работы обязательно надо прописать листнер “fileTaskInheritance”
Путь на печатную форму в папке patterns задается в поле name (типа [pattern/print/subsidy_zayava.html]) 
* **PrintForm** - Позволяет автоматически создавать файл из соответствующей принтформы, который потом можно подгружать к вложениям в письмо на сервис-таске (используем ${PrintForm_1} при отправке письма с вложениями). Номер PrintForm должен совпадать с номером sBody.

## Валидируемые переменные
* **vin_code, vin_code1, vin** - string - VIN-код авто.
Набор из 17 символов. Разрешено использовать все арабские цифры и латинские буквы (А В C D F Е G Н J К L N М Р R S Т V W U X Y Z) , за исключением букв Q, O, I. Эти буквы запрещены для использования, поскольку O и Q похожи между собой, а I и O можно спутать с 0 и 1.
* ![3_4](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_4.JPG)
* **privatePhone, workPhone, phone** - string - номер телефона.
Первый символ “+”, остальные 12 цифр
* **lastName_UA1,  firstName_UA1, middleName_UA1, lastName_UA2,  firstName_UA2, middleName_UA2**  - string - Название или ФИО с украинскими буквами. Разрешена только кириллица, дефис, апостроф.
* **lastName_RU1,  firstName_RU1, middleName_RU1, lastName_RU2,  firstName_RU2, middleName_RU2** - string - Название или ФИО с русскими буквами. Разрешена только кириллица, дефис.
* **date_of_birth** - date - дата рождения. Не разрешено выбирать дату больше текущей.
* **kved** - string - вид экономической деятельности по КВЕД. Две цифры точка две цифры (первые две цифры не могут быть 04, 34, 40, 44, 48, 54, 57, 67, 76, 83, 89).
* ![3_2](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_2.JPG)
* **edrpou** - string - восемь цифр.
* ![3_3](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_3.JPG)
* **mfo** - string - шесть цифр.
* **kids_Birth** - date - не разрешено выбирать дату больше текущей, разница между текущей датой и выбранной не должна превышать 14 лет.
* **privateMail, email** - string - емейлы
* **landNumb**- string - кадастровый номер в формате хххххххххх:хх:ххх:хххх

## Другие
* **response** - invisible - задать кастомизированный текст на спасибо странице, после подачи обращения (с поддержкой html)
* **footer** - string - задать кастомизированный текст на стандартной форме для печати в дашборде( с поддержкой html)
* **sNotifyEvent_AfterSubmit** - invisible - Отображение кастомного текста в дашборде после нажатия на кнопку “Опрацювати”. Текст  подсказки задаем в аттрибуте default. [Issue 1027](https://github.com/e-government-ua/i/issues/1027).
* **bReferent** - invisible - признак заполнения заявки референтом (true/false).
* **form_signed** - если объявлена эта переменная на стартовой форме, то при нажатии на кнопку "замовити послугу" заявитель будет перенаправлен на доп.страницу для наложения ЕЦП на заявку.
* ![3_9](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_9.JPG)
* **form_signed_all** - при наложении ЕЦП на заявку, она так же будет наложена и на все прикрепленные файлы. При этом все файлы, которые прикрепил гражданин, должны иметь расширение *.pdf.

## Автокомплиты
* **sObjectCustoms** - select - Товар 
* **sID_UA_ObjectCustoms** - Код товара (заполнится автоматически после выбора в селекте sObjectCustoms)
* ![3_8](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_8.JPG)
* **sCountry** - select - Страна 
* ![3_7](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/3_7.JPG)
* **sID_UA_Country** - Код страны (заполнится автоматически после выбора в селекте sCountry)
* **sCurrency** - select - Валюта 
* **sID_UA_Currenc**y - Код валюти (заполнится автоматически после выбора в селекте sCurrency)
* **sSubjectOrganJoinTax** - select - Таможня
* **sID_UA_SubjectOrganJoinTax** - Код таможни (заполнится автоматически после выбора в селекте sSubjectOrganJoinTax)
* **sID_Place_UA** - string - В переменную передается КОАТУУ выбранного населенного пункта (поле Place)

## Переменные-запросы
**sID_SubjectOrgan_OKPO_** - string - Делает запрос к базе ранее полученных данных из ЕДРПОУ, по соответствующему коду предприятия, и возвращает результат в несколько зарезервированных переменных (если по запросу найдены данные). Возможно неограниченное количество полей запроса на форме - **sID_SubjectOrgan_OKPO_** используется как префикс, главное обеспечить уникальность **id** каждой следующей переменной-запроса и каждого возвращаемой переменной. 
* **sFullName_SubjectOrgan_** - textArea - Полное наименование
* **sShortName_SubjectOrgan_** - string - Краткое наименование
* **sLocation_SubjectOrgan_** - textArea - Адрес регистрации
* **sCEOName_SubjectOrgan_** - string - ФИО руководителя
* **sID_SubjectActionKVED_SubjectOrgan_** - string - Основной КВЕД - доступен для редактирования как автокомплит
* **sNote_ID_SubjectActionKVED_SubjectOrgan_** - label - Полное наименование выбранного КВЕДа - автоматически обновится при изменении **sID_SubjectActionKVED_SubjectOrgan_**
* **sDateActual_SubjectOrgan_** - label - Дата получения данных из ЄДРПОУ
``` xml
        <activiti:formProperty id="markers_01" name="лінія ЄДРПОУ 01" type="markers" default="{&quot;attributes&quot;:{&quot;Line_01&quot;:{&quot;aElement_ID&quot;:[&quot;sID_SubjectOrgan_OKPO_01&quot;],&quot;sValue&quot;:&quot;Дані з ЄДРПОУ - 01&quot;}}}"></activiti:formProperty>
        <activiti:formProperty id="sID_SubjectOrgan_OKPO_01" name="Введіть код ЄДРПОУ" type="string" required="true"></activiti:formProperty>
        <activiti:formProperty id="sFullName_SubjectOrgan_01" name="Найменування повне" type="textArea" required="true"></activiti:formProperty>
        <activiti:formProperty id="sShortName_SubjectOrgan_01" name="Найменування скорочене" type="string" required="true"></activiti:formProperty>
        <activiti:formProperty id="sLocation_SubjectOrgan_01" name="Адреса" type="textArea" required="true"></activiti:formProperty>
        <activiti:formProperty id="sCEOName_SubjectOrgan_01" name="ПІБ керівника" type="string" required="true"></activiti:formProperty>
        <activiti:formProperty id="sID_SubjectActionKVED_SubjectOrgan_01" name="Основний КВЕД" type="string" required="true"></activiti:formProperty>
        <activiti:formProperty id="sNote_ID_SubjectActionKVED_SubjectOrgan_01" name="" type="label" default=" "></activiti:formProperty>
        <activiti:formProperty id="sDateActual_SubjectOrgan_01" name="Дата отримання інформації з ЄДРПОУ" type="label" default=" "></activiti:formProperty>
```
Приведенные переменные не обязательны для вывода, использование и порядок вывода - произвольные. Однако желательно придерживаться общего дизайна. Для единообразного использования данного виртуального "блока данных" установлен дизайн, который обязательно начинается с разделительной линии содержащей общее наименование идущих за ним данных.
Полный блок выглядит на форме следующим образом.

**Незаполненный**
![Незаполенный](https://goo.gl/dCRcRQ)

**Заполненный**
![Заполненный](https://goo.gl/UtPH43)

***

