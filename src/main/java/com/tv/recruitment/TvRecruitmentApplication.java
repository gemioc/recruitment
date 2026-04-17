package com.tv.recruitment;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 电视招聘展示系统启动类
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.tv.recruitment.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class TvRecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TvRecruitmentApplication.class, args);
        log.info("========================================");
        log.info("  电视招聘展示系统启动成功!");
        log.info("  API文档地址: http://localhost:8080/doc.html");
        log.info("========================================");
    }
}