package processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:09
 */

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    private static final String NAME = MyBeanPostProcessor.class.getSimpleName();

    public MyBeanPostProcessor() {
        System.out.print(NAME + ": ");
        System.out.println("constructor");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.print(NAME + ": ");
        System.out.println("BeanPostProcessor#postProcessBeforeInitialization");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.print(NAME + ": ");
        System.out.println("BeanPostProcessor#postProcessAfterInitialization");
        return bean;
    }
}
