/*
   Copyright 2022 Linda project

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package uno.d1s.linda.converter.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uno.d1s.linda.converter.AbstractDtoConverter
import uno.d1s.linda.domain.Redirect
import uno.d1s.linda.dto.redirect.RedirectDto
import uno.d1s.linda.service.ShortLinkService
import uno.d1s.linda.strategy.shortLink.byId
import uno.d1s.linda.util.checkNotNull

@Component
class RedirectDtoConverter : AbstractDtoConverter<Redirect, RedirectDto>() {

    @Autowired
    private lateinit var shortLinkService: ShortLinkService

    override fun convertToDto(entity: Redirect): RedirectDto =
        RedirectDto(
            entity.id.checkNotNull("id"),
            entity.shortLink.id.checkNotNull("short link id"),
            entity.creationTime.checkNotNull("creation time")
        )

    override fun convertToEntity(dto: RedirectDto): Redirect =
        Redirect(shortLinkService.find(byId(dto.shortLink))).apply {
            id = dto.id
            creationTime = dto.creationTime
        }
}