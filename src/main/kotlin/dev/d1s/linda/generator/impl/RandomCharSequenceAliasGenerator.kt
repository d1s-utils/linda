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

package dev.d1s.linda.generator.impl

import dev.d1s.linda.dto.shortLink.ShortLinkCreationDto
import dev.d1s.linda.generator.AliasGenerator
import dev.d1s.linda.service.ShortLinkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@Component
class RandomCharSequenceAliasGenerator : AliasGenerator {

    override val identifier = "random-char-sequence"

    @set:Autowired
    lateinit var shortLinkService: ShortLinkService

    override fun generateAlias(creation: ShortLinkCreationDto): String {
        var length = INITIAL_LENGTH
        var aliasCandidate = this.getRandomCharSequence(length)

        while (shortLinkService.doesAliasExist(aliasCandidate)) {
            aliasCandidate = this.getRandomCharSequence(++length)
        }

        return aliasCandidate
    }

    private fun getRandomCharSequence(length: Int) = buildString {
        val rnd = ThreadLocalRandom.current()

        for (i in 0 until length) {
            // a-z
            val randomChar = 'a' + rnd.nextInt(26)

            val randomInt = rnd.nextInt(10)

            if (rnd.nextBoolean()) {
                append(randomInt)
            } else {
                append(randomChar.let {
                    if (rnd.nextBoolean()) {
                        it.uppercaseChar()
                    } else {
                        it
                    }
                })
            }
        }
    }

    private companion object {
        private const val INITIAL_LENGTH = 4
    }
}