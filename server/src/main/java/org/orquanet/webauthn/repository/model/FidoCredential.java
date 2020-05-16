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

package org.orquanet.webauthn.repository.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@Getter

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(name = "wa_user_credential",schema = "app")
public class FidoCredential {


    @SequenceGenerator(name = "wa_id", schema = "app",sequenceName = "wa_credential_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wa_id")
    @Id
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wa_id")
    private FidoUser fidoUser;

    @Column(name= "wa_credential_id")
    private String credentialId;

    @Column(name = "wa_credential_creation")
    private Date created;

    @Column(name= "wa_cose_algorithm")
    private int coseAlgorithm;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name="wa_credential_key")
    private byte[] publicKey;

}
