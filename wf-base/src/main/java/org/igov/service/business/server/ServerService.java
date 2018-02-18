package org.igov.service.business.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.igov.io.GeneralConfig;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author idenysenko
 */
@Component("serverService")
public class ServerService {

    private static final Logger LOG = LoggerFactory.getLogger(ServerService.class);

    @Autowired private ServerDao oServerDao;
    @Autowired private GeneralConfig generalConfig;

    private final LoadingCache<Long, Server> serverCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, Server>() {
                @Override
                public Server load(Long nID) throws Exception {
                    return oServerDao.findById(nID).orNull();
                }
            });

    /**
     * Создание/редактирование записи Server. Проверяем по уникальному полю sID существует ли запись, если нет - создаем
     *
     * @param sID уникальное поле название сервера
     * @param sType тип (Region/Central)
     * @param sURL_Alpha
     * @param sURL_Beta
     * @param sURL_Omega
     * @param sURL
     * @return новую или отредактированную сущность
     */
    public Server setServer(String sID, String sType, String sURL_Alpha, String sURL_Beta,
            String sURL_Omega, String sURL) {
        LOG.info("setServer started with: sID={}, sType={}, sURL_Alpha={}, sURL_Beta={}, sURL_Omega={}, sURL={}",
                sID, sType, sURL_Alpha, sURL_Beta, sURL_Omega, sURL);

        Server oServer = oServerDao.findBy("sID", sID).orNull();
        //если не нашли существующей сущности (sID - уникальное поле) создаем новую
        if (oServer == null) {
            oServer = new Server();
            oServer.setsID(sID);
        }
        oServer.setsType(sType);
        oServer.setsURL(sURL);
        oServer.setsURL_Alpha(sURL_Alpha);
        oServer.setsURL_Beta(sURL_Beta);
        oServer.setsURL_Omega(sURL_Omega);

        oServerDao.saveOrUpdate(oServer);
        LOG.info("oServer={}", oServer);

        return oServer;
    }

    /**
     * Получение Server по ид.
     *
     * @param nID - ид
     * @return сущность с заданым ид
     * @throws java.util.concurrent.ExecutionException ошибка в кеше (в частности, если запись не найдена)
     */
    public Server getServer(Integer nID) throws ExecutionException {

        nID = generalConfig.getServerId(nID);

        Server oServer = null;
        try {
            oServer = serverCache.get(Long.valueOf(nID));
        } catch (ExecutionException ex) {
            LOG.error("Error during searching Server {}", ex);
            throw new ExecutionException(ex);
        }
        LOG.info("oServer={}", oServer);

        return oServer;
    }
}
