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

package dev.d1s.linda.constant.lp

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AvailabilityChangeLongPollingConstantsTest {

    @Test
    fun `should return valid availability change related values`() {
        expectThat(AVAILABILITY_CHANGE_CREATED_GROUP) isEqualTo
                "availability-change-created"

        expectThat(AVAILABILITY_CHANGE_REMOVED_GROUP) isEqualTo
                "availability-change-removed"

        expectThat(AVAILABILITY_CHECK_PERFORMED_GROUP) isEqualTo
                "availability-check-performed"

        expectThat(GLOBAL_AVAILABILITY_CHECK_PERFORMED_GROUP) isEqualTo
                "global-availability-check-performed"
    }
}