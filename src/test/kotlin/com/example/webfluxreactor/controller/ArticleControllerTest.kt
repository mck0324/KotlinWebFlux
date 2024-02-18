package com.example.webfluxreactor.controller

import com.example.webfluxreactor.model.Article
import com.example.webfluxreactor.service.ReqCreate
import com.example.webfluxreactor.service.ReqUpdate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
class ArticleControllerTest(
    @Autowired private val context : ApplicationContext
) {

    val client = WebTestClient.bindToApplicationContext(context).build()

    @Test
    fun create() {
        val request = ReqCreate("title1","body1",1234)
        client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("title").isEqualTo(request.title)
            .jsonPath("body").isEqualTo(request.body!!)
            .jsonPath("authorId").isEqualTo(request.authorId!!)
    }

    @Test
    fun get() {
        val request = ReqCreate("title1","body1",1234)
        val created = client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
            .expectBody(Article::class.java).returnResult().responseBody!!
        val read = client.get().uri("/article/${created.id}").accept(APPLICATION_JSON).exchange()
            .expectStatus().isOk
            .expectBody(Article::class.java).returnResult().responseBody!!

        assertEquals(created.title,read.title)
        assertEquals(created.body,read.body)
        assertEquals(created.authorId,read.authorId)
        assertEquals(created.createdAt,read.createdAt)
        assertEquals(created.updatedAt,read.updatedAt)

    }

    @Test
    fun getAll() {
        repeat(5) {i ->
            val request = ReqCreate("title$i","body$i",i.toLong())
            client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
        }

        client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(ReqCreate("title matched","body",123)).exchange()

        val cnt = client.get().uri("/article/all").accept(APPLICATION_JSON).exchange()
            .expectStatus().isOk
            .expectBody(List::class.java)
            .returnResult().responseBody?.size ?: 0
        assertTrue(cnt > 0)

        client.get().uri("/article/all?title=matched").accept(APPLICATION_JSON).exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
    }

    @Test
    fun update() {
        val create = client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(ReqCreate("title1","body1",123))
            .exchange()
            .expectBody(Article::class.java).returnResult().responseBody!!
        client.put().uri("/article/${create.id}").accept(APPLICATION_JSON)
            .bodyValue(ReqUpdate(
                "title change"
            )).exchange().expectBody()
            .jsonPath("title").isEqualTo("title change")

    }

     @Test
     fun delete() {
         val create = client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(ReqCreate("title1","body1",1234))
             .exchange()
             .expectBody(Article::class.java).returnResult().responseBody!!
         val prevCnt = getArticleSize()
         client.delete().uri("/article/${create.id}").accept(APPLICATION_JSON).exchange()
         val currCnt = getArticleSize()
         assertEquals(prevCnt - 1,currCnt)
     }

    private fun getArticleSize(): Int {
        return client.get().uri("/article/all").accept(APPLICATION_JSON).exchange()
            .expectStatus().isOk
            .expectBody(List::class.java).returnResult().responseBody?.size ?: 0
    }
}