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

package dev.d1s.linda.dto.converter.redirect

import dev.d1s.linda.dto.converter.impl.redirect.RedirectDtoConverter
import dev.d1s.linda.testUtil.mockRedirect
import dev.d1s.linda.testUtil.mockRedirectDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@SpringBootTest
@ContextConfiguration(classes = [RedirectDtoConverter::class])
internal class RedirectDtoConverterTest {

    @Autowired
    private lateinit var converter: RedirectDtoConverter

    private val redirect = mockRedirect(true)

    // redirectDto has the same properties as the associated entity
    private val redirectDto = mockRedirectDto()

    @Test
    fun `should convert to dto`() {
        expectThat(
            converter.convertToDto(redirect)
        ) isEqualTo redirectDto
    }
}