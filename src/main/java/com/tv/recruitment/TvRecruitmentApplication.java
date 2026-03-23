package com.tv.recruitment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 电视招聘展示系统启动类
 */
@SpringBootApplication
@MapperScan("com.tv.recruitment.mapper")
public class TvRecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TvRecruitmentApplication.class, args);
        System.out.println("========================================");
        System.out.println("  电视招聘展示系统启动成功!");
        System.out.println("  API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}