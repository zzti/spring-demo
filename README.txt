文件结构：

├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   ├── MyConfiguration.java                                        ## 配置类，功能与xml相同
    │   │   ├── bean
    │   │   │   ├── MyBeanImplementsInitializingBeanAndDisposableBean.java  ## 实现InitializingBean和DisposableBean接口
    │   │   │   ├── MyBeanWithInitMethodAndDestroyMethod.java               ## 在@Bean注解中指定参数
    │   │   │   ├── MyBeanWithManyMethod.java                               ## 多种方式同时使用
    │   │   │   └── MyBeanWithPostConstructAndPreDestroy.java               ## 利用JSR250规范中的注解
    │   │   └── processor
    │   │       └── MyBeanPostProcessor.java                                ## 自定义后置处理器
    │   └── resources
    └── test
        └── java
            └── MyTest.java                                                 ## 测试类