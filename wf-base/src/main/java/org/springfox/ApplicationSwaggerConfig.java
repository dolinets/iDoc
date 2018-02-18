package org.springfox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.common.base.Predicate;

import springfox.documentation.RequestHandler;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import io.swagger.annotations.Api;

import static springfox.documentation.builders.PathSelectors.*;
import static com.google.common.base.Predicates.*;

@Configuration
@EnableSwagger2
@EnableWebMvc
@PropertySource("classpath:springfox.properties")
public class ApplicationSwaggerConfig {
    // При изменении наименования группы, изменить файлы в src/main/asciidoc
    public static final String GROUP_COMMON = "common";
    public static final String GROUP_PROJECT = "project";
    
    @Bean
    public UiConfiguration uiConfig() {
	System.out.println(" ApplicationSwaggerConfig start");
	return UiConfiguration.DEFAULT;
    }

    private ApiInfo oApiInfoAlien() {
	return new ApiInfoBuilder().title("Activiti's API & Entity").description("REST-Сервисы и Сущности - Activiti")
		.termsOfServiceUrl("https://www.activiti.org/userguide/").build();
    }

    private ApiInfo oApiInfo() {
	return new ApiInfoBuilder().title("API & Entity").description("REST-Сервисы и Сущности проекта").version("1.0")
		.contact("start@igov.org.ua").termsOfServiceUrl("https://github.com/e-government-ua/i/wiki").build();
    }
    
    /*@Bean
    public Docket dockAccessAndAuth() {
	return new Docket(DocumentationType.SWAGGER_2).groupName("Авторизация и Доступ").apiInfo(apiInfo()).select()
		.paths(pathAccessAndAuth()).build();
    }
    private Predicate<String> pathAccessAndAuth() {
	return or(regex("/access.*"), regex("/auth.*"));
    }*/

    @Bean
    public Docket DocsAlien() {
	return new Docket(DocumentationType.SWAGGER_2).groupName(GROUP_COMMON).apiInfo(oApiInfoAlien()).select()
		.apis(oPredicateNotREST()).paths(oPredicatePathAlien()).build();
    }
    private Predicate<String> oPredicatePathAlien() {
	return not(regex("/org/igov.*"));
    }
    private Predicate<RequestHandler> oPredicateNotREST() {
	return and(not(RequestHandlerSelectors.withClassAnnotation(Api.class)),
		not(RequestHandlerSelectors.withClassAnnotation(ApiIgnore.class)),
		not(RequestHandlerSelectors.withMethodAnnotation(ApiIgnore.class)));
    }
    
    /*@Bean
    public Docket Docs_All() {
	return new Docket(DocumentationType.SWAGGER_2).apiInfo(oApiInfo()).select()
		.paths(PathSelectors.any()).build();
    }*/

    @Bean
    public Docket Docs() {
	return new Docket(DocumentationType.SWAGGER_2).groupName(GROUP_PROJECT).apiInfo(oApiInfo()).select()
		.apis(oPredicateNoIgnore()).paths(PathSelectors.any()).build();//.apis(oPredicateREST())
    }
    private Predicate<RequestHandler> oPredicateREST() {
	return and(RequestHandlerSelectors.withClassAnnotation(Api.class),
		not(RequestHandlerSelectors.withClassAnnotation(ApiIgnore.class)),
		not(RequestHandlerSelectors.withMethodAnnotation(ApiIgnore.class)));
    }
    private Predicate<RequestHandler> oPredicateNoIgnore() {
	return and(not(RequestHandlerSelectors.withClassAnnotation(ApiIgnore.class)),
		not(RequestHandlerSelectors.withMethodAnnotation(ApiIgnore.class)));
    }
}