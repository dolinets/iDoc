package org.igov.service.business.launch;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.igov.io.web.HttpRequester;
import org.igov.model.action.launch.*;
import org.igov.model.action.vo.LaunchVO;
import org.igov.model.server.Server;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.business.util.CommonUtils;
import org.igov.util.JSON.JsonRestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Обертка запуска методов, с протоколированием, и возможностью повторного
 * запуска. А так-же отложенного запуска (через очередь).
 *
 * @author Belyavtsev Vladimir Vladimirovich (bw), перенес idenysenko
 */
@Service
public class LaunchService {

    private static final Logger LOG = LoggerFactory.getLogger(LaunchService.class);
    private static ExecutorService execution = Executors.newCachedThreadPool();

    @Autowired private LaunchOldDao oLaunchOldDao;
    @Autowired private LaunchDao oLaunchDao;
    @Autowired private ProcessLinkService oProcessLinkService;
    @Autowired private HttpRequester oHttpRequester;

    /**
     * Запустить метод. Для работы необходимо, чтобы бин сервиса у которого нужно вызвать метод был в LaunchService, а
     * так же добавить условие в getBean(), который по классу отдаст бин на котором будет вызываться нужный метод.
     * @see #getBean(Class)
     * @param sLoginPrincipal логин инициатора запуска
     * @param sMethod название метода
     * @param aClass сигнатура метода
     * @param aoObject аргументы метода
     * @param oClass класс у которого нужно будет вызывать метод
     * @param oServer сервер на котором должен выполниться метод
     */
    public void start(String sLoginPrincipal, String sMethod, Class[] aClass, Object[] aoObject, Class<?> oClass, Server oServer) {
        if (sLoginPrincipal == null) {
            sLoginPrincipal = "system";
        }
        //вернется пустой обьект, если будет ексепшен
        Launch oLaunch = doProtocolInsert(sLoginPrincipal, sMethod, aClass, aoObject, oClass, oServer);
        //выполняем метод в отдельном треде
        execution.submit(() -> invokeMethodAndUpdateProtocol(oLaunch));
    }

    /**
     * Вставка новой записи в протокол, перед попыткой запуска
     *
     *
     * @param sLoginPrincipal логин инициатора запуска
     * @param sMethod название метода
     * @param aClass  сигнатура метода
     * @param aoObject аргументы метода
     * @param oClass  класс у которого нужно будет вызывать метод
     */
    private Launch doProtocolInsert(String sLoginPrincipal, String sMethod, Class[] aClass, Object[] aoObject, Class<?> oClass, Server oServer) {
        LOG.info("doProtocolInsert start...");
        //обьект для сериализации аргументов для вызова метода oProduce
        LaunchVO oLaunchVO = new LaunchVO();
        oLaunchVO.setaClass(aClass);
        oLaunchVO.setaObject(aoObject);
        oLaunchVO.setoClass(oClass);
        //инициализация протокола
        Launch oLaunch = new Launch();
        oLaunch.setsMethod(sMethod);
        try {
            oLaunch.setsData(JsonRestUtils.toJson(oLaunchVO));
        } catch (JsonProcessingException oException) {
            LOG.error("[doProtocolInsert] error, during data serialization! {}", oException);
        }
        oLaunch.setsLoginPrincipal(sLoginPrincipal);
        oLaunch.setoLaunchStatus(LaunchStatus.None);
        oLaunch.setsDateEdit(DateTime.now());
        oLaunch.setoServer(oServer);
        oLaunchDao.saveOrUpdate(oLaunch);
        LOG.info("Save object oLaunch={}", oLaunch);

        return oLaunch;
    }

