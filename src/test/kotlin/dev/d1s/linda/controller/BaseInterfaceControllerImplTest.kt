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

package dev.d1s.linda.controller

import com.ninjasquad.springmockk.MockkBean
import dev.d1s.linda.constant.mapping.BASE_INTERFACE_CONFIRMATION_MAPPING
import dev.d1s.linda.constant.mapping.BASE_INTERFACE_MAPPING
import dev.d1s.linda.constant.utm.UTM_CAMPAIGN
import dev.d1s.linda.controller.impl.BaseInterfaceControllerImpl
import dev.d1s.linda.service.BaseInterfaceService
import dev.d1s.teabag.stdlib.text.replacePlaceholder
import dev.d1s.teabag.testing.constant.VALID_STUB
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.servlet.view.RedirectView

@ContextConfiguration(classes = [BaseInterfaceControllerImpl::class])
@WebMvcTest(
    controllers = [BaseInterfaceControllerImpl::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class BaseInterfaceControllerImplTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var baseInterfaceService: BaseInterfaceService

    @BeforeEach
    fun setup() {
        every {
            baseInterfaceService.createRedirectView(
                VALID_STUB,
                null,
                null,
                VALID_STUB,
                null,
                null,
                any()
            )
        } returns RedirectView(VALID_STUB)
    }

    @Test
    fun `should perform unconfirmed redirect`() {
        this.performRedirect(BASE_INTERFACE_MAPPING)

        verify {
            baseInterfaceService.createRedirectView(
                VALID_STUB,
                null,
                null,
                VALID_STUB,
                null,
                null,
                false
            )
        }
    }

    @Test
    fun `should return confirmed redirect`() {
        this.performRedirect(BASE_INTERFACE_CONFIRMATION_MAPPING)

        verify {
            baseInterfaceService.createRedirectView(
                VALID_STUB,
                null,
                null,
                VALID_STUB,
                null,
                null,
                true
            )
        }
    }

    private fun performRedirect(path: String) {
        mockMvc.get(
            path.replacePlaceholder(
                "alias" to VALID_STUB
            )
        ) {
            param(UTM_CAMPAIGN, VALID_STUB)
        }.andExpect {
            status {
                isFound()
            }

            redirectedUrl(VALID_STUB)
        }
    }
}