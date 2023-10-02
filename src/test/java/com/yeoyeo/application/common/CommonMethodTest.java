package com.yeoyeo.application.common;

import com.yeoyeo.application.common.method.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommonMethodTest {

    @Autowired
    CommonMethod commonMethod;

    @Test
    public void redisTest() {
        commonMethod.setCache("test", "testValue");
        String testValue = commonMethod.getCache("test");

        log.info("testValue : {}", testValue);
        assertThat(testValue).isEqualTo("testValue");
    }

}