    /**
     * Обновление записи в протоколе, и перемещение ее в архив, при успешном
     * результате запуска
     *  @param oStatus статус
     * @param oReturnData ответ метода
     * @param oLaunch сущность протокола, для обновления
     */
    private void doProtocolUpdate(LaunchStatus oStatus, Object oReturnData, Launch oLaunch) {
        LOG.info("doProtocolUpdate start, status={}", oStatus);
        boolean bMoveToOld = oStatus == LaunchStatus.Success;
        oLaunch.setoLaunchStatus(oStatus);
        oLaunch.setnTry(oLaunch.getnTry() + 1);
        try {
            oLaunch.setsDataReturn(JsonRestUtils.toJson(oReturnData));
        }  catch (JsonProcessingException oException) {
            LOG.error("[doProtocolUpdate] error, during data serialization! {}", oException);
        }
        oLaunchDao.saveOrUpdate(oLaunch);
        LOG.info("Entity {} updated.", oLaunch.getId());
        if (bMoveToOld) {
            LaunchOld oLaunchOld = new LaunchOld();
            oLaunchOld.setnID_LaunchGroup(Math.toIntExact(oLaunch.getId()));
            oLaunchOld.setoLaunchStatus(oLaunch.getoLaunchStatus());
            oLaunchOld.setnID_LaunchGroup(oLaunch.getnID_LaunchGroup());
            oLaunchOld.setsDateEdit(DateTime.now());
            oLaunchOld.setnTry(oLaunch.getnTry());
            oLaunchOld.setsData(oLaunch.getsData());
            oLaunchOld.setsDataReturn(oLaunch.getsDataReturn());
            oLaunchOld.setsMethod(oLaunch.getsMethod());
            oLaunchOld.setsReturn(oLaunch.getsReturn());
            oLaunchOld.setoServer(oLaunch.getoServer());
            oLaunchOld.setoClient(oLaunch.getoClient());
            oLaunchOld.setsLoginPrincipal(oLaunch.getsLoginPrincipal());
            oLaunchOldDao.saveOrUpdate(oLaunchOld);
            LOG.info("Entity {} moved to old.", oLaunchOld.getId());
            //удаляем сущность из таблцы Launch, которую перенесли в LaunchOld
            oLaunchDao.delete(oLaunch);
        }
    }

