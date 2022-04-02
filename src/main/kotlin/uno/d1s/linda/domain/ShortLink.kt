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

package uno.d1s.linda.domain

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "short_link")
class ShortLink(
    @Column(nullable = false, unique = true)
    val url: String,

    @Column(nullable = false, unique = true)
    val alias: String
) {

    @Id
    @Column
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    var id: String? = null

    @Column
    @CreationTimestamp
    var creationTime: Instant? = null

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "shortLink")
    lateinit var redirects: List<Redirect>
}