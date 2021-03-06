package org.igov.model.action.launch;

/**
 *Cтатусы запуска (итог запуса если он был)
 *
 * @author idenysenko
 */

public enum LaunchStatus {
    None //Без статуса/инициализировано (только создан пункт протокола, но еще небыло попытки запуска(возможно будет запущено через JMS))
    , Success //Успешно выполненно
    , Warning //Предупреждения по результатам запуска
    , Error //Не выполненно (ошибка)
    , Delay //"Отложено" выполнение (будет выполнено точечно при переборе очереди)
    , Null //Фэйковый статус "не найдено" (обычно при запросе записи протокола, которая уже удалена из таблиц)
}
