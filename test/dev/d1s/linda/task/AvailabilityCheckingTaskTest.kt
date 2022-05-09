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

package dev.d1s.linda.task

import com.ninjasquad.springmockk.MockkBean
import dev.d1s.linda.service.AvailabilityChangeService
import dev.d1s.linda.testUtil.prepare
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [AvailabilityCheckingTask::class])
class AvailabilityCheckingTaskTest {

    @Autowired
    private lateinit var availabilityCheckingTask: AvailabilityCheckingTask

    @MockkBean
    private lateinit var availabilityChangeService: AvailabilityChangeService

    @BeforeEach
    fun setup() {
        availabilityChangeService.prepare()
    }

    @Test
    fun `should check availability`() {
        assertDoesNotThrow {
            availabilityCheckingTask.checkAvailability()
        }

        verify {
            availabilityChangeService.checkAvailabilityOfAllShortLinks()
        }
    }
}