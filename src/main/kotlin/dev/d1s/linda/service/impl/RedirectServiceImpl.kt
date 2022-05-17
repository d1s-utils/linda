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

package dev.d1s.linda.service.impl

import dev.d1s.linda.constant.lp.REDIRECT_CREATED_GROUP
import dev.d1s.linda.constant.lp.REDIRECT_REMOVED_GROUP
import dev.d1s.linda.constant.lp.REDIRECT_UPDATED_GROUP
import dev.d1s.linda.entity.Redirect
import dev.d1s.linda.entity.utmParameter.UtmParameter
import dev.d1s.teabag.dto.EntityWithDto
import dev.d1s.teabag.dto.EntityWithDtoSet
import dev.d1s.linda.dto.redirect.RedirectDto
import dev.d1s.linda.event.data.redirect.CommonRedirectEventData
import dev.d1s.linda.event.data.redirect.RedirectUpdatedEventData
import dev.d1s.linda.exception.notAllowed.impl.DefaultUtmParameterOverrideNotAllowedException
import dev.d1s.linda.exception.notAllowed.impl.IllegalUtmParametersException
import dev.d1s.linda.exception.notAllowed.impl.UtmParametersNotAllowedException
import dev.d1s.linda.exception.notFound.impl.RedirectNotFoundException
import dev.d1s.linda.repository.RedirectRepository
import dev.d1s.linda.service.RedirectService
import dev.d1s.linda.util.mapToIdSet
import dev.d1s.lp.server.publisher.AsyncLongPollingEventPublisher
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.dto.util.convertToDtoIf
import dev.d1s.teabag.dto.util.convertToDtoSetIf
import dev.d1s.teabag.dto.util.converterForSet
import org.lighthousegames.logging.logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RedirectServiceImpl : RedirectService {

    @Autowired
    private lateinit var redirectRepository: RedirectRepository

    @Autowired
    private lateinit var publisher: AsyncLongPollingEventPublisher

    @Autowired
    private lateinit var redirectDtoConverter: DtoConverter<RedirectDto, Redirect>

    @Lazy
    @Autowired
    private lateinit var redirectService: RedirectServiceImpl

    private val redirectSetDtoConverter by lazy {
        redirectDtoConverter.converterForSet()
    }

    private val log = logging()

    @Transactional(readOnly = true)
    override fun findAll(requireDto: Boolean): EntityWithDtoSet<Redirect, RedirectDto> {
        val redirects = redirectRepository.findAll().toSet()

        log.debug {
            "found all redirects: ${
                redirects.mapToIdSet()
            }"
        }

        return redirects to redirectSetDtoConverter
            .convertToDtoSetIf(redirects, requireDto)
    }

    @Transactional(readOnly = true)
    override fun findById(id: String, requireDto: Boolean): EntityWithDto<Redirect, RedirectDto> {
        val redirect = redirectRepository.findById(id).orElseThrow {
            RedirectNotFoundException(id)
        }

        log.debug {
            "found redirect by id: $redirect"
        }

        return redirect to redirectDtoConverter
            .convertToDtoIf(redirect, requireDto)
    }

    @Transactional
    override fun create(redirect: Redirect): EntityWithDto<Redirect, RedirectDto> {
        val createdRedirect = redirectService.assignUtmParametersAndSave(
            redirect.apply {
                validate()

                val defaultUtmParameters = shortLink.defaultUtmParameters

                defaultUtmParameters.forEach { defaultUtmParameter ->
                    utmParameters.forEach { utmParameter ->
                        if (utmParameter.type == defaultUtmParameter.type
                            && !defaultUtmParameter.allowOverride
                            && utmParameter !in defaultUtmParameters
                        ) {
                            throw DefaultUtmParameterOverrideNotAllowedException(defaultUtmParameters)
                        }
                    }

                    if (
                        defaultUtmParameter.type !in utmParameters.map {
                            it.type
                        }
                    ) {
                        utmParameters += defaultUtmParameter
                    }
                }
            },
            redirect.utmParameters
        )

        val dto = redirectDtoConverter.convertToDto(redirect)

        publisher.publish(
            REDIRECT_CREATED_GROUP,
            dto.shortLink,
            CommonRedirectEventData(dto)
        )

        return createdRedirect to dto
    }

    @Transactional
    override fun update(id: String, redirect: Redirect): EntityWithDto<Redirect, RedirectDto> {
        redirect.validate()

        val (foundRedirect, oldRedirectDto) = redirectService.findById(id, true)

        foundRedirect.shortLink = redirect.shortLink

        val savedRedirect = redirectService.assignUtmParametersAndSave(
            foundRedirect,
            redirect.utmParameters
        )

        log.debug {
            "updated redirect: $savedRedirect"
        }

        val dto = redirectDtoConverter.convertToDto(savedRedirect)

        publisher.publish(
            REDIRECT_UPDATED_GROUP,
            id,
            RedirectUpdatedEventData(oldRedirectDto!!, dto)
        )

        return savedRedirect to dto
    }

    @Transactional
    override fun assignUtmParametersAndSave(redirect: Redirect, utmParameters: Set<UtmParameter>): Redirect {
        val originUtmParameters = redirect.utmParameters

        originUtmParameters.forEach {
            if (!utmParameters.contains(it)) {
                originUtmParameters.remove(it)
            }
        }

        redirect.utmParameters = utmParameters.toMutableSet()

        utmParameters.forEach {
            it.redirects += redirect
        }

        return redirectRepository.save(redirect)
    }

    @Transactional
    override fun removeById(id: String) {
        val (redirectToRemove, redirectDto) = redirectService.findById(id, true)

        redirectRepository.delete(redirectToRemove)

        publisher.publish(
            REDIRECT_REMOVED_GROUP,
            id,
            CommonRedirectEventData(redirectDto!!)
        )

        log.debug {
            "removed redirect with id $id"
        }
    }

    private fun Redirect.validate() {
        if (utmParameters.isNotEmpty()) {
            if (!shortLink.allowUtmParameters) {
                throw UtmParametersNotAllowedException
            }

            val allowedUtmParameters = shortLink.allowedUtmParameters

            if (allowedUtmParameters.isNotEmpty()) {
                if (!allowedUtmParameters.containsAll(utmParameters)) {
                    throw IllegalUtmParametersException(
                        utmParameters.filter {
                            it !in allowedUtmParameters
                        }.toSet()
                    )
                }
            }
        }
    }
}