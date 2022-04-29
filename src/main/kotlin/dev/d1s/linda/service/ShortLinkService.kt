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

package dev.d1s.linda.service

import dev.d1s.linda.domain.ShortLink
import dev.d1s.linda.domain.utm.UtmParameter
import dev.d1s.linda.strategy.shortLink.ShortLinkFindingStrategy

interface ShortLinkService {

    fun findAll(): Set<ShortLink>

    fun find(shortLinkFindingStrategy: ShortLinkFindingStrategy): ShortLink

    fun create(shortLink: ShortLink): ShortLink

    fun update(id: String, shortLink: ShortLink): ShortLink

    fun assignDefaultUtmParameters(shortLink: ShortLink, utmParameters: Set<UtmParameter>)

    fun assignAllowedUtmParameters(shortLink: ShortLink, utmParameters: Set<UtmParameter>)

    fun removeById(id: String)

    fun doesAliasExist(alias: String): Boolean
}