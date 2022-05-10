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

import dev.d1s.linda.configuration.properties.AvailabilityChecksConfigurationProperties
import dev.d1s.linda.domain.ShortLink
import dev.d1s.linda.domain.utm.UtmParameterPurpose
import dev.d1s.linda.exception.notAllowed.impl.DefaultUtmParametersNotAllowedException
import dev.d1s.linda.exception.notFound.impl.ShortLinkNotFoundException
import dev.d1s.linda.repository.ShortLinkRepository
import dev.d1s.linda.service.AvailabilityChangeService
import dev.d1s.linda.service.ShortLinkService
import dev.d1s.linda.strategy.shortLink.ShortLinkFindingStrategy
import dev.d1s.linda.strategy.shortLink.byAlias
import dev.d1s.linda.strategy.shortLink.byId
import dev.d1s.linda.util.mapToIdSet
import dev.d1s.teabag.log4j.logger
import dev.d1s.teabag.log4j.util.lazyDebug
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class ShortLinkServiceImpl : ShortLinkService {

    @Autowired
    private lateinit var shortLinkRepository: ShortLinkRepository

    @Autowired
    private lateinit var availabilityChangeService: AvailabilityChangeService

    @Autowired
    private lateinit var availabilityChecksConfigurationProperties: AvailabilityChecksConfigurationProperties

    @Autowired
    private lateinit var scheduler: ThreadPoolTaskScheduler

    @Lazy
    @Autowired
    private lateinit var shortLinkService: ShortLinkServiceImpl

    private val log = logger()

    private val scheduledDeletions =
        mutableMapOf<String, ScheduledFuture<*>>()

    @Transactional(readOnly = true)
    override fun findAll(): Set<ShortLink> =
        shortLinkRepository.findAll().toSet()
            .also {
                log.lazyDebug {
                    "found all short links: ${
                        it.mapToIdSet()
                    }"
                }
            }

    @Transactional(readOnly = true)
    override fun find(shortLinkFindingStrategy: ShortLinkFindingStrategy): ShortLink =
        when (shortLinkFindingStrategy) {
            is ShortLinkFindingStrategy.ById -> shortLinkRepository.findById(shortLinkFindingStrategy.identifier)
            is ShortLinkFindingStrategy.ByAlias -> shortLinkRepository.findByAlias(
                shortLinkFindingStrategy.identifier
            )
        }.orElseThrow {
            ShortLinkNotFoundException(shortLinkFindingStrategy.identifier)
        }.also {
            log.lazyDebug {
                "found short link using $shortLinkFindingStrategy strategy: $it"
            }
        }

    @Transactional
    override fun create(shortLink: ShortLink): ShortLink {
        shortLink.validate()

        if (
            availabilityChecksConfigurationProperties.eagerAvailabilityCheck
        ) {
            shortLink.availabilityChanges +=
                availabilityChangeService.checkAvailability(shortLink)
        }

        shortLinkService.assignUtmParameters(
            shortLink,
            shortLink,
            UtmParameterPurpose.DEFAULT
        )

        shortLinkService.assignUtmParameters(
            shortLink,
            shortLink,
            UtmParameterPurpose.ALLOWED
        )

        val savedShortLink = shortLinkRepository.save(
            shortLink
        )

        shortLinkService.scheduleForDeletion(
            savedShortLink
        )

        log.lazyDebug {
            "created short link: $savedShortLink"
        }

        return savedShortLink
    }

    @Transactional
    override fun update(id: String, shortLink: ShortLink): ShortLink {
        shortLink.validate()

        val foundShortLink = shortLinkService.find(byId(id))

        val willSchedule = foundShortLink.deleteAfter != shortLink.deleteAfter

        foundShortLink.url = shortLink.url
        foundShortLink.alias = shortLink.alias
        foundShortLink.allowUtmParameters = shortLink.allowUtmParameters
        foundShortLink.deleteAfter = shortLink.deleteAfter
        foundShortLink.redirects = shortLink.redirects

        shortLinkService.assignUtmParameters(
            foundShortLink,
            shortLink,
            UtmParameterPurpose.DEFAULT
        )

        shortLinkService.assignUtmParameters(
            foundShortLink,
            shortLink,
            UtmParameterPurpose.ALLOWED
        )

        val savedShortLink = shortLinkRepository.save(foundShortLink)

        log.lazyDebug {
            "updated short link: $savedShortLink"
        }

        if (willSchedule) {
            shortLinkService.scheduleForDeletion(savedShortLink)
        }

        return savedShortLink
    }

    override fun assignUtmParameters(
        shortLink: ShortLink,
        associatedShortLink: ShortLink,
        purpose: UtmParameterPurpose
    ) {
        val utmParameters = associatedShortLink.chooseUtmParameters(purpose)
        val originUtmParameters = shortLink.chooseUtmParameters(purpose)

        originUtmParameters.forEach {
            if (!utmParameters.contains(it)) {
                originUtmParameters.remove(it)
            }
        }

        when (purpose) {
            UtmParameterPurpose.DEFAULT -> {
                shortLink.defaultUtmParameters = utmParameters.toMutableSet()
            }

            UtmParameterPurpose.ALLOWED -> {
                shortLink.allowedUtmParameters = utmParameters.toMutableSet()
            }
        }

        utmParameters.forEach {
            when (purpose) {
                UtmParameterPurpose.DEFAULT -> {
                    it.defaultForShortLinks += shortLink
                }

                UtmParameterPurpose.ALLOWED -> {
                    it.allowedForShortLinks += shortLink
                }
            }
        }
    }

    @Transactional
    override fun removeById(id: String) {
        shortLinkRepository.deleteById(id)

        log.lazyDebug {
            "removed short link with id $id"
        }
    }

    @Transactional
    override fun removeAll(shortLinks: Iterable<ShortLink>) {
        shortLinkRepository.deleteAllInBatch(shortLinks)

        log.lazyDebug {
            "removed short links in batch: $shortLinks"
        }
    }

    override fun doesAliasExist(alias: String): Boolean = try {
        shortLinkService.find(byAlias(alias))
        true
    } catch (_: ShortLinkNotFoundException) {
        false
    }

    override fun isExpired(shortLink: ShortLink): Boolean =
        (shortLink.deleteAfter?.let { deleteAfter ->
            (shortLink.creationTime!! + deleteAfter) < Instant.now()
        } ?: false).also {
            log.lazyDebug {
                "isExpired: $it; shortLink: $shortLink"
            }
        }

    override fun scheduleForDeletion(shortLink: ShortLink) {
        val id = shortLink.id!!

        log.lazyDebug {
            "scheduling $id for deletion"
        }

        shortLink.deleteAfter?.let { deleteAfter ->
            scheduledDeletions.put(
                id, scheduler.schedule({
                    shortLinkService.removeById(id)
                }, shortLink.creationTime!! + deleteAfter)
            )?.let {
                if (!it.isDone) {
                    it.cancel(true)
                }
            }

            log.lazyDebug {
                "scheduled $id for deletion."
            }
        } ?: run {
            log.lazyDebug {
                "deleteAfter is null, won't schedule for deletion."
            }
        }
    }

    @Transactional(readOnly = true)
    override fun scheduleAllEphemeralShortLinksForDeletion() {
        log.lazyDebug {
            "scheduling all ephemeral short links for deletion"
        }

        shortLinkRepository.findByDeleteAfterIsNull()
            .forEach(shortLinkService::scheduleForDeletion)

        log.lazyDebug {
            "scheduled all ephemeral short links for deletion."
        }
    }

    private fun ShortLink.validate() {
        if (!allowUtmParameters && defaultUtmParameters.isNotEmpty()) {
            throw DefaultUtmParametersNotAllowedException(defaultUtmParameters)
        }
    }

    private fun ShortLink.chooseUtmParameters(purpose: UtmParameterPurpose) = when (purpose) {
        UtmParameterPurpose.DEFAULT -> this.defaultUtmParameters
        UtmParameterPurpose.ALLOWED -> this.allowedUtmParameters
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    protected fun cleanScheduledDeletions() {
        scheduledDeletions.forEach { (id, scheduledFuture) ->
            if (scheduledFuture.isDone) {
                scheduledDeletions.remove(id)
            }
        }
    }
}