    /**
     * Вызов метода, который протоколируем
     *
     * @param sMethod название метода
     * @param oClass класс у которого нужно будет вызывать метод
     * @param aClass сигнатура метода
     * @param aoObject аргументы метода
     *
     * @return результат вызова метода
     *
     * @throws NoSuchMethodException не найден метод у класса
     * @throws InvocationTargetException ошибка вызова метода у инстанса
     * @throws IllegalAccessException ошибка доступа
     */
    private Object oProduced(String sMethod, Class<?> oClass, Class[] aClass, Object[] aoObject)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method oMethod = oClass.getMethod(sMethod, aClass);
        return oMethod.invoke(getBean(oClass), aoObject);
    }

    /**
     * Фабрика - по классу отдает бин на которому будем вызывать метод.
     *
     * @param oClass класс для которого нужен бин, для вызова метода
     * @return бин для oClass
     */
    private Object getBean(Class<?> oClass) {
        Object oResult = null;
        if (oClass.isInstance(oProcessLinkService)) {
            oResult = oProcessLinkService;
        } else if (oClass.isInstance(oHttpRequester)) {
            oResult = oHttpRequester;
        }
        if (oResult == null) {
            throw new IllegalArgumentException("There is no such bean for method invocation.");
        }
        return  oResult;
    }

    /**
     * Обработать все сущности протокола у которых кол-вл попыток запуска <= nTry
     *
     * @param nTry кол-во попыток запуска
     */
    public void processLaunchers(Integer nTry, Long nID_Server, String sDateFrom, String sDateTo) {
        LOG.info("processLaunchers start...");
        List<Launch> aoLaunch = oLaunchDao.findByFilter(nTry, nID_Server, sDateFrom, sDateTo);
        LOG.info("Launchers to process count {}", aoLaunch.size());
        for (Launch oLaunch : aoLaunch) {
            invokeMethodAndUpdateProtocol(oLaunch);
        }
    }

    /**
     * Вызвать метод и обновить протокол в зависимости от результата.
     *
     * @param oLaunch сущность протокола
     */
    private void invokeMethodAndUpdateProtocol(Launch oLaunch) {
        try {
            LaunchVO oLaunchVO = JsonRestUtils.readObject(oLaunch.getsData(), LaunchVO.class);

            Object oReturnData = oProduced(oLaunch.getsMethod(), oLaunchVO.getoClass(), oLaunchVO.getaClass(), oLaunchVO.getaObject());
            doProtocolUpdate(LaunchStatus.Success, oReturnData, oLaunch);
        } catch (Exception oException) {
            LOG.warn("Method call error: {}", oException);
            doProtocolUpdate(LaunchStatus.Error, oException.toString(), oLaunch);
        }
    }

    /*
     * Группа для класса Launch
     */
    public enum LaunchGroup {

        Default //Дефолтная группа (когда не входит ни в какую другую группу (в базе = null))
        , QueuedUnprotocolized //
        , Sheduled_GoogleAPI //
        , QueuedProtocolized_PD //
        //Предварительно записав запрос в протокол кладем ид ланчера в JMS
    }


    public boolean bProtocolize(String sMethod) {
        return true;
        //return MethodGroup.Protocolize.bHas(sMethod);
    }

    public String sLoginPrincipal(Map mParam) {
        return CommonUtils.sO(mParam.get("loginPrincipal"));
    }

    public String sMethod(Map mParam) {
        return CommonUtils.sO(mParam.get("nameMethod"));
    }

    public void reduceParameters(Map mParam) throws Exception {
    }

    public void doProduceQueued(Map mParam) throws Exception {
        //No JMS yet
        /*Param.remove("timingExecuteAction");
        JSONObject oParams = new JSONObject();
        oParams.putAll(mParam);
        new Sender().setDestinationName(Config.getEnvironmentValue("queueNameESC"))
                .sendToQueue(oParams.toString(), null);*/
    }

    public void doProduceQueuedProtocolized() throws Exception {
        //No JMS yet
        /*String sCase = "doProduceQueuedProtocolized";
        HashMap mParam = new HashMap();
        mParam.put("nameMethod", "launch");
        mParam.put("ID", String.valueOf(oThis().nID()));
        JSONObject oParams = new JSONObject();
        oParams.putAll(mParam);
        Sender sender = new Sender();
        LOG.info("[" + sCase + "](oParams=" + oParams + "):...");
        if (oThis().nID_Group() == LaunchGroup.QueuedProtocolized_PD.ordinal()) {
            sender.setDestinationName(Config.getEnvironmentValue("queueNameESC_Pdoc"));
        } else {
            sender.setDestinationName(Config.getEnvironmentValue("queueNameESC"));
        }
        sender.sendToQueue(oParams.toString(), null);*/
    }

    /*
    public void doPrepare() throws Exception {
        _Delay(bDelay
                || oThis().nID_Group() == LaunchGroup.Sheduled_GoogleAPI.ordinal());
        _Queue(bQueue
                || oThis().nID_Group() == LaunchGroup.QueuedUnprotocolized.ordinal()
                || oThis().nID_Group() == LaunchGroup.QueuedProtocolized_PD.ordinal());
        _Protocolize(bDelay || bProtocolize(oThis().sMethod)
                || oThis().nID_Group() == LaunchGroup.QueuedProtocolized_PD.ordinal());
    }*/

    public boolean bLogErrorOnly() throws Exception {
        return false;
    }

    /**
     * Установить добавочный фильтр по группе
     *
     * @param oGroupsLaunch
     * @return this
     */
    /*public LaunchService _FilterAddGroup(LaunchGroup oGroupsLaunch) {
        _FilterAddGroup(oGroupsLaunch.ordinal());
        return this;
    }*/

    /**
     * Установить группу в протоколе
     *
     * @param oGroupsLaunch
     * @return this
     */
    public LaunchService _Group(LaunchGroup oGroupsLaunch) {
        LOG.debug("oGroupsLaunch: " + oGroupsLaunch);
        if (oGroupsLaunch != null) {
            //oThis()._Group(oGroupsLaunch.ordinal());
        }
        return this;
    }

    //Вань, это на ДАО перевести нужно
    public void removeOld() {
        String sCase = "removeOld";
        LOG.info("[" + sCase + "]:...");

        int countUpdated = 1;
        while (countUpdated != 0) {
            List<LaunchOld> aoLaunchOld_ToDelete = oLaunchOldDao.findWithDateDiff(93);
            List<LaunchOld> aoLaunchOld_NotDeleted = oLaunchOldDao.delete(aoLaunchOld_ToDelete);
            countUpdated = aoLaunchOld_NotDeleted.size();
        }
        LOG.info("[" + sCase + "]:Ok!");
    }

}
