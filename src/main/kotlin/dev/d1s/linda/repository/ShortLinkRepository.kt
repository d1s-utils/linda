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

package dev.d1s.linda.repository

import dev.d1s.linda.entity.shortLink.ShortLink
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ShortLinkRepository : JpaRepository<ShortLink, String> {

    fun findByAlias(alias: String): Optional<ShortLink>

    @Query(
        nativeQuery = true,
        value = "select * from short_link where alias regexp ?"
    )
    fun findByAliasMatches(regex: String): Set<ShortLink>

    fun findByDisableAfterIsNotNull(): Set<ShortLink>
}