/*
 * Copyright 2022 Linda project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.linda.service

import com.ninjasquad.springmockk.MockkBean
import dev.d1s.linda.configuration.properties.AvailabilityChecksConfigurationProperties
import dev.d1s.linda.domain.utm.UtmParameterPurpose
import dev.d1s.linda.exception.notAllowed.impl.DefaultUtmParametersNotAllowedException
import dev.d1s.linda.exception.notFound.impl.ShortLinkNotFoundException
import dev.d1s.linda.repository.ShortLinkRepository
import dev.d1s.linda.service.impl.ShortLinkServiceImpl
import dev.d1s.linda.strategy.shortLink.byAlias
import dev.d1s.linda.strategy.shortLink.byId
import dev.d1s.linda.testUtil.*
import dev.d1s.teabag.stdlib.collection.mapToMutableSet
import dev.d1s.teabag.testing.constant.INVALID_STUB
import dev.d1s.teabag.testing.constant.VALID_STUB
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.*

@SpringBootTest
@ContextConfiguration(classes = [ShortLinkServiceImpl::class])
class ShortLinkServiceImplTest {

    @Autowired
    private lateinit var shortLinkServiceImpl: ShortLinkServiceImpl

    @MockkBean
    private lateinit var shortLinkRepository: ShortLinkRepository

    @MockkBean
    private lateinit var availabilityChangeService: AvailabilityChangeService

    @MockkBean
    private lateinit var availabilityChecksConfigurationProperties: AvailabilityChecksConfigurationProperties

    @BeforeEach
    fun setup() {
        shortLinkRepository.prepare()
        availabilityChangeService.prepare()
        availabilityChecksConfigurationProperties.prepare()
    }

    @Test
    fun `should find all short links`() {
        expectThat(
            shortLinkServiceImpl.findAll()
        ) isEqualTo shortLinks

        verify {
            shortLinkRepository.findAll()
        }
    }

    @Test
    fun `should find short link by id`() {
        expectThat(
            shortLinkServiceImpl.find(byId(VALID_STUB))
        ) isEqualTo shortLink

        verify {
            shortLinkRepository.findById(VALID_STUB)
        }
    }

    @Test
    fun `should throw ShortLinkNotFoundException when finding by invalid id`() {
        assertThrows<ShortLinkNotFoundException> {
            shortLinkServiceImpl.find(byId(INVALID_STUB))
        }

        verify {
            shortLinkRepository.findById(INVALID_STUB)
        }
    }

    @Test
    fun `should find short link by alias`() {
        expectThat(
            shortLinkServiceImpl.find(byAlias(VALID_STUB))
        ) isEqualTo shortLink

        verify {
            shortLinkRepository.findShortLinkByAliasEquals(VALID_STUB)
        }
    }

    @Test
    fun `should throw ShortLinkNotFoundException when finding by invalid alias`() {
        assertThrows<ShortLinkNotFoundException> {
            shortLinkServiceImpl.find(byAlias(INVALID_STUB))
        }

        verify {
            shortLinkRepository.findShortLinkByAliasEquals(INVALID_STUB)
        }
    }

    @Test
    fun `should create short link`() {
        val testShortLink = shortLink.copy()
            .setAutoGeneratedValues()

        expectThat(
            shortLinkServiceImpl.create(testShortLink)
        ) isEqualTo testShortLink

        verifyAll {
            availabilityChecksConfigurationProperties.eagerAvailabilityCheck
            availabilityChangeService.checkAvailability(shortLink)
            shortLinkRepository.save(testShortLink)
        }
    }

    @Test
    fun `should throw DefaultUtmParametersNotAllowedException`() {
        val testShortLink = shortLink.copy().apply {
            allowUtmParameters = false
            defaultUtmParameters = utmParameters.toMutableSet()
        }

        assertThrows<DefaultUtmParametersNotAllowedException> {
            shortLinkServiceImpl.create(testShortLink)
        }
    }

    @Test
    fun `should update short link`() {
        val anotherShortLink = shortLink.copy().apply {
            alias = INVALID_STUB
        }

        val updatedRedirect = shortLinkServiceImpl.update(
            VALID_STUB,
            anotherShortLink
        )

        expectThat(
            updatedRedirect.alias
        ) isEqualTo INVALID_STUB

        verify {
            shortLinkRepository.save(updatedRedirect)
        }
    }

    @Test
    fun `should assign utm parameters to short link`() {
        val testUtmParameters = utmParameters.mapToMutableSet {
            it.copy()
        }

        val testShortLink = shortLink.copy()
        val associatedShortLink = shortLink.copy().apply {
            defaultUtmParameters = testUtmParameters
            allowedUtmParameters = testUtmParameters
        }

        setOf(
            UtmParameterPurpose.DEFAULT,
            UtmParameterPurpose.ALLOWED
        ).forEach {
            shortLinkServiceImpl.assignUtmParameters(
                testShortLink,
                associatedShortLink,
                it
            )
        }

        val assignedDefaultUtmParameters = testShortLink.defaultUtmParameters
        val assignedAllowedUtmParameters = testShortLink.allowedUtmParameters

        setOf(
            assignedDefaultUtmParameters,
            assignedAllowedUtmParameters
        ).forEach {
            expectThat(it).containsExactly(
                testUtmParameters
            )
        }

        testUtmParameters.forEach {
            expectThat(
                it.defaultForShortLinks
            ).contains(testShortLink)

            expectThat(
                it.allowedForShortLinks
            ).contains(testShortLink)
        }
    }

    @Test
    fun `should remove short link by id`() {
        assertDoesNotThrow {
            shortLinkServiceImpl.removeById(VALID_STUB)
        }

        verify {
            shortLinkRepository.deleteById(VALID_STUB)
        }
    }

    @Test
    fun `should return valid answer whether the given alias exists or not`() {
        expectThat(
            shortLinkServiceImpl.doesAliasExist(VALID_STUB)
        ).isTrue()

        expectThat(
            shortLinkServiceImpl.doesAliasExist(INVALID_STUB)
        ).isFalse()
    }
}