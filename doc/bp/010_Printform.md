* Принтформа прописывается на Юзертаске:

        activiti:formProperty id="PrintForm_1" name="File label;File title;pattern/print/UPSZN/subsidy_declaration_2.html" type="file"></activiti:formProperty

        activiti:formProperty id="sBody_1" name="[pattern/print/UPSZN/subsidy_zayava_1.html]" type="invisible" default="Заява" writable="false"></activiti:formProperty
 * ![10_0](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/10_0.JPG)

        
* При необходимости, сформированную принтформу можно отправить в письме как Attachment {PrintForm_1}

* Динамически содержимое принтформы можно изменять маркерами: [issue #816](https://github.com/e-government-ua/i/issues/816)

**отображение/скрытие  полей**

например, "all_table" -  id какого-либо элемента печатной формы


На юзертаск прописываем маркер:

`{"motion": {`

  `"ShowElementsOnTrue_1": {`

     `"aElement_ID": ["all_table"],`

    `"asID_Field": {`

    `"sCond": "condition"`

    `},`

    `"sCondition": "[sCond]=='1'"`

  `}`
 `}`
`}`
* ![9_0](https://github.com/e-government-ua/i/blob/test-alpha/doc/bp/img/9_0.JPG)


