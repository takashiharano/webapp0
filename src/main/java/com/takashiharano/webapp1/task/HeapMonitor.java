package com.takashiharano.webapp1.task;

import java.io.IOException;

import com.takashiharano.util.CsvBuilder;
import com.takashiharano.util.FileUtil;
import com.takashiharano.util.HeapInfo;
import com.takashiharano.util.Log;
import com.takashiharano.webapp1.AppManager;

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
    Log.d(now + ": Heap: total=" + total + " / used=" + used + "(" + percent + "%) / free=" + free + " / max=" + max);

    CsvBuilder csvBuilder = new CsvBuilder();
    csvBuilder.append(total);
    csvBuilder.append(used);
    csvBuilder.append(percent);
    csvBuilder.append(free);
    csvBuilder.append(max);

    String path = AppManager.getAppWorkspacePath() + "/" + "heap.txt";
    String info = csvBuilder.toString();
    try {
      FileUtil.appendLine(path, info, 5);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
