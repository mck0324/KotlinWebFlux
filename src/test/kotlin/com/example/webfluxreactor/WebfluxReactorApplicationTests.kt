package com.example.webfluxreactor

import com.example.webfluxreactor.model.Article
import com.example.webfluxreactor.repository.ArticleRepository
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

private val logger = KotlinLogging.logger {  }
@SpringBootTest
class WebfluxReactorApplicationTests(
    @Autowired private val repository: ArticleRepository,
) {

    @Test
    fun contextLoads() {
        val prevCount = repository.count().block() ?: 0
        repository.save(Article(title = "title")).block()
        val articles = repository.findAll().collectList().block()
        articles?.forEach { logger.debug { it } }

        val currCount = repository.count().block()
        Assertions.assertEquals(prevCount + 1,currCount)
    }

}
