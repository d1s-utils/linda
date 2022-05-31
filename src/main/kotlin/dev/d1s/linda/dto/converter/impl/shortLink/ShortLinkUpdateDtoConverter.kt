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

package dev.d1s.linda.dto.converter.impl.shortLink

import dev.d1s.linda.dto.shortLink.CommonShortLinkDto
import dev.d1s.linda.dto.shortLink.ShortLinkUpdateDto
import dev.d1s.linda.entity.shortLink.ShortLink
import dev.d1s.linda.service.UtmParameterService
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.dto.DtoValidator
import dev.d1s.teabag.stdlib.collection.mapToMutableSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ShortLinkUpdateDtoConverter : DtoConverter<ShortLinkUpdateDto, ShortLink> {

    @set:Autowired
    lateinit var commonShortLinkDtoValidator: DtoValidator<CommonShortLinkDto>

    @set:Autowired
    lateinit var utmParameterService: UtmParameterService

    override fun convertToEntity(dto: ShortLinkUpdateDto): ShortLink = dto.run {
        commonShortLinkDtoValidator.validate(this)

        ShortLink(
            alias,
            target,
            aliasType,
            allowUtmParameters,
            deleteAfter,
            defaultUtmParameters.mapToMutableSet {
                val (utmParameter, _) = utmParameterService.findById(it)
                utmParameter
            },
            allowedUtmParameters.mapToMutableSet {
                val (utmParameter, _) = utmParameterService.findById(it)
                utmParameter
            }
        )
    }
}