package com.example.webfluxreactor.repository

import com.example.webfluxreactor.model.Article
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ArticleRepository: R2dbcRepository<Article, Long> {

}