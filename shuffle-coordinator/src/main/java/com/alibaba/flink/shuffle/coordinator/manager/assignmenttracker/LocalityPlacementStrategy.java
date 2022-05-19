/*
 * Copyright 2021 The Flink Remote Shuffle Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.flink.shuffle.coordinator.manager.assignmenttracker;

import com.alibaba.flink.shuffle.core.storage.DataPartitionFactory;
import com.alibaba.flink.shuffle.core.storage.StorageSpaceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * This strategy always tries to select the local shuffle worker first, if the local worker fails to
 * satisfy some constraints, for example, no enough free space, it will select other remote workers
 * in round-robin order.
 */
class LocalityPlacementStrategy extends BasePartitionPlacementStrategy {

    private final PartitionPlacementStrategy defaultPlacementStrategy;

    private final Map<String, WorkerStatus> workersByHostName = new HashMap<>();

    LocalityPlacementStrategy(long minReservedSpaceBytes, long maxUsableSpaceBytes) {
        super(minReservedSpaceBytes, maxUsableSpaceBytes);
        this.defaultPlacementStrategy =
                new RoundRobinPlacementStrategy(minReservedSpaceBytes, maxUsableSpaceBytes);
    }

    @Override
    public WorkerStatus[] selectNextWorker(PartitionPlacementContext partitionPlacementContext)
            throws ShuffleResourceAllocationException {
        DataPartitionFactory partitionFactory = partitionPlacementContext.getPartitionFactory();
        WorkerStatus selectedWorker = null;
        String taskLocation = partitionPlacementContext.getTaskLocation();
        if (taskLocation != null && workersByHostName.containsKey(taskLocation)) {
            selectedWorker = workersByHostName.get(taskLocation);
        }

        if (selectedWorker != null) {
            StorageSpaceInfo storageSpaceInfo =
                    selectedWorker.getStorageSpaceInfo(partitionFactory.getClass().getName());
            if (isStorageSpaceValid(partitionFactory, storageSpaceInfo)) {
                return PlacementUtils.singleElementWorkerArray(selectedWorker);
            }
        }
        return defaultPlacementStrategy.selectNextWorker(partitionPlacementContext);
    }

    @Override
    public void addWorker(WorkerStatus worker) {
        defaultPlacementStrategy.addWorker(worker);
        workersByHostName.put(worker.getWorkerHostName(), worker);
    }

    @Override
    public void removeWorker(WorkerStatus worker) {
        defaultPlacementStrategy.removeWorker(worker);
        workersByHostName.remove(worker.getWorkerHostName(), worker);
    }
}
