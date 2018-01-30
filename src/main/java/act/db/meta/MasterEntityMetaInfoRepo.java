package act.db.meta;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.app.App;
import act.app.event.SysEventId;
import act.db.DB;
import act.util.ClassInfoRepository;

import java.util.HashMap;
import java.util.Map;

public class MasterEntityMetaInfoRepo extends EntityMetaInfoRepo {

    // map entity meta info repo to db id
    private Map<String, EntityMetaInfoRepo> regions = new HashMap<>();

    public MasterEntityMetaInfoRepo(final App app) {
        super(app);
        final MasterEntityMetaInfoRepo me = this;
        app.jobManager().on(SysEventId.CLASS_LOADED, new Runnable() {
            @Override
            public void run() {
                final ClassInfoRepository classRepo = app.classLoader().classInfoRepository();
                for (Map.Entry<String, EntityClassMetaInfo> entry : lookup.entrySet()) {
                    Class<?> entityClass = app.classForName(entry.getKey());
                    EntityClassMetaInfo info = entry.getValue();
                    info.mergeFromMappedSuperClasses(classRepo, me);
                    register(entityClass, info);
                    DB db = entityClass.getAnnotation(DB.class);
                    String dbId = (null == db ? DB.DEFAULT : db.value()).toUpperCase();
                    EntityMetaInfoRepo repo = regions.get(dbId);
                    if (null == repo) {
                        repo = new EntityMetaInfoRepo(app);
                        regions.put(dbId, repo);
                    }
                    repo.register(entityClass, info);
                }
            }
        });
    }

    public EntityMetaInfoRepo forDefaultDb() {
        return forDb(DB.DEFAULT);
    }

    public EntityMetaInfoRepo forDb(String dbId) {
        return regions.get(null == dbId ? DB.DEFAULT : dbId.toUpperCase());
    }
}
