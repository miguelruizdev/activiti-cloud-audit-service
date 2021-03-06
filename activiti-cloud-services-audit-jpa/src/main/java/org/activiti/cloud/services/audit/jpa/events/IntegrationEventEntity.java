/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.audit.jpa.events;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class IntegrationEventEntity extends AuditEventEntity {

    private String integrationContextId;

    private String flowNodeId;

    public IntegrationEventEntity() {
    }

    public IntegrationEventEntity(String eventId,
                                  Long timestamp,
                                  String eventType) {
        super(eventId,
              timestamp,
              eventType);
    }

    public IntegrationEventEntity(String eventId,
                                  Long timestamp,
                                  String eventType,
                                  String integrationContextId,
                                  String flowNodeId) {
        super(eventId,
              timestamp,
              eventType);
        this.integrationContextId = integrationContextId;
        this.flowNodeId = flowNodeId;
    }

    public String getIntegrationContextId() {
        return integrationContextId;
    }

    public String getFlowNodeId() {
        return flowNodeId;
    }
}
