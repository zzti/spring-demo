import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:16
 */
public class MyTest {
    @Test
    public void test_01() {
        AnnotationConfigApplicationContext applicationContext =
            new AnnotationConfigApplicationContext(MyConfiguration.class);
        System.out.println("容器创建完毕");

        applicationContext.close();
        System.out.println("容器销毁完毕");
    }
}
