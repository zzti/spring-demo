package bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:09
 */

public class MyBeanWithPostConstructAndPreDestroy {

    private static final String NAME = MyBeanWithPostConstructAndPreDestroy.class.getSimpleName();

    public MyBeanWithPostConstructAndPreDestroy() {
        System.out.print(NAME + ": ");
        System.out.println("constructor");
    }

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
