package com.yhp.studybbs;

import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.services.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ArticleTest {
    @Autowired
    private ArticleService articleService;
    @Test
    void testDeleteComment() {
        final int index = 3;
        final UserEntity user = new UserEntity() {{setEmail("ghkdqh853@gmail.com");
        }};
        System.out.println("index : ");
    }
}
