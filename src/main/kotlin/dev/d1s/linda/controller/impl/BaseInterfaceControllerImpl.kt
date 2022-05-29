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

package dev.d1s.linda.controller.impl

import dev.d1s.linda.controller.BaseInterfaceController
import dev.d1s.linda.service.BaseInterfaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
@ConditionalOnProperty("linda.base-interface.enabled", matchIfMissing = true)
class BaseInterfaceControllerImpl : BaseInterfaceController {

    @set:Autowired
    lateinit var baseInterfaceService: BaseInterfaceService

    override fun redirect(
        alias: String,
        utmSource: String?,
        utmMedium: String?,
        utmCampaign: String?,
        utmTerm: String?,
        utmContent: String?
    ): ResponseEntity<String> = baseInterfaceService.createRedirectPage(
        alias, utmSource, utmMedium, utmCampaign, utmTerm, utmContent, false
    )

    override fun confirmRedirect(
        alias: String,
        utmSource: String?,
        utmMedium: String?,
        utmCampaign: String?,
        utmTerm: String?,
        utmContent: String?
    ): ResponseEntity<String> = baseInterfaceService.createRedirectPage(
        alias, utmSource, utmMedium, utmCampaign, utmTerm, utmContent, true
    )
}