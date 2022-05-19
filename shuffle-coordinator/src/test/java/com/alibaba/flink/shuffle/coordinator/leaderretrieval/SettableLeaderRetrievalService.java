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

package com.alibaba.flink.shuffle.coordinator.leaderretrieval;

import com.alibaba.flink.shuffle.coordinator.highavailability.LeaderInformation;
import com.alibaba.flink.shuffle.coordinator.highavailability.LeaderRetrievalListener;
import com.alibaba.flink.shuffle.coordinator.highavailability.LeaderRetrievalService;

import static com.alibaba.flink.shuffle.common.utils.CommonUtils.checkNotNull;

/**
 * {@link LeaderRetrievalService} implementation which directly forwards calls of notifyListener to
 * the listener.
 */
public class SettableLeaderRetrievalService implements LeaderRetrievalService {

    private LeaderInformation leaderInfo;

    private LeaderRetrievalListener listener;

    public SettableLeaderRetrievalService() {
        this(LeaderInformation.empty());
    }

    public SettableLeaderRetrievalService(LeaderInformation leaderInfo) {
        this.leaderInfo = leaderInfo;
    }

    @Override
    public synchronized void start(LeaderRetrievalListener listener) throws Exception {
        this.listener = checkNotNull(listener);

        if (leaderInfo != LeaderInformation.empty()) {
            listener.notifyLeaderAddress(leaderInfo);
        }
    }

    @Override
    public void stop() throws Exception {}

    public synchronized void notifyListener(LeaderInformation leaderInfo) {
        this.leaderInfo = leaderInfo;

        if (listener != null) {
            listener.notifyLeaderAddress(leaderInfo);
        }
    }
}
