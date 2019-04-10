package com.aaron.es.annotation;

import com.aaron.es.config.ElasticsearchConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @description: 启动类配置该注解，该注解有两个作用
 * 作用1：引入自动配置的restHighLevelClient
 * 作用2：配置entityPath以识别es entity自动创建索引以及mapping（如果不配置，则按照Main方法的路径包进行扫描）
 * @author: Aaron
 * @date 2019/4/2

 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@Import(ElasticsearchScannerRegistrar.class)
@Import(ElasticsearchConfiguration.class)
@ComponentScan("org.zxp.esclientrhl")
public @interface EnableESTools {

    /**
     * 配置repository包路径,如果不配置，则按照Main方法的路径包进行扫描
     * @return
     */
    String[] basePackages() default {};
    /**
     * 配置repository包路径,如果不配置，则按照Main方法的路径包进行扫描
     * @return
             */
    String[] value() default {};
    /**
     * entity路径配置,如果不配置，则按照Main方法的路径包进行扫描
     */
    String[] entityPath() default {};
}
