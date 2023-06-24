/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
package com.takashiharano.webapp0.task;

import java.io.IOException;

import com.libutil.CsvBuilder;
import com.libutil.FileUtil;
import com.libutil.HeapInfo;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.util.Log;

public class HeapMonitor extends IntervalTask {

  @Override
  public void exec() {
    long now = System.currentTimeMillis();

    HeapInfo heapInfo = new HeapInfo();
    long total = heapInfo.getTotal();
    long used = heapInfo.getUsed();
    long free = heapInfo.getFree();
    long max = heapInfo.getMax();
    String percent = heapInfo.getPercent();
    Log.i(now + ": Heap: total=" + total + " / used=" + used + "(" + percent + "%) / free=" + free + " / max=" + max);

    CsvBuilder csvBuilder = new CsvBuilder();
    csvBuilder.append(total);
    csvBuilder.append(used);
    csvBuilder.append(percent);
    csvBuilder.append(free);
    csvBuilder.append(max);

    String path = AppManager.getInstance().getAppWorkspacePath() + "/" + "heap.txt";
    String info = csvBuilder.toString();
    try {
      FileUtil.appendLine(path, info, 5);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
