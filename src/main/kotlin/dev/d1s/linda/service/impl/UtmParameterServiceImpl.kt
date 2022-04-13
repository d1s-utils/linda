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

package dev.d1s.linda.service.impl

import dev.d1s.linda.domain.utm.UtmParameter
import dev.d1s.linda.domain.utm.UtmParameterType
import dev.d1s.linda.exception.impl.alreadyExists.UtmParameterAlreadyExistsException
import dev.d1s.linda.exception.impl.notFound.UtmParameterNotFoundException
import dev.d1s.linda.repository.UtmParameterRepository
import dev.d1s.linda.service.UtmParameterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UtmParameterServiceImpl : UtmParameterService {

    @Autowired
    private lateinit var utmParameterRepository: UtmParameterRepository

    @Lazy
    @Autowired
    private lateinit var utmParameterService: UtmParameterServiceImpl

    @Transactional(readOnly = true)
    override fun findAll(): Set<UtmParameter> =
        utmParameterRepository.findAll().toSet()

    @Transactional(readOnly = true)
    override fun findById(id: String): UtmParameter =
        utmParameterRepository.findById(id).orElseThrow {
            UtmParameterNotFoundException
        }

    @Transactional(readOnly = true)
    override fun findByTypeAndValue(type: UtmParameterType, value: String): Optional<UtmParameter> =
        utmParameterRepository.findUtmParameterByTypeAndValue(type, value)

    @Transactional
    override fun create(utmParameter: UtmParameter): UtmParameter {
        if (utmParameterService.findByTypeAndValue(utmParameter.type, utmParameter.parameterValue).isPresent) {
            throw UtmParameterAlreadyExistsException
        }

        return utmParameterRepository.save(utmParameter)
    }

    @Transactional
    override fun removeById(id: String) {
        utmParameterRepository.deleteById(id)
    }
}