package application.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author xiaoxi666
 * @date 2022-08-18 20:50
 */
@Configuration
//@EnableWebMvc // 使用它的话，后面两个注解就不用配置了
@AutoConfigureOrder
@AutoConfigureAfter({WebMvcAutoConfiguration.class})
public class SpringMvcConfig implements WebMvcConfigurer {

    /**
     * jetty配置
     *
     * @return
     */
    @Bean
    public JettyServerCustomizer jettyServerCustomizer() {
        return server -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            // 默认最大线程连接数200
            threadPool.setMaxThreads(200);
            // 默认最小线程连接数8
            threadPool.setMinThreads(8);
            // 默认线程最大空闲时间100000ms
            threadPool.setIdleTimeout(100000);
        };
    }

    /**
     * jsp视图解析
     *
     * @return
     */
    @Bean
    public ViewResolver getViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/view/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    /**
     * 静态资源处理
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    /**
     * json消息转换器
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter(objectMapper);
        jackson.setSupportedMediaTypes(Lists.newArrayList(MediaType.APPLICATION_JSON_UTF8));

        converters.add(0, new StringHttpMessageConverter(Charsets.UTF_8));
        converters.add(1, jackson);
    }
}
