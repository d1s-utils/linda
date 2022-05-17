/*
 * Copyright 2022 Linda project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.linda.controller.impl

import dev.d1s.linda.configuration.properties.SslConfigurationProperties
import dev.d1s.linda.controller.RedirectController
import dev.d1s.linda.entity.Redirect
import dev.d1s.linda.dto.redirect.RedirectAlterationDto
import dev.d1s.linda.dto.redirect.RedirectDto
import dev.d1s.linda.service.RedirectService
import dev.d1s.security.configuration.annotation.Secured
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.web.buildFromCurrentRequest
import dev.d1s.teabag.web.configureSsl
import dev.d1s.teabag.web.noContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RestController

@RestController
class RedirectControllerImpl : RedirectController {

    @Autowired
    private lateinit var redirectService: RedirectService

    @Autowired
    private lateinit var redirectAlterationDtoConverter: DtoConverter<RedirectAlterationDto, Redirect>

    @Autowired
    private lateinit var sslConfigurationProperties: SslConfigurationProperties

    @Secured
    override fun findAll(): ResponseEntity<Set<RedirectDto>> {
        val (_, redirects) = redirectService.findAll(true)

        return ok(redirects)
    }

    @Secured
    override fun findById(identifier: String): ResponseEntity<RedirectDto> {
        val (_, redirect) = redirectService.findById(identifier, true)

        return ok(redirect)
    }

    @Secured
    override fun create(alteration: RedirectAlterationDto): ResponseEntity<RedirectDto> {
        val (_, redirect) = redirectService.create(
            redirectAlterationDtoConverter.convertToEntity(
                alteration
            )
        )

        return created(
            buildFromCurrentRequest {
                configureSsl(sslConfigurationProperties.fallbackToHttps)
                path("/${redirect!!.id}")
                build().toUri()
            }
        ).body(redirect)
    }

    @Secured
    override fun update(identifier: String, alteration: RedirectAlterationDto): ResponseEntity<RedirectDto> {
        val (_, redirect) = redirectService.update(
            identifier,
            redirectAlterationDtoConverter.convertToEntity(
                alteration
            )
        )

        return ok(redirect)
    }

    @Secured
    override fun removeById(identifier: String): ResponseEntity<*> {
        redirectService.removeById(identifier)

        return noContent
    }
}