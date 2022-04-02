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

package dev.d1s.linda.converter.impl

import dev.d1s.linda.domain.ShortLink
import dev.d1s.linda.dto.shortLink.ShortLinkDto
import dev.d1s.linda.service.RedirectService
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.stdlib.checks.checkNotNull
import dev.d1s.teabag.stdlib.collection.mapToSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ShortLinkDtoConverter : DtoConverter<ShortLinkDto, ShortLink> {

    @Autowired
    private lateinit var redirectService: RedirectService

    override fun convertToDto(entity: ShortLink): ShortLinkDto =
        ShortLinkDto(
            entity.id.checkNotNull("id"),
            entity.url,
            entity.alias,
            entity.creationTime.checkNotNull("creation time"),
            entity.redirects.mapToSet {
                it.id.checkNotNull("redirect id")
            })

    override fun convertToEntity(dto: ShortLinkDto): ShortLink =
        ShortLink(dto.url, dto.alias).apply {
            id = dto.id
            creationTime = dto.creationTime
            redirects = dto.redirects.mapToSet {
                redirectService.findById(it)
            }
        }
}