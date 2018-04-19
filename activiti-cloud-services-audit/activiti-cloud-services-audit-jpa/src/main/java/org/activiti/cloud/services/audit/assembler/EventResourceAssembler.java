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

package org.activiti.cloud.services.audit.assembler;

import org.activiti.cloud.services.audit.resources.EventResource;
import org.activiti.cloud.services.audit.ProcessEngineEventsController;
import org.activiti.cloud.services.audit.events.ProcessEngineEventEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class EventResourceAssembler extends ResourceAssemblerSupport<ProcessEngineEventEntity, EventResource> {

    public EventResourceAssembler(){
        super(ProcessEngineEventsController.class,EventResource.class);
    }

    @Override
    public EventResource toResource(ProcessEngineEventEntity entity) {
        Link selfRel = linkTo(methodOn(ProcessEngineEventsController.class).findById(entity.getId())).withSelfRel();
        return new EventResource(entity, selfRel);
    }

}