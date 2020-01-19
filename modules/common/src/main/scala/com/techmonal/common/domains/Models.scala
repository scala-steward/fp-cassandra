package com.techmonal.common.domains

final case class Note(id: String, title: String, content: String, authorId: String, isActive: Boolean)

final case class Author(id: String, name: String, email: String)

final case class Request(id: String, action: String, requestTime: Long, responseTime: Long, status: Int)
