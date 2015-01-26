/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.server.logic.init;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.syncope.server.provisioning.api.job.PushJob;
import org.apache.syncope.server.provisioning.api.job.SyncJob;
import org.apache.syncope.server.provisioning.api.job.TaskJob;
import org.apache.syncope.server.provisioning.api.propagation.PropagationActions;
import org.apache.syncope.server.provisioning.api.sync.PushActions;
import org.apache.syncope.server.provisioning.api.sync.SyncActions;
import org.apache.syncope.server.provisioning.api.sync.SyncCorrelationRule;
import org.apache.syncope.server.logic.report.Reportlet;
import org.apache.syncope.server.persistence.api.SyncopeLoader;
import org.apache.syncope.server.persistence.api.attrvalue.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Cache class names for all implementations of Syncope interfaces found in classpath, for later usage.
 */
@Component
public class ImplementationClassNamesLoader implements SyncopeLoader {

    public enum Type {

        REPORTLET,
        TASKJOB,
        PROPAGATION_ACTIONS,
        SYNC_ACTIONS,
        PUSH_ACTIONS,
        SYNC_CORRELATION_RULE,
        PUSH_CORRELATION_RULE,
        VALIDATOR

    }

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImplementationClassNamesLoader.class);

    @Autowired
    private ResourcePatternResolver resResolver;

    private Map<Type, Set<String>> classNames;

    @Override
    public Integer getPriority() {
        return 400;
    }

    @Override
    public void load() {
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

        classNames = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            classNames.put(type, new HashSet<String>());
        }

        try {
            for (Resource resource : resResolver.getResources("classpath*:**/*.class")) {
                ClassMetadata metadata = factory.getMetadataReader(resource).getClassMetadata();

                try {
                    Class<?> clazz = ClassUtils.forName(metadata.getClassName(), ClassUtils.getDefaultClassLoader());
                    Set<Class<?>> interfaces = ClassUtils.getAllInterfacesForClassAsSet(clazz);

                    if (interfaces.contains(Reportlet.class) && !metadata.isAbstract()) {
                        classNames.get(Type.REPORTLET).add(clazz.getName());
                    }

                    if ((interfaces.contains(TaskJob.class))
                            && !metadata.isAbstract()
                            && !SyncJob.class.getName().equals(metadata.getClassName())
                            && !PushJob.class.getName().equals(metadata.getClassName())) {

                        classNames.get(Type.TASKJOB).add(metadata.getClassName());
                    }

                    if (interfaces.contains(SyncActions.class) && !metadata.isAbstract()) {
                        classNames.get(Type.SYNC_ACTIONS).add(metadata.getClassName());
                    }

                    if (interfaces.contains(PushActions.class) && !metadata.isAbstract()) {
                        classNames.get(Type.PUSH_ACTIONS).add(metadata.getClassName());
                    }

                    if (interfaces.contains(SyncCorrelationRule.class) && !metadata.isAbstract()) {
                        classNames.get(Type.SYNC_CORRELATION_RULE).add(metadata.getClassName());
                    }

                    // TODO: SYNCOPE-631
                    /* if (interfaces.contains(PushCorrelationRule.class) && !metadata.isAbstract()) {
                     * classNames.get(Type.PUSH_CORRELATION_RULES).add(metadata.getClassName());
                     * } */
                    if (interfaces.contains(PropagationActions.class) && !metadata.isAbstract()) {
                        classNames.get(Type.PROPAGATION_ACTIONS).add(metadata.getClassName());
                    }

                    if (interfaces.contains(Validator.class) && !metadata.isAbstract()) {
                        classNames.get(Type.VALIDATOR).add(metadata.getClassName());
                    }
                } catch (ClassNotFoundException e) {
                    LOG.warn("Could not load class {}", metadata.getClassName());
                } catch (LinkageError e) {
                    LOG.warn("Could not link class {}", metadata.getClassName());
                }
            }
        } catch (IOException e) {
            LOG.error("While searching for implementatiom classes", e);
        }

        classNames = Collections.unmodifiableMap(classNames);

        LOG.debug("Implementation classes found: {}", classNames);
    }

    public Set<String> getClassNames(final Type type) {
        return classNames.get(type);
    }
}