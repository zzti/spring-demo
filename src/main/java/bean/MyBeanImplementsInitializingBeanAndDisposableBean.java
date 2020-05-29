package bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:09
 */

public class MyBeanImplementsInitializingBeanAndDisposableBean implements InitializingBean, DisposableBean {

    private static final String NAME = MyBeanImplementsInitializingBeanAndDisposableBean.class.getSimpleName();

    public MyBeanImplementsInitializingBeanAndDisposableBean() {
        System.out.print(NAME + ": ");
        System.out.println("constructor");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.print(NAME + ": ");
        System.out.println("InitializingBean#afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        System.out.print(NAME + ": ");
        System.out.println("DisposableBean#destroy");
    }
}
