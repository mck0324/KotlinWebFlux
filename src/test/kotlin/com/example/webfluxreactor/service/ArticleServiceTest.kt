package com.example.webfluxreactor.service

import com.example.webfluxreactor.repository.ArticleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ArticleServiceTest(
    @Autowired private val service: ArticleService,
    @Autowired private val repository: ArticleRepository,
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

    }

    @Test
    fun getAll() {
        service.create(ReqCreate("title1",body = "body1")).block()!!
        service.create(ReqCreate("title2",body = "body2")).block()!!
        service.create(ReqCreate("title matched",body = "body3")).block()!!
        assertEquals(3,service.getAll().collectList().block()!!.size)

        assertEquals(1,service.getAll("matched").collectList().block()!!.size )
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