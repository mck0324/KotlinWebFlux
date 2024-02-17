package com.example.webfluxreactor.service

import com.example.webfluxreactor.exception.NoArticleException
import com.example.webfluxreactor.model.Article
import com.example.webfluxreactor.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class ArticleService(
    @Autowired private val repository: ArticleRepository,
) {

    @Transactional
    fun create(request: ReqCreate): Mono<Article> {
        return repository.save(request.toArticle())
    }

    fun get(id: Long): Mono<Article> {
        return repository.findById(id)
            .switchIfEmpty { throw NoArticleException("id: $id") }
    }

    fun getAll(title: String? = null): Flux<Article> {
        return if (title.isNullOrEmpty()) repository.findAll() else repository.findAllByTitleContains(title)
    }

    fun update(id:Long, request: ReqUpdate): Mono<Article> {
        return repository.findById(id)
            .switchIfEmpty { throw NoArticleException("id: $id") }
            .flatMap { article ->
                request.title?.let { article.title = it }
                request.body?.let { article.body = it }
                request.authorId?.let { article.authorId = it }
                repository.save(article)
            }
    }

    fun delete(id:Long): Mono<Void> {
        return repository.deleteById(id)
    }
}

data class ReqUpdate(
    val title: String,
    var body: String?= null,
    var authorId: Long?= null,
)

data class ReqCreate(
    val title: String,
    var body: String? = null,
    var authorId: Long? = null,
) {
    fun toArticle(): Article {
        return Article(
            title = this.title,
            body = this.body,
            authorId = this.authorId
        )
    }
}