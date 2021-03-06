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

package dev.d1s.linda.dto.converter.impl.redirect

import dev.d1s.advice.exception.NotFoundException
import dev.d1s.linda.dto.redirect.RedirectCreationDto
import dev.d1s.linda.entity.redirect.Redirect
import dev.d1s.linda.service.ShortLinkService
import dev.d1s.linda.service.UtmParameterService
import dev.d1s.linda.strategy.shortLink.byId
import dev.d1s.teabag.dto.DtoConverter
import dev.d1s.teabag.stdlib.collection.mapToMutableSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RedirectCreationDtoConverter : DtoConverter<RedirectCreationDto, Redirect> {

    @set:Autowired
    lateinit var shortLinkService: ShortLinkService

    @set:Autowired
    lateinit var utmParameterService: UtmParameterService

    override fun convertToEntity(dto: RedirectCreationDto): Redirect {
        val (shortLink, _) = shortLinkService.find(byId(dto.shortLink))

        return Redirect(
            shortLink,
        ).apply {
            utmParameters = dto.utmParameters.mapToMutableSet {
                val (utmParameter, _) =
                    utmParameterService.findById(it)

                utmParameter
            }

            templateVariables = dto.rawAlias?.let {
                try {
                    val (_, templateVariables) =
                        shortLinkService.resolveTemplateVariables(it)
                    templateVariables
                } catch (_: NotFoundException) {
                    setOf()
                }
            } ?: setOf()
        }
    }
}