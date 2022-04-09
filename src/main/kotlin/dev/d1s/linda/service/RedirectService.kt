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

import dev.d1s.linda.domain.Redirect
import dev.d1s.linda.domain.ShortLink
import dev.d1s.linda.strategy.shortLink.ShortLinkFindingStrategy

interface RedirectService {

    fun findAll(): Set<Redirect>

    fun findAllByShortLink(shortLinkFindingStrategy: ShortLinkFindingStrategy): Set<Redirect>

    fun findById(id: String): Redirect

    fun create(shortLink: ShortLink): Redirect

    fun create(shortLinkFindingStrategy: ShortLinkFindingStrategy): Redirect

    fun remove(redirect: Redirect): Redirect

    fun remove(id: String): Redirect

    fun removeAll(redirects: Set<Redirect>): Set<Redirect>

    fun removeAll()

    fun removeAllByShortLink(shortLinkFindingStrategy: ShortLinkFindingStrategy): Set<Redirect>
}