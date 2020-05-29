import bean.MyBeanImplementsInitializingBeanAndDisposableBean;
import bean.MyBeanWithInitMethodAndDestroyMethod;
import bean.MyBeanWithManyMethod;
import bean.MyBeanWithPostConstructAndPreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaoxi666
 * @date 2020-05-28 18:09
 */
@ComponentScan("processor")
@Configuration
public class MyConfiguration {

    @Bean(initMethod = "myInitMethod", destroyMethod = "myDestroyMethod")
    public MyBeanWithInitMethodAndDestroyMethod myBeanWithInitMethodAndDestroyMethod() {
        return new bean.MyBeanWithInitMethodAndDestroyMethod();
    }

    @Bean
    public MyBeanImplementsInitializingBeanAndDisposableBean myBeanImplementsInitializingBean() {
        return new bean.MyBeanImplementsInitializingBeanAndDisposableBean();
    }

    @Bean
    public MyBeanWithPostConstructAndPreDestroy myBeanWithPostConstructAndPreDestroy() {
        return new MyBeanWithPostConstructAndPreDestroy();
    }

    @Bean(initMethod = "myInitMethod", destroyMethod = "myDestroyMethod")
    public MyBeanWithManyMethod myBeanWithManyMethod() {
        return new MyBeanWithManyMethod();
    }

}
