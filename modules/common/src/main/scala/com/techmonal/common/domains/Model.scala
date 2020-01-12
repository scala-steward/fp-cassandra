package com.techmonal.common.domains

import java.time.Instant
import java.util.UUID

final case class User(id: UUID, name: String, email: String, isActive: Boolean)

final case class Request(id: UUID, action: String, requestTime: Instant, responseTime: Instant, status: Int)
