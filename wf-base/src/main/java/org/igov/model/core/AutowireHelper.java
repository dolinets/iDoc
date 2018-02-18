/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author iDoc-2
 */
public final class AutowireHelper implements ApplicationContextAware {

    private static final AutowireHelper INSTANCE = new AutowireHelper();
    private static ApplicationContext applicationContext;

    private AutowireHelper() {}

    /**
     * Tries to autowire the specified instance of the class if one of the
     * specified beans which need to be autowired are null.
     *
     * @param classToAutowire the instance of the class which holds @Autowire
     * annotations
     * @param beansToAutowireInClass the beans which have the @Autowire
     * annotation in the specified {#classToAutowire}
     */
    public static void autowire(Object classToAutowire, Object... beansToAutowireInClass) {
        for (Object bean : beansToAutowireInClass) {
            System.out.println("bean: " + bean);
            if (bean == null) {
                System.out.println("bean before autowire: " + bean);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(classToAutowire);
                System.out.println("bean: " + bean);
                return;
            }
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        AutowireHelper.applicationContext = applicationContext;
    }

    /**
     * @return the singleton instance.
     */
    public static AutowireHelper getInstance() {
        return INSTANCE;
    }

}
