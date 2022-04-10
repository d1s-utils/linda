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

package dev.d1s.linda.service.impl

import dev.d1s.caching.annotation.CachePutByIdProvider
import dev.d1s.caching.annotation.CacheableList
import dev.d1s.linda.cache.idProvider.ShortLinkIdProvider
import dev.d1s.linda.constant.cache.SHORT_LINKS_CACHE
import dev.d1s.linda.domain.ShortLink
import dev.d1s.linda.exception.impl.ShortLinkNotFoundException
import dev.d1s.linda.repository.ShortLinkRepository
import dev.d1s.linda.service.ShortLinkService
import dev.d1s.linda.strategy.shortLink.ShortLinkFindingStrategy
import dev.d1s.linda.strategy.shortLink.byAlias
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ShortLinkServiceImpl : ShortLinkService {

    @Autowired
    private lateinit var shortLinkRepository: ShortLinkRepository

    @Lazy
    @Autowired
    private lateinit var shortLinkService: ShortLinkServiceImpl

    @Transactional(readOnly = true)
    @CacheableList(cacheName = SHORT_LINKS_CACHE, idProvider = ShortLinkIdProvider::class)
    override fun findAll(): Set<ShortLink> =
        shortLinkRepository.findAll().toSet()

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = [SHORT_LINKS_CACHE])
    override fun find(shortLinkFindingStrategy: ShortLinkFindingStrategy): ShortLink =
        when (shortLinkFindingStrategy) {
            is ShortLinkFindingStrategy.ById -> shortLinkRepository.findById(shortLinkFindingStrategy.identifier)
            is ShortLinkFindingStrategy.ByAlias -> shortLinkRepository.findShortLinkByAliasEquals(
                shortLinkFindingStrategy.identifier
            )
        }.orElseThrow {
            ShortLinkNotFoundException
        }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @CachePutByIdProvider(cacheName = SHORT_LINKS_CACHE, idProvider = ShortLinkIdProvider::class)
    override fun create(shortLink: ShortLink): ShortLink =
        shortLinkRepository.save(shortLink)

    @Transactional
    @CacheEvict(SHORT_LINKS_CACHE, key = "#id")
    override fun removeById(id: String) =
        shortLinkRepository.deleteById(id)

    override fun doesAliasExist(alias: String): Boolean = try {
        shortLinkService.find(byAlias(alias))
        true
    } catch (_: ShortLinkNotFoundException) {
        false
    }
}