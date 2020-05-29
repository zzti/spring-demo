package bean;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:09
 */

public class MyBeanWithInitMethodAndDestroyMethod {

    private static final String NAME = MyBeanWithInitMethodAndDestroyMethod.class.getSimpleName();

    public MyBeanWithInitMethodAndDestroyMethod() {
        System.out.print(NAME + ": ");
        System.out.println("constructor");
    }

    public void myInitMethod() {
        System.out.print(NAME + ": ");
        System.out.println("@Bean#initMethod");
    }

    public void myDestroyMethod() {
        System.out.print(NAME + ": ");
        System.out.println("@Bean#destroyMethod");
    }
}
