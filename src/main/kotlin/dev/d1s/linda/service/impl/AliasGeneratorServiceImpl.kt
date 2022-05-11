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

import dev.d1s.linda.exception.notFound.impl.AliasGeneratorNotFoundException
import dev.d1s.linda.generator.AliasGenerator
import dev.d1s.linda.service.AliasGeneratorService
import org.lighthousegames.logging.logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AliasGeneratorServiceImpl : AliasGeneratorService {

    @Autowired
    private lateinit var aliasGenerators: Set<AliasGenerator>

    private val log = logging()

    override fun getAliasGenerator(identifier: String): AliasGenerator = (aliasGenerators.firstOrNull {
        it.identifier == identifier
    } ?: throw AliasGeneratorNotFoundException(identifier)).also {
        log.debug {
            "retrieved alias generator: $identifier"
        }
    }
}