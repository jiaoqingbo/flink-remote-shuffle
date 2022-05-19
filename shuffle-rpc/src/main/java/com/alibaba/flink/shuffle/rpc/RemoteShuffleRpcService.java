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

package com.alibaba.flink.shuffle.rpc;

import org.apache.flink.runtime.rpc.RpcService;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** {@link RpcService} for remote shuffle service. */
public interface RemoteShuffleRpcService extends RpcService {

    Executor getExecutor();

    <F extends Serializable, C extends RemoteShuffleFencedRpcGateway<F>>
            CompletableFuture<C> connectTo(String address, F fencingToken, Class<C> clazz);

    <C extends RemoteShuffleRpcGateway> CompletableFuture<C> connectTo(
            String address, Class<C> clazz);
}
