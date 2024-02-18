package com.example.webfluxreactor.service

import com.example.webfluxreactor.repository.ArticleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono

@SpringBootTest
class ArticleServiceTest(
    @Autowired private val service: ArticleService,
    @Autowired private val repository: ArticleRepository,
    @Autowired private val rxtx: TransactionalOperator,

) {

    @Test
    fun createAndGet() {
        val prevCnt = repository.count().block() ?: 0
        val article = service.create(ReqCreate("title1","blabla")).block()!!
        val currCnt = repository.count().block() ?: 0
        Assertions.assertEquals(prevCnt + 1, currCnt)
        val readArticle = service.get(article.id).block()!!
        Assertions.assertEquals(article.id, readArticle.id)
        Assertions.assertEquals(article.title, readArticle.title)
        Assertions.assertEquals(article.body, readArticle.body)
        Assertions.assertEquals(article.authorId, readArticle.authorId)
        assertNotNull(readArticle.createdAt)
        assertNotNull(readArticle.updatedAt)

    }

    @Test
    fun getAll() {
        rxtx.execute { tx ->
            tx.setRollbackOnly()
            Mono.zip(
                service.create(ReqCreate("title1",body = "body1")),
                service.create(ReqCreate("title2",body = "body2")),
                service.create(ReqCreate("title matched",body = "body3")),
            ).flatMap {
                service.getAll().collectList().doOnNext {
                    assertEquals(3, it.size)
                }
            }.flatMap {
                service.getAll("matched").collectList().doOnNext {
                    assertEquals(1,it.size)
                }
            }
        }.subscribe()
    }

    @Test
    fun update() {
        val new = service.create(ReqCreate("title1","body2", 1234)).block()!!
        val request = ReqUpdate(
            title = "updated!!",
            body =  "updated body!!!"
        )
        service.update(new.id, request).block()
        service.get(new.id).block()!!.let { article ->
            assertEquals(request.title, article.title)
            assertEquals(request.body, article.body)
            assertEquals(new.authorId, article.authorId)
        }
    }

    @Test
    fun delete() {
        val preCount = repository.count().block() ?: 0
        val create = service.create(ReqCreate(
            "title1","body1"
        )).block()!!

        service.delete(create.id).block()
        val currCount = repository.count().block() ?:0
        Assertions.assertEquals(preCount,currCount)


    }
}