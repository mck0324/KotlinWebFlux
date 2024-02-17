package com.example.webfluxreactor.exception

import java.lang.RuntimeException


class NoArticleException(message: String?): RuntimeException(message) {
}