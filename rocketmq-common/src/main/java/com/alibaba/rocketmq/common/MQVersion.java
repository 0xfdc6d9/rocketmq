/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.common;

/**
 * ��������汾��Ϣ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class MQVersion {
    // TODO ÿ�η����汾��Ҫ�޸Ĵ˴��汾��
    public static final int CurrentVersion = Version.V3_0_0_SNAPSHOT.ordinal();

    enum Version {
        V3_0_0_SNAPSHOT,
        V3_0_0_ALPHA1,
        V3_0_0,
        V3_0_1_SNAPSHOT,
        V3_0_1,
        V3_0_2_SNAPSHOT,
        V3_0_2,
        V3_0_3_SNAPSHOT,
        V3_0_3,
        V3_0_4_SNAPSHOT,
        V3_0_4,
    }


    public static String getVersionDesc(int value) {
        Version v = Version.values()[value];
        return v.name();
    }


    public static Version value2Version(int value) {
        return Version.values()[value];
    }
}
