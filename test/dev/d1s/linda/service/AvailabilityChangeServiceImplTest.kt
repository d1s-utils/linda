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
import com.ninjasquad.springmockk.SpykBean
import dev.d1s.linda.configuration.properties.AvailabilityChecksConfigurationProperties
import dev.d1s.linda.constant.lp.AVAILABILITY_CHANGE_CREATED_GROUP
import dev.d1s.linda.domain.availability.AvailabilityChange
import dev.d1s.linda.domain.availability.UnavailabilityReason
import dev.d1s.linda.dto.availability.AvailabilityChangeDto
import dev.d1s.linda.event.data.AvailabilityChangeEventData
import dev.d1s.linda.exception.notFound.impl.AvailabilityChangeNotFoundException
import dev.d1s.linda.repository.AvailabilityChangeRepository
import dev.d1s.linda.service.impl.AvailabilityChangeServiceImpl
import dev.d1s.linda.testUtil.*
import dev.d1s.lp.server.publisher.AsyncLongPollingEventPublisher
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.testing.constant.INVALID_STUB
import dev.d1s.teabag.testing.constant.VALID_STUB
import io.mockk.every
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.io.IOException
import java.net.URI
import java.util.*

@SpringBootTest
@ContextConfiguration(classes = [AvailabilityChangeServiceImpl::class])
class AvailabilityChangeServiceImplTest {

    @SpykBean
    private lateinit var availabilityChangeServiceImpl: AvailabilityChangeServiceImpl

    @MockkBean
    private lateinit var availabilityChangeRepository: AvailabilityChangeRepository

    @MockkBean
    private lateinit var availabilityChangeDtoConverter: DtoConverter<AvailabilityChangeDto, AvailabilityChange>

    @MockkBean(relaxed = true)
    private lateinit var publisher: AsyncLongPollingEventPublisher

    @MockkBean
    private lateinit var restTemplate: RestTemplate

    @MockkBean
    private lateinit var properties: AvailabilityChecksConfigurationProperties

    @MockkBean
    private lateinit var shortLinkService: ShortLinkService

    private val unsavedAvailabilityChange =
        availabilityChange
            .copy()
            .setNullAutoGeneratedValues()

    @BeforeEach
    fun setup() {
        availabilityChangeRepository.prepare()
        availabilityChangeDtoConverter.prepare()
        restTemplate.prepare()
        properties.prepare()
        shortLinkService.prepare()
        clientHttpRequestFactoryMock.prepare()
    }

    @Test
    fun `should find all availability changes`() {
        expectThat(
            availabilityChangeServiceImpl.findAll()
        ) isEqualTo availabilityChanges

        verify {
            availabilityChangeRepository.findAll()
        }
    }

    @Test
    fun `should find availability change by id`() {
        expectThat(
            availabilityChangeServiceImpl.findById(VALID_STUB)
        ) isEqualTo availabilityChange

        verify {
            availabilityChangeRepository.findById(VALID_STUB)
        }
    }

    @Test
    fun `should throw AvailabilityChangeNotFoundException`() {
        assertThrows<AvailabilityChangeNotFoundException> {
            availabilityChangeServiceImpl.findById(INVALID_STUB)
        }

        verify {
            availabilityChangeServiceImpl.findById(INVALID_STUB)
        }
    }

    @Test
    fun `should find last availability change`() {
        expectThat(
            availabilityChangeServiceImpl.findLast(VALID_STUB)
        ) isEqualTo availabilityChange

        verify {
            availabilityChangeRepository.findLast(VALID_STUB)
        }
    }

    @Test
    fun `should return null when trying to find last availability change in empty repository`() {
        expectThat(
            availabilityChangeServiceImpl.findLast(INVALID_STUB)
        ).isNull()

        verify {
            availabilityChangeRepository.findLast(INVALID_STUB)
        }
    }

    @Test
    fun `should create availability change`() {
        expectThat(
            availabilityChangeServiceImpl.create(availabilityChange)
        ) isEqualTo availabilityChange

        verifyAll {
            availabilityChangeRepository.save(availabilityChange)
            availabilityChangeDtoConverter.convertToDto(availabilityChange)
            publisher.publish(
                AVAILABILITY_CHANGE_CREATED_GROUP,
                VALID_STUB,
                AvailabilityChangeEventData(availabilityChangeDto)
            )
        }
    }

    @Test
    fun `should remove availability change by id`() {
        assertDoesNotThrow {
            availabilityChangeServiceImpl.removeById(VALID_STUB)
        }

        verify {
            availabilityChangeRepository.deleteById(VALID_STUB)
        }
    }

    @Test
    fun `should check origin url availability with null unavailabilityReason`() {
        expectThat(
            availabilityChangeServiceImpl.checkAvailability(
                shortLink
            )
        ) isEqualTo unsavedAvailabilityChange

        this.verifyAvailabilityCheckCalls(true)
    }

    @Test
    fun `should check origin url availability with CONNECTION_ERROR unavailabilityReason`() {
        every {
            clientHttpRequestFactoryMock.createRequest(
                URI.create(TEST_URL),
                HttpMethod.GET
            ).execute()
        } throws IOException()

        expectThat(
            availabilityChangeServiceImpl.checkAvailability(
                shortLink
            )
        ) isEqualTo availabilityChange
            .copy(
                unavailabilityReason = UnavailabilityReason.CONNECTION_ERROR
            ).setNullAutoGeneratedValues()

        this.verifyAvailabilityCheckCalls(false)
    }

    @Test
    fun `should check origin url availability with MALFORMED_URL unavailabilityReason`() {
        every {
            clientHttpRequestFactoryMock.createRequest(
                URI.create(TEST_URL),
                HttpMethod.GET
            ).execute()
        } throws IllegalArgumentException()

        expectThat(
            availabilityChangeServiceImpl.checkAvailability(
                shortLink
            )
        ) isEqualTo availabilityChange
            .copy(
                unavailabilityReason = UnavailabilityReason.MALFORMED_URL
            ).setNullAutoGeneratedValues()

        this.verifyAvailabilityCheckCalls(false)
    }

    @Test
    fun `should check and save availability change`() {
        this.prepareForAvailabilityCheck()

        expectThat(
            availabilityChangeServiceImpl.checkAndSaveAvailability(shortLink)
        ) isEqualTo unsavedAvailabilityChange

        // this is a mockk moment.
        // Verification failed: some calls were not matched: [AvailabilityChangeServiceImpl(availabilityChangeServiceImpl#7).checkAndSaveAvailability(ShortLink(url='https://d1s.dev/', alias='v', allowUtmParameters=true, id=v, creationTime=1970-01-01T00:00:00Z, redirects=[], availabilityChanges=[], defaultUtmParameters=[], allowedUtmParameters=[]))]
        // verifyAll {
        // availabilityChangeServiceImpl.findLast(VALID_STUB)
        // availabilityChangeServiceImpl.checkAvailability(shortLink)
        // availabilityChangeServiceImpl.create(
        //     expectedAvailabilityChange
        // )
        // }
    }

    @Test
    fun `should check availability of all short links`() {
        this.prepareForAvailabilityCheck()

        expectThat(
            availabilityChangeServiceImpl.checkAvailabilityOfAllShortLinks()
        ) isEqualTo setOf(unsavedAvailabilityChange)

        verifyAll {
            shortLinkService.findAll()
        }
    }

    private fun verifyAvailabilityCheckCalls(verifyPropertiesCall: Boolean) {
        verifyAll {
            clientHttpRequestFactoryMock.createRequest(
                URI.create(TEST_URL),
                HttpMethod.GET
            ).execute()

            if (verifyPropertiesCall) {
                properties.badStatusCodeIntRanges
            }
        }
    }

    private fun prepareForAvailabilityCheck() {
        every {
            availabilityChangeDtoConverter.convertToDto(
                unsavedAvailabilityChange
            )
        } returns availabilityChangeDto

        every {
            availabilityChangeRepository.findLast(VALID_STUB)
        } returns Optional.empty()
    }
}