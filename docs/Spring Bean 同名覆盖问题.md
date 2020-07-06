# Spring 同名 Bean 加载策略

## 前言

在平时的开发中，你是否遇到过以下几种场景：

- 不同的类声明了同一个bean名字，有时这两个类实现了同一个接口，有时是完全无关的两个类
- 多个同名bean，有的在xml中声明，有的以Java Config的方式声明
- xml中，既配了context:component-scan标签扫描bean，又通过bean标签声明了bean，而且两种方式都可以取到同一个bean
- Java Config中，既配了@ComponentScan注解扫描bean，又通过注解@Bean声明bean，而且两种方式都可以取到同一个bean
- xml和Java Config的方式混合使用，两种方式都可以取到同一个bean

那么问题来了，你清楚这几种场景下，Spring会分别执行什么策略吗？也即：最终取到的bean到底是哪一个？



既然有这么多种场景，那我们一一列举，看看到底是怎样的。

## 场景列举

开始前，先介绍一下环境：

- Spring Boot 2.1.0

我们用的启动类模板：

```java
package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 * 启动类模板程序，可根据需要添加不同的注解，以便引入对应的上下文。
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Applicationloader.class);
        // Spring Boot版本>=2.1.0时，默认不允许bean覆盖。我们为了研究bean覆盖机制，将它改成允许覆盖。
        application.setAllowBeanDefinitionOverriding(true);
        // 启动运行，并获取context
        ApplicationContext context = application.run(args);
        // 获取bean，并打印对应的实体类路径
        Object object = context.getBean("myBean");
        System.out.println(object.getClass().getName());
    }
}
```

好了，让我们开始吧。



### 场景1

两个同名bean，对应的两个实体类分别是同一个接口的不同实现。

```java
package beans;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:31
 */
public interface X {
}
```

```java
package beans;

import org.springframework.stereotype.Component;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:31
 */

@Component(value = "myBean")
public class XImpl1 implements X {
}
```

```java
package beans;

import org.springframework.stereotype.Component;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:31
 */

@Component(value = "myBean")
public class XImpl2 implements X {
}
```

```xml
文件名：applicationContext1.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.XImpl1"/>
</beans>
```

```xml
文件名：applicationContext2.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.XImpl2"/>
</beans>
```

```java
package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 * 启动类模板程序，可根据需要添加不同的注解，以便引入对应的上下文。
 */

@ImportResource({"classpath:applicationContext1.xml", "classpath:applicationContext2.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Applicationloader.class);
        // Spring Boot版本>=2.1.0时，默认不允许bean覆盖。我们为了研究bean覆盖机制，将它改成允许覆盖。
        application.setAllowBeanDefinitionOverriding(true);
        // 启动运行，并获取context
        ApplicationContext context = application.run(args);
        // 获取bean，并打印对应的实体类路径
        Object object = context.getBean("myBean");
        System.out.println(object.getClass().getName());
    }
}
```

执行结果：

```xml
beans.XImpl2
```

如果对调两个xml文件的顺序，也即：把

```java
@ImportResource({"classpath:applicationContext1.xml", "classpath:applicationContext2.xml"})
```

换成

```java
@ImportResource({"classpath:applicationContext2.xml", "classpath:applicationContext1.xml"})
```

执行结果就会变成：

```xml
beans.XImpl1
```



### 场景2

两个同名bean，对应的两个类完全没有关系。

```java
package beans;

import org.springframework.stereotype.Component;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:45
 */

@Component(value = "myBean")
public class Y {
}
```

```java
package beans;

import org.springframework.stereotype.Component;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:45
 */

@Component(value = "myBean")
public class Z {
}

```

```xml
文件名：applicationContext1.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.Y"/>

</beans>
```

```xml
文件名：applicationContext2.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.Z"/>

</beans>
```

执行结果与场景1类似。

因此我们可以知道：同名bean的覆盖，与具体的类组织方式没有关系。



###场景3

两个同名bean，均通过xml的bean标签声明。其实这就是上面的场景了。

可以看出，最终使用的是后面的xml中声明的bean。其实原因是“后面的xml中声明的bean”把“前面的xml中声明的bean”覆盖了。我们可以看到Bebug信息：

```xml
Overriding bean definition for bean 'myBean' with a different definition: replacing [Generic bean: class [beans.Z]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [applicationContext2.xml]] with [Generic bean: class [beans.Y]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [applicationContext1.xml]]
```

这段信息位于源码org.springframework.beans.factory.support.DefaultListableBeanFactory#registerBeanDefinition处：

```java
@Override
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
      throws BeanDefinitionStoreException {

   ...

   BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
   if (existingDefinition != null) {
      if (!isAllowBeanDefinitionOverriding()) {
         throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
      }
      else if (existingDefinition.getRole() < beanDefinition.getRole()) {
         ...
      }
      else if (!beanDefinition.equals(existingDefinition)) {
         if (logger.isDebugEnabled()) {
            logger.debug("Overriding bean definition for bean '" + beanName +
                  "' with a different definition: replacing [" + existingDefinition +
                  "] with [" + beanDefinition + "]");
         }
      }
      else {
         ...
      }
      this.beanDefinitionMap.put(beanName, beanDefinition);
   }
   else {
      ...
   }

   if (existingDefinition != null || containsSingleton(beanName)) {
      resetBeanDefinition(beanName);
   }
}
```

可以看出来，这里首先判断了allowBeanDefinitionOverriding属性，也即是否允许bean覆盖，如果允许的话，就继续判断role、beanDefinition等属性。当debug开启时，就会打印出上述的信息，告诉我们bean发生了覆盖行为。

如果我们把ApplicationLoader中的这行代码删除：

```java
application.setAllowBeanDefinitionOverriding(true);
```

由于Spring Boot 2.1.0及其以上版本默认不允许bean覆盖，此时会直接抛BeanDefinitionOverrideException异常，上面的源码也有体现。

如果是在Spring Boot 2.1.0以下，默认是允许覆盖的，但setAllowBeanDefinitionOverriding方法也不存在（它是2.1.0加入的，具体可以参见官方文档）。

那我们如果想设置该属性该怎么办呢？此时，我们可以参考Spring Boot2.1.0的实现org.springframework.boot.SpringApplication#prepareContext。通过方法addInitializers给SpringApplication注册ApplicationContextInitializer，并复写它的initialize方法，通过入参ConfigurableApplicationContext获取DefaultListableBeanFactory，再调用setAllowBeanDefinitionOverriding进行设置。示例：

自定义MyAplicationInitializer：

```java
package application;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xiaoxi666
 * @date 2020-07-06 21:52
 */
public class MyAplicationInitializer implements ApplicationContextInitializer {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory instanceof DefaultListableBeanFactory) {
            ((DefaultListableBeanFactory) beanFactory)
                .setAllowBeanDefinitionOverriding(false);
        }
    }
}
```

注册自定义的ApplicationInitializer：

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        application.addInitializers(new MyAplicationInitializer);
        ...
    }
}
```

这样就可以了。

另外，网上还提到重定义ContextLoader的方式，可以参考文末列出的第一篇文章。



### 场景4

两个同名bean，均通过JavaConfig的@Bean注解声明。

bean的定义不变，我们增加一个配置类，替换之前的xml配置文件：

```java
package configuration;

import beans.Y;
import beans.Z;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@Configuration
public class MyConfiguration {

    @Bean(name = "myBean")
    public Object y() {
        return new Y();
    }

    @Bean(name = "myBean")
    public Object z() {
        return new Z();
    }
}

```

启动类：

```java
package application;

