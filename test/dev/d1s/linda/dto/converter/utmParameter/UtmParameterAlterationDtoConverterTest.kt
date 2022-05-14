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

package dev.d1s.linda.dto.converter.utmParameter

import dev.d1s.linda.dto.converter.impl.utmParameter.UtmParameterAlterationDtoConverter
import dev.d1s.linda.testUtil.setNullAutoGeneratedValues
import dev.d1s.linda.testUtil.utmParameter
import dev.d1s.linda.testUtil.utmParameterAlterationDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@SpringBootTest
@ContextConfiguration(classes = [UtmParameterAlterationDtoConverter::class])
class UtmParameterAlterationDtoConverterTest {

    @Autowired
    private lateinit var converter: UtmParameterAlterationDtoConverter

    @Test
    fun `should convert utm parameter alteration dto to entity`() {
        expectThat(
            converter.convertToEntity(utmParameterAlterationDto)
        ) isEqualTo utmParameter.copy().setNullAutoGeneratedValues()
    }
}