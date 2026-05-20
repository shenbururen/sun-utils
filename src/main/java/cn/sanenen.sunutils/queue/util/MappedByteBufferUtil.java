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
import cn.hutool.log.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author sun
 */
public class MappedByteBufferUtil {
    private static final Log log = Log.get();

    public static void clean(final Object buffer) {
        if (buffer == null) {
            return;
        }
        if (buffer instanceof ByteBuffer && cleanByUnsafe((ByteBuffer) buffer)) {
            return;
        }
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method getCleanerMethod = ReflectUtil.getMethodByName(buffer.getClass(), "cleaner");
                getCleanerMethod.setAccessible(true);
                Object cleaner = getCleanerMethod.invoke(buffer, new Object[0]);
                if (cleaner != null) {
                    Method cleanMethod = ReflectUtil.getMethodByName(cleaner.getClass(), "clean");
                    cleanMethod.invoke(cleaner);
                }
            } catch (Exception e) {
                log.warn("clean mappedByteBuffer failed", e);
            }
            return null;
        });

    }

    private static boolean cleanByUnsafe(ByteBuffer buffer) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object unsafe = theUnsafeField.get(null);
            Method invokeCleanerMethod = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            invokeCleanerMethod.invoke(unsafe, buffer);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