import configuration.MyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@Import(MyConfiguration.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果：

```
beans.Y
```

如果把配置文件中Y和Z的顺序对调，也即将其改成这样：

```java
package configuration;

import beans.Y;
import beans.Z;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@Configuration
public class MyConfiguration {

    @Bean(name = "myBean")
    public Object z() {
        return new Z();
    }

    @Bean(name = "myBean")
    public Object y() {
        return new Y();
    }
}
```

执行j结果就会变成：

```
beans.Z
```

可以看出，最终使用的是位置靠前的bean。其实原因是“后面的bean”被忽略了，参考源码org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod：

```java
private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {
    ...
      
    // Has this effectively been overridden before (e.g. via XML)?
    if (isOverriddenByExistingDefinition(beanMethod, beanName)) {
       if (beanName.equals(beanMethod.getConfigurationClass().getBeanName())) {
          throw new BeanDefinitionStoreException(beanMethod.getConfigurationClass().getResource().getDescription(),
                beanName, "Bean name derived from @Bean method '" + beanMethod.getMetadata().getMethodName() +
                "' clashes with bean name for containing configuration class; please make those names unique!");
       }
       return;
    }
  
    ...
}
```

如果发现后加载的bean可以被overridden，就会将其忽略。因此最终使用的是先前被加载的bean。



### 场景5

两个同名bean，一个通过xml的bean标签声明，一个通过JavaConfig的@Bean注解声明。

我们通过xml声明Y：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.Y"/>

</beans>
```

通过JavaConfig声明Z：

```java
package configuration;

import beans.Z;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */


@Configuration
public class MyConfiguration {

    @Bean(name = "myBean")
    public Object z() {
        return new Z();
    }
}
```

启动类：

```java
package application;

import configuration.MyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@ImportResource({"classpath:applicationContext.xml"})
@Import(MyConfiguration.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果：

```
beans.Y
```

如果把

```
@ImportResource({"classpath:applicationContext.xml"})
@Import(MyConfiguration.class)
```

两者的上下位置对调一下，输出结果也不变。

因此可以得出结论：当xml和Java Config均采用注解引入时，最终拿到的bean是xml文件中声明的。原因是xml在Java Config之后加载，把Java Config声明的bean覆盖了。此时我们可以看到Debug信息：

```xml
 Overriding bean definition for bean 'myBean' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=configuration.MyConfiguration; factoryMethodName=z; initMethodName=null; destroyMethodName=(inferred); defined in configuration.MyConfiguration] with [Generic bean: class [beans.Y]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [applicationContext.xml]]
```

原理和场景3相同，不再赘述。



### 场景6

两个同名bean，均通过xml的context:component-scan标签扫描发现bean。

与场景2类似，我们新增一个applicationContext.xml文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">
    
    <context:component-scan base-package="beans"/>

</beans>
```

由于采用了扫描的方式，我们不用写两个xml文件分别声明两个bean了，现在一个applicationContext.xml文件就可以搞定。

启动类：

```java
package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 * 启动类模板程序，可根据需要添加不同的注解，以便引入对应的上下文。
 */

@ImportResource({"classpath:applicationContext.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果：

```xml
org.springframework.beans.factory.BeanDefinitionStoreException: Unexpected exception parsing XML document from class path resource [applicationContext.xml]; nested exception is org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Z] conflicts with existing, non-compatible bean definition of same name and class [beans.Y]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:419) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:336) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:304) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:188) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:224) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:195) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.lambda$loadBeanDefinitionsFromImportedResources$0(ConfigurationClassBeanDefinitionReader.java:358) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at java.util.LinkedHashMap.forEach(LinkedHashMap.java:684) ~[na:1.8.0_171]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitionsFromImportedResources(ConfigurationClassBeanDefinitionReader.java:325) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitionsForConfigurationClass(ConfigurationClassBeanDefinitionReader.java:144) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitions(ConfigurationClassBeanDefinitionReader.java:117) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:327) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:232) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:275) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:95) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:691) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:528) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:140) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at application.Applicationloader.main(Applicationloader.java:23) [classes/:na]
Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Z] conflicts with existing, non-compatible bean definition of same name and class [beans.Y]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.checkCandidate(ClassPathBeanDefinitionScanner.java:348) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.doScan(ClassPathBeanDefinitionScanner.java:286) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ComponentScanBeanDefinitionParser.parse(ComponentScanBeanDefinitionParser.java:90) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.NamespaceHandlerSupport.parse(NamespaceHandlerSupport.java:74) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1366) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1352) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:179) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.doRegisterBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:149) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.registerBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:96) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.registerBeanDefinitions(XmlBeanDefinitionReader.java:513) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:393) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	... 21 common frames omitted
```

可以看到抛了异常，异常信息告诉我们：发现了两个bean，但它们不兼容。抛异常的源码位于org.springframework.context.annotation.ClassPathBeanDefinitionScanner#checkCandidate：

```java
protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
   if (!this.registry.containsBeanDefinition(beanName)) {
      return true;
   }
   BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
   BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
   if (originatingDef != null) {
      existingDef = originatingDef;
   }
   if (isCompatible(beanDefinition, existingDef)) {
      return false;
   }
   throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
         "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
         "non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
}
```

这段代码执行时机很早（要知道我们现在是允许同名bean覆盖的，但显然可以看出，还没有走到判断allowBeanDefinitionOverriding属性的地方），扫描出来就检查候选bean，发现有两个同名bean，直接报冲突。



### 场景7

两个同名bean，均通过Java Config的注解@ComponentScan扫描发现bean。

与场景4类似，我们改一下配置文件：

```java
package configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@ComponentScan(basePackages = "beans")
@Configuration
public class MyConfiguration {
}
```

执行结果：

```xml
org.springframework.beans.factory.BeanDefinitionStoreException: Failed to process import candidates for configuration class [application.Applicationloader]; nested exception is org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Z] conflicts with existing, non-compatible bean definition of same name and class [beans.Y]
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:599) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:302) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:242) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:199) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:167) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:315) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:232) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:275) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:95) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:691) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:528) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:140) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at application.Applicationloader.main(Applicationloader.java:23) [classes/:na]
Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Z] conflicts with existing, non-compatible bean definition of same name and class [beans.Y]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.checkCandidate(ClassPathBeanDefinitionScanner.java:348) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.doScan(ClassPathBeanDefinitionScanner.java:286) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ComponentScanAnnotationParser.parse(ComponentScanAnnotationParser.java:132) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:287) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:242) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:589) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	... 15 common frames omitted
```

可以看到抛了异常，异常信息告诉我们：发现了两个bean，但它们不兼容。

同时，我们可以看到，场景6和7类似，抛的异常相同。但由于场景6是xml解析，场景7是Java Config解析，因此具体的堆栈信息有些差异。



### 场景8

两个同名bean，一个通过xml的context:component-scan标签扫描发现，一个通过Java Config的注解@ComponentScan扫描发现。

我们通过xml扫描Y：

```java
文件名：applicationContext.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <context:component-scan base-package="beans" use-default-filters="false">
        <context:include-filter type="assignable" expression="beans.Y"/>
    </context:component-scan>

</beans>
```

通过JavaConfig扫描Z：

```java
package configuration;

import beans.Z;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@ComponentScan(basePackages = "beans",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Z.class), useDefaultFilters = false)
@Configuration
public class MyConfiguration {
}
```

启动类：

```java
package application;

import configuration.MyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@ImportResource({"classpath:applicationContext.xml"})
@Import(MyConfiguration.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果：

```xml
org.springframework.beans.factory.BeanDefinitionStoreException: Unexpected exception parsing XML document from class path resource [applicationContext.xml]; nested exception is org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Y] conflicts with existing, non-compatible bean definition of same name and class [beans.Z]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:419) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:336) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:304) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:188) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:224) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanDefinitionReader.loadBeanDefinitions(AbstractBeanDefinitionReader.java:195) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.lambda$loadBeanDefinitionsFromImportedResources$0(ConfigurationClassBeanDefinitionReader.java:358) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at java.util.LinkedHashMap.forEach(LinkedHashMap.java:684) ~[na:1.8.0_171]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitionsFromImportedResources(ConfigurationClassBeanDefinitionReader.java:325) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitionsForConfigurationClass(ConfigurationClassBeanDefinitionReader.java:144) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.loadBeanDefinitions(ConfigurationClassBeanDefinitionReader.java:117) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:327) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:232) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:275) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:95) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:691) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:528) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:140) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-2.1.0.RELEASE.jar:2.1.0.RELEASE]
	at application.Applicationloader.main(Applicationloader.java:25) [classes/:na]
Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'myBean' for bean class [beans.Y] conflicts with existing, non-compatible bean definition of same name and class [beans.Z]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.checkCandidate(ClassPathBeanDefinitionScanner.java:348) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.doScan(ClassPathBeanDefinitionScanner.java:286) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.context.annotation.ComponentScanBeanDefinitionParser.parse(ComponentScanBeanDefinitionParser.java:90) ~[spring-context-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.NamespaceHandlerSupport.parse(NamespaceHandlerSupport.java:74) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1366) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1352) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:179) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.doRegisterBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:149) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.registerBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:96) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.registerBeanDefinitions(XmlBeanDefinitionReader.java:513) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	at org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:393) ~[spring-beans-5.1.2.RELEASE.jar:5.1.2.RELEASE]
	... 21 common frames omitted
