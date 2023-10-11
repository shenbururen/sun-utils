/*
 *  Copyright 2011 sunli [sunli1223@gmail.com][weibo.com@sunli1223]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.sanenen.sunutils.queue.util;

import cn.hutool.core.util.ReflectUtil;
import sun.misc.Cleaner;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author sun
 */
public class MappedByteBufferUtil {
    public static void clean(final Object buffer) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method getCleanerMethod = ReflectUtil.getMethodByName(buffer.getClass(), "cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
                cleaner.clean();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

    }
}
