package bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author xiaoxi666
 * @date 2020-05-28 19:53
 */
public class MyBeanWithManyMethod implements InitializingBean, DisposableBean {
    private static final String NAME = MyBeanWithManyMethod.class.getSimpleName();

    public MyBeanWithManyMethod() {
        System.out.print(NAME + ": ");
        System.out.println("constructor");
    }

    /***************************@Bean中的方法*******************************/
    public void myInitMethod() {
        System.out.print(NAME + ": ");
        System.out.println("@Bean#initMethod");
    }

    public void myDestroyMethod() {
        System.out.print(NAME + ": ");
        System.out.println("@Bean#destroyMethod");
    }


    /****************InitializingBean和DisposableBean中的方法*****************/
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


    /****************@PostConstruct和@PreDestroy*****************/
    @PostConstruct
    public void myPostConstructMethod() throws Exception {
        System.out.print(NAME + ": ");
        System.out.println("@PostConstruct");
    }

    @PreDestroy
    public void myPreDestroyMethod() throws Exception {
        System.out.print(NAME + ": ");
        System.out.println("@PreDestroy");
    }
}