```

发现抛异常，异常信息和场景6一致，都是在xml解析过程中抛的异常。

如果我们把

```java
@ImportResource({"classpath:applicationContext.xml"})
@Import(MyConfiguration.class)
```

两者的上下位置换一下：

```java
package application;

import configuration.MyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@Import(MyConfiguration.class)
@ImportResource({"classpath:applicationContext.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

再次执行。发现和上面抛的异常一致。

因此我们可以得出结论：当xml和Java Config都扫描bean时，注解@ComponentScan会先于xml标签中的context:component-scan标签执行（因为抛异常的点在解析后者的过程中，也可以调试源码得出相同的结论，参见下图）。

![Screen Shot 2020-07-07 at 12.06.08 AM](https://tva1.sinaimg.cn/large/007S8ZIlly1gghpbbqtbrj31b30u01kx.jpg)



### 场景9

两个同名bean，一个通过xml的bean标签声明，一个通过xml的context:component-scan标签扫描发现。

我们通过xml的bean标签声明Y，并通过xml的context:component-scan标签扫描发现Z：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.2.xsd">

    <bean id="myBean" class="beans.Y"/>
    
    <context:component-scan base-package="beans" use-default-filters="false">
        <context:include-filter type="assignable" expression="beans.Z"/>
    </context:component-scan>

</beans>
```

启动类：

```java
package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 * 启动类模板程序，可根据需要添加不同的注解，以便引入对应的上下文。
 */

@ImportResource({"classpath:applicationContext.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果：

```
beans.Y
```

如果我们通过xml的bean标签声明Z，并通过xml的context:component-scan标签扫描发现Y的话，执行结果就会是：

```
beans.Z
```

可以看出，最终使用的是通过xml的bean标签声明的bean，而非通过xml的context:component-scan标签扫描发现的bean。

我们跟踪源码会发现，在注册bean前，会在org.springframework.context.annotation.ClassPathBeanDefinitionScanner#checkCandidate方法中，判断两个bean是否兼容（第21行代码），如果兼容的话会返回false，bean就不会被注册了（注意：这里的解析顺序是先解析通过xml的bean标签声明的bean，后解析通过xml的context:component-scan标签扫描发现的bean，稍后解释）：

```java
/**
 * Check the given candidate's bean name, determining whether the corresponding
 * bean definition needs to be registered or conflicts with an existing definition.
 * @param beanName the suggested name for the bean
 * @param beanDefinition the corresponding bean definition
 * @return {@code true} if the bean can be registered as-is;
 * {@code false} if it should be skipped because there is an
 * existing, compatible bean definition for the specified name
 * @throws ConflictingBeanDefinitionException if an existing, incompatible
 * bean definition has been found for the specified name
 */
protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
   if (!this.registry.containsBeanDefinition(beanName)) {
      return true;
   }
   BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
   BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
   if (originatingDef != null) {
      existingDef = originatingDef;
   }
   if (isCompatible(beanDefinition, existingDef)) {
      return false;
   }
   throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
         "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
         "non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
}
```

具体地：org.springframework.context.annotation.ClassPathBeanDefinitionScanner#isCompatible

```java
/**
 * Determine whether the given new bean definition is compatible with
 * the given existing bean definition.
 * <p>The default implementation considers them as compatible when the existing
 * bean definition comes from the same source or from a non-scanning source.
 * @param newDefinition the new bean definition, originated from scanning
 * @param existingDefinition the existing bean definition, potentially an
 * explicitly defined one or a previously generated one from scanning
 * @return whether the definitions are considered as compatible, with the
 * new definition to be skipped in favor of the existing definition
 */
protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
   return (!(existingDefinition instanceof ScannedGenericBeanDefinition) ||  // explicitly registered overriding bean
         (newDefinition.getSource() != null && newDefinition.getSource().equals(existingDefinition.getSource())) ||  // scanned same file twice
         newDefinition.equals(existingDefinition));  // scanned equivalent class twice
}
```

我们知道，先前解析的bean是通过xml的bean标签声明的，因此existingDefinition的类型是org.springframework.beans.factory.support.GenericBeanDefinition，因此，条件

```
!(existingDefinition instanceof ScannedGenericBeanDefinition)
```

为true，也就表示兼容，因此该方法返回true。*附注：回顾一下场景6、7、8，它们就是在方法checkCandidate中抛了异常，因为这3个场景中的两个bean都是扫描发现的，因此existingDefinition的类型是ScannedGenericBeanDefinition，会被判定为不兼容。*

最终会导致通过xml的context:component-scan标签扫描发现的bean未被注册。因此我们最终使用的是通过xml的bean标签声明的bean。

前面留了个小尾巴：解析顺序是先解析通过xml的bean标签声明的bean，后解析通过xml的context:component-scan标签扫描发现的bean。源码位于org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseBeanDefinitions：

```java
/**
 * Parse the elements at the root level in the document:
 * "import", "alias", "bean".
 * @param root the DOM root element of the document
 */
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
   if (delegate.isDefaultNamespace(root)) {
      NodeList nl = root.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
         Node node = nl.item(i);
         if (node instanceof Element) {
            Element ele = (Element) node;
            if (delegate.isDefaultNamespace(ele)) {
               parseDefaultElement(ele, delegate);
            }
            else {
               delegate.parseCustomElement(ele);
            }
         }
      }
   }
   else {
      delegate.parseCustomElement(root);
   }
}
```

注意第14行和第17行。第14行用于解析默认命名空间的标签，第17行用于解析自定义命名空间的标签。bean标签属于默认命名空间，而component-scan属于自定义的命名空间。明显可以看出：先解析通过xml的bean标签声明的bean，后解析通过xml的context:component-scan标签扫描发现的bean。



### 场景10

两个同名bean，一个通过JavaConfig的@Bean注解声明，一个通过Java Config的注解@ComponentScan扫描发现。

我们通过JavaConfig的@Bean注解声明Y，并通过Java Config的注解@ComponentScan扫描发现Z：

```java
import beans.Z;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@ComponentScan(basePackages = "beans",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Z.class), useDefaultFilters = false)
@Configuration
public class MyConfiguration {

    @Bean(name = "myBean")
    public Object y() {
        return new Y();
    }
}
```

启动类：

```java
package application;

import configuration.MyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@Import(MyConfiguration.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        ...
    }
}
```

执行结果:

```
beans.Y
```

如果把Y和Z的声明方式对调一下，也即配置文件改成：

```java
package configuration;

import beans.Y;
import beans.Z;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @author xiaoxi666
 * @date 2020-07-06 11:02
 */

@ComponentScan(basePackages = "beans",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Y.class), useDefaultFilters = false)
@Configuration
public class MyConfiguration {

    @Bean(name = "myBean")
    public Object z() {
        return new Z();
    }
}
```

执行结果就是：

```
beans.Z
```

可以看出，最终使用的是通过注解@Bean声明的bean。通过源码可以看出，“通过注解@ComponentScan扫描的bean”被“通过注解@Bean声明的bean”覆盖了，源码位于org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#isOverriddenByExistingDefinition：

```java
protected boolean isOverriddenByExistingDefinition(BeanMethod beanMethod, String beanName) {
    ...
      
  	// A bean definition resulting from a component scan can be silently overridden
    // by an @Bean method, as of 4.2...
    if (existingBeanDef instanceof ScannedGenericBeanDefinition) {
       return false;
    }
  
    ...
}
```

上面的代码明确说明：通过component scan扫描的bean会被通过@Bean声明的bean覆盖掉，而且这种覆盖没有任何提示，也即silently（悄悄地）覆盖掉。



## 场景梳理

根据不同的维度，我们梳理一下上面的场景：

1. 根据实体类区分（这两种情况下的覆盖策略是相同的）
   1. 同一个接口的两个实现，对应的的两个bean同名。（**场景1**）
   2. 两个同名bean，对应的两个类完全没有关系（参见**场景2**）
2. 根据配置方式区分
   1. xml方式（**场景3**）
   2. Java config方式（**场景4**）
   3. xml和Java config方式混用（**场景5**：最终使用的是xml配置的bean）
3. 根据bean发现方式区分（通过@Bean注解声明的bean，会将@ComponentScan扫描的bean覆盖）
   1. xml的component-scan扫描方式（**场景6**）
   2. Java Config 的 @ComponentScan扫描方式（**场景7**）
   3. xml的component-scan扫描方式 和 Java Config的@ComponentScan扫描方式 混用（**场景8**）
   4. xml的bean标签方式（**场景3**）
   5. Java Config 的 @Bean注解声明bean（**场景4**）
   6. xml的component-scan扫描方式 和 bean标签方式混用（**场景9**）
   7. Java Config的@ComponentScan扫描方式 和 通过@Bean注解声明bean 混用（**场景10**）



## 总结

- 本文列举了平时开发中可能遇到的多种bean配置方式，并且简析了相关源码，解释了执行结果。
- 本文并未讲解bean解析整体流程，因此强烈建议读者手动调试，自己过一遍源码。
- 很多细节问题在方法源码注释标注了，这些内容在Spring的官方文档也有说明。建议抽空看一下官方文档，也许很多问题就迎刃而解了。



## 参考文章

1. 重定义ContextLoader，控制isAllowBeanDefinitionOverridng参数（提到了父子容器）：https://blog.csdn.net/zgmzyr/article/details/39380477