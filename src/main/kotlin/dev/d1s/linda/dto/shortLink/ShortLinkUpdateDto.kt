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

package dev.d1s.linda.dto.shortLink

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class ShortLinkUpdateDto(
    @field:Pattern(
        regexp = "^https?://[-a-zA-Z\\d+&@#/%?=~_|!:,.;]*[-a-zA-Z\\d+&@#/%=~_|]",
        message = "The provided URL must be valid."
    )
    val url: String,

    // note: this is a hacky way to set custom alias.
    // This feature is not yet implemented:
    // https://github.com/linda-project/linda/issues/11
    @field:NotBlank(message = "aliasGeneratorId must not be blank.")
    val alias: String,

    @field:NotNull(message = "redirects field must not be null.")
    val redirects: Set<String>
)