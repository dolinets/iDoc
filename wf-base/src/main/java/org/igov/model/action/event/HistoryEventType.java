package org.igov.model.action.event;

public enum HistoryEventType {

    CUSTOM(0L,
            "custom",
            "Частный тип",
            ""),
    GET_SERVICE(1L,
            "getService",
            "Пользователь воспользовался услугой на портале",
            "Ви подали заявку на послугу " + HistoryEventMessage.SERVICE_NAME
                    + ". \n Cтатус: " + HistoryEventMessage.SERVICE_STATE),
    SET_DOCUMENT_INTERNAL(2L,
            "setDocument_internal",
            "В Мои документы пользователя загружен новый документ – через наш портал",
            HistoryEventMessage.ORGANIZATION_NAME + " завантажує " + HistoryEventMessage.DOCUMENT_TYPE
                    + " " + HistoryEventMessage.DOCUMENT_NAME + " у Ваш розділ Мої документи"),
    SET_DOCUMENT_EXTERNAL(3L,
            "setDocument_external",
            "В Мои документы пользователя загружен новый документ – внешняя организация",
            HistoryEventMessage.ORGANIZATION_NAME + " завантажує " + HistoryEventMessage.DOCUMENT_TYPE
                    + " " + HistoryEventMessage.DOCUMENT_NAME + " у Ваш розділ Мої документи"),
    SET_DOCUMENT_ACCESS_LINK(4L,
            "setDocumentAccessLink",
            "Пользователь предоставил доступ к своему документу",
            "Ви надаєте доступ до документу "
                    + HistoryEventMessage.DOCUMENT_TYPE + " " + HistoryEventMessage.DOCUMENT_NAME
                    + " іншій людині: " + HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")"
                    + " (телефон: " + HistoryEventMessage.TELEPHONE
                    + ", e-mail: " + HistoryEventMessage.EMAIL
                    + ", термiн дії: " + HistoryEventMessage.DAYS + " днів)"),
    SET_DOCUMENT_ACCESS(5L,
            "setDocumentAccess",
            "Кто-то воспользовался доступом к документу через OTP, который ему предоставил пользователь",
            "" + HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")"
                    + " скористався доступом, який Ви надали, та переглянув документ "
                    + HistoryEventMessage.DOCUMENT_TYPE + " " + HistoryEventMessage.DOCUMENT_NAME + ""),
    ACTIVITY_STATUS_NEW(6L,
            "ActivitiStatusNew",
            "Изменение статуса заявки",
            "Ваша заявка №" + HistoryEventMessage.TASK_NUMBER
                    + " змiнила свiй статус на " + HistoryEventMessage.SERVICE_STATE + ""),
    GET_DOCUMENT_ACCESS_BY_HANDLER(7L,
            "getDocumentAccessByHandler",
            "Кто-то воспользовался доступом к документу, который ему предоставил пользователь",
            "Організація " + HistoryEventMessage.ORGANIZATION_NAME
                    + " скористалась доступом, який Ви надали, та переглянула документ "
                    + HistoryEventMessage.DOCUMENT_TYPE + " " + HistoryEventMessage.DOCUMENT_NAME + ""),
    FINISH_SERVICE(8L,
            "ActivitiFinish",
            "Выполнение заявки",
            "Ваша заявка №" + HistoryEventMessage.TASK_NUMBER + " виконана"),
    SET_TASK_QUESTIONS(9L,
            "ActivitiFinish",
            "Запрос на уточнение данных",
            "По заявці №" + HistoryEventMessage.TASK_NUMBER + " задане прохання уточнення:\n"
                    + HistoryEventMessage.S_BODY + "\n"
                    + HistoryEventMessage.TABLE_BODY),
    SET_TASK_ANSWERS(10L,
            "ActivitiFinish",
            "Ответ на запрос об уточнении данных",
            "По заявці №" + HistoryEventMessage.TASK_NUMBER + " дана відповідь громадянином:\n"
                    + HistoryEventMessage.S_BODY + "\n"
                    + HistoryEventMessage.TABLE_BODY),
    CREATING_DOCUMENT(11L,
            "DocumentCreating",
            "Нажата кнопка \"створити документ\"",
            "Створення документу " + HistoryEventMessage.ORDER_ID),
    CREATE_DOCUMENT(12L,
            "DocumentCreated",
            "Нажата кнопка \"створити\"",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + "  - документ відредаговано автором"),
    SIGNE_DOCUMENT(13L,
            "DocumentSigned",
            "Нажата кнопка \"підписати\" или \"ознайомлен\"",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - документ завізовано"),
    CHANGE_DOCUMENT(14L,
            "DocumentChanged",
            "Внесены изменения в документ или задачу (Слушатель SetTask)",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - документ відредаговано:\n\n" +  
            "<table style=\"width:30%\"><tr><th>Було</th><th>Cтало</th></tr>" +         
            HistoryEventMessage.OLD_DATA + "\n" 
            + HistoryEventMessage.NEW_DATA + "</table>"),         
    CREATE_SUBDOCUMENT(15L,
            "SubDocumentCreated",
            "В текущем процессе вызван другой процесс (с помощью элемента БП callActiviti)",
            "Створено вкладений документ\n" + HistoryEventMessage.ORDER_ID 
            + "\n" + HistoryEventMessage.BP_ID),
    CHANGE_DOCUMENT_STEP(16L,
            "DocumentChangeStep",
            "Изменился статус документа (переход с одной юзертаски на другую)",
            "Статус документа змінено - " + HistoryEventMessage.PIP),
    MENTION_DOCUMENT(17L,
            "DocumentMentioned",
            "Документ упомянули в другом документе",
            "Документ" + HistoryEventMessage.ORDER_ID + "пов'язаний з іншим" 
            + HistoryEventMessage.LINKED_ORDER_ID),
    CLOSE_DOCUMENT(18L,
            "DocumentClosed",
            "Процесс дошел до конца и закрылся",
            "Документ перміщено до архіву"),    
    TASK_CANCELED(19L,
            "TaskCanceled",
            "Вы самосточтельно отменили заявку №" + HistoryEventMessage.TASK_NUMBER,
            "Ви самостійно скасували заявку"),
    TASK_REQUEST_DONE(20L,
            "TaskRequestDone",
            "Исполнитель отработал задачу",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання виконано: " + HistoryEventMessage.S_BODY),
    TASK_REQUEST_NOT_DONE(21L,
            "TaskRequestNotDone",
            "исполнитель отработал задачу",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання не виконано: " + HistoryEventMessage.S_BODY),
    TASK_REQUEST_NOT_ACTUAL(22L,
            "TaskRequestNotActual",
            "исполнитель отработал задачу",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання не актуальне: " + HistoryEventMessage.S_BODY),
    TASK_REQUEST_TRANSFERED(23L,
            "TaskRequestTransfered",
            "исполнитель попросил перенести срок",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - прохання перенести термін виконання завдання: " + HistoryEventMessage.NEW_DATA),
    TASK_TRANSFERED(24L,
            "TaskTransfered",
            "Контролирующий перенес срок исполнения",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - перенесення терміну виконання завдання: " + HistoryEventMessage.NEW_DATA),
    TASK_REJECTED(25L,
            "TaskRejected",
            "Контролирующий отклонил отчет",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - відхилено звіт про виконання завдання: " + HistoryEventMessage.S_BODY),
    TASK_DONE(26L,
            "TaskDone",
            "Контролирующий снял задание как выполненное",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання виконано"),
    TASK_NOT_DONE(27L,
            "TaskNotDone",
            "Контролирующий снял задание как выполненное",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання не виконано: " + HistoryEventMessage.S_BODY),
    TASK_NOT_ACTUAL(28L,
            "TaskNotActual",
            "Контролирующий снял задание как выполненное",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - завдання не актуальне"),
    RELATE_DOCUMENT(29L,
            "DocumentRelated",
            "Документ связан",
            "Створено вкладений документ " + HistoryEventMessage.LINKED_ORDER_ID + " " + HistoryEventMessage.BP_ID),
    RELATED_DOCUMENT(30L,
            "DocumentRelated",
            "Документ связан",
            "Створено вкладений документ до " + HistoryEventMessage.LINKED_ORDER_ID + " " + HistoryEventMessage.BP_ID),
    ADD_CHAT(31L,
            "DocumentAddChat",
            "Добавлено замечание",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Винесено зауваження: " + HistoryEventMessage.S_BODY),          
    EDIT_CHAT(32L,
            "DocumentEditChat",
            "Замечание отредактировано",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Відредаговано зауваження: " + HistoryEventMessage.S_BODY),            
    ADD_ACCEPTOR(33L,
            "DocumentAddWriter",
            "Добавлено подписанта",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.S_BODY + ")."
            + " До документу №" + HistoryEventMessage.ORDER_ID + " запрошено "
            + HistoryEventMessage.FIO + " у ролі: підписанта."),
    ADD_VIEWER(34L,
            "DocumentAddReader",
            "Добавлено наблюдателя",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.S_BODY + ")."
            + " До документу №" + HistoryEventMessage.ORDER_ID + " запрошено "
            + HistoryEventMessage.FIO + " у ролі: переглядача."),
    ADD_VISOR(35L,
            "DocumentAddViewer",
            "Добавлено ознакомителя",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.S_BODY + ")."
            + " До документу №" + HistoryEventMessage.ORDER_ID + " запрошено "
            + HistoryEventMessage.FIO + " у ролі: ознайомлюючого."),
    DELEGATE(36L,
            "DocumentDelegate",
            "Документ делегировано",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.S_BODY + ") "
            + "делеговано права на документ № " + HistoryEventMessage.ORDER_ID + " "
            + HistoryEventMessage.FIO),
    CANCEL_SIGN(37L,
            "DocumentSignCancel",
            "Подпись снята",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Знято підпис."),
    SIGN_NOT_NEED(38L,
            "DocumentSignNotNeed",
            "Подпись не нужна",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Підпис не потрібен."),
    DOCUMENT_REFUSE(39L,
            "DocumentRefuse",
            "Отказано",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Відмова по документу № " + HistoryEventMessage.ORDER_ID + "."),
    ADD_TASK(40L,
            "TaskCreating",
            "Добавлено задание",
            "Створено завдання на дату " + HistoryEventMessage.NEW_DATA + "."),
    SIGN_DOCUMENT(41L,
            "DocumentSign",
            "Нажата кнопка подписи",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ")" + " - документ " + HistoryEventMessage.S_BODY + "."),
    DELETE_CHAT(42L,
            "DocumentDeleteChat",
            "Замечание удалено",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Видалено зауваження!"),
    CREATE_TASK(43L,
            "CreateTask",
            "Задача создана",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Завдання створено " + HistoryEventMessage.NEW_DATA),
    EDIT_TASK(44L,
            "EditTask",
            "Задача отредактирована",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Завдання відредаговано " + HistoryEventMessage.NEW_DATA),
    DELEGATE_TASK(45L,
            "DelegateTask",
            "Задача делегирована",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.LOGIN + ") " + "Завдання делеговано на " + HistoryEventMessage.FIO),
    MASTERPASS_PAYMENT_SUCCESS(46L,
            "MasterPassPayment_Success",
            "Оплата проведена успешно",
            "Ваш платіж у сумі " + HistoryEventMessage.IPAY_AMOUNT + " грн проведено успішно"),
    MASTERPASS_PAYMENT_REJECT(47L,
            "MasterPassPayment_Reject",
            "Оплата отклонена",
            "Ваш платіж у сумі " + HistoryEventMessage.IPAY_AMOUNT +  " грн скасовано, кошти повернено на Ваш рахунок"),
    URGENT_DOCUMENT(48L,
            "UrgentDocumentMarked",
            "Документ помечен как срочный",
            HistoryEventMessage.FIO + "(" + HistoryEventMessage.S_BODY + ") — позначив документ як терміновий"), 
    UNURGENT_DOCUMENT(49L,
            "UnurgentDocumentMarked",
            "Документ помечен как несрочный",
            HistoryEventMessage.FIO + "(" + HistoryEventMessage.S_BODY + ") — позначив документ як нетерміновий"),
    DELETE_URGENT_DOCUMENT(50L,
            "DeleteUnurgentDocumentMarked",
            "С документа снята срочность",
            HistoryEventMessage.FIO + "(" + HistoryEventMessage.S_BODY + ") зняв терміновість з документа"),
    AUTO_URGENT_DOCUMENT(51L,
            "AutoUrgentDocument",
            "Документ стал срочным",
            "Документ був позначений як терміновий"),
    AUTO_SUBMIT_DOCUMENT(52L,
            "AutoSubmitDocument",
            "Документ автоматично перешел на следующий шаг",
            "Автоматичний перехід на наступний крок"),
    REMOVE_DOCUMENT_PARTICIPANT(53L,
            "RemoveDocumentStepSubject",
            "Учасника документа було видалено",
            HistoryEventMessage.PIP + "(" + HistoryEventMessage.S_BODY + "). Учасника документа №" 
                    + HistoryEventMessage.ORDER_ID + " " + HistoryEventMessage.FIO + " було видалено");       
    
    private Long nID;
    private String sID;
    private String sName;
    private String sTemplate;

    private HistoryEventType(Long nID, String sID, String sName, String sTemplate) {
        this.nID = nID;
        this.sID = sID;
        this.sName = sName;
        this.sTemplate = sTemplate;
    }

    public static HistoryEventType getById(Long id) {
        if (id != null) {
            for (HistoryEventType eventType : values()) {
                if (eventType.nID.equals(id)) {
                    return eventType;
                }
            }
        }
        return null;
    }

    public Long getnID() {
        return nID;
    }

    public String getsID() {
        return sID;
    }

    public String getsName() {
        return sName;
    }

    public String getsTemplate() {
        return sTemplate;
    }

}

