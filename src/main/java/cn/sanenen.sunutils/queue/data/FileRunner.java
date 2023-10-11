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
package cn.sanenen.sunutils.queue.data;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 持久化文件 创建和删除线程类
 * @author sun
 */
public class FileRunner implements Runnable {
    private static final Log log = Log.get();
    // 删除队列
    private static final Queue<String> deleteQueue = new ConcurrentLinkedQueue<>();
    
    private volatile boolean keepRunning = true;

    public static void addDeleteFile(String path) {
        deleteQueue.add(path);
    }

    @Override
    public void run() {
        String filePath;
        while (keepRunning) {
            filePath = deleteQueue.poll();
            if (filePath == null) {
                ThreadUtil.sleep(10);
                continue;
            }
            File delFile = new File(filePath);
            try {
                boolean delete = delFile.delete();
                if (!delete){
                    addDeleteFile(filePath);
                    log.error("删除文件失败,重新删除。path:{}",filePath);
                }
            } catch (Exception e) {
                log.error("删除文件失败",e);
                ThreadUtil.sleep(100);
            }
        }
    }

    public void close(){
        keepRunning = false;
    }
}
