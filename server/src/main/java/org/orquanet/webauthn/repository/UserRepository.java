/*
 * Copyright 2020 the original author or authors.
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

package org.orquanet.webauthn.repository;

import org.orquanet.webauthn.repository.model.FidoUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface UserRepository<T> extends CrudRepository<FidoUser,Long> {

    @Query("SELECT fu FROM FidoUser fu LEFT JOIN  fu.user u  LEFT JOIN fu.fidoCredentials c  WHERE u.email= :email")
    Optional<FidoUser> findFidoUserDetailsByMail(@Param("email") final String email);





}
