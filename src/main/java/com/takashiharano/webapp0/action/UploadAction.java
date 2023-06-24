/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.action;

import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.libutil.FileUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.Log;

public class UploadAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();
    AppManager appManager = context.getAppManager();
    String uploadPath = appManager.getAppWorkspacePath() + "/upload/";

    if (!FileUtil.exists(uploadPath)) {
      FileUtil.mkdir(uploadPath);
    }

    StringBuilder sb = new StringBuilder("Uploaded.\n");

    Collection<Part> parts = request.getParts();
    for (Part part : parts) {
      String name = part.getName();
      if (name.equals("files")) {
        String fileName = getFilename(part);
        if ((fileName != null) && !fileName.equals("")) {
          long time = new Date().getTime();
          String newFileName = time + "_" + fileName;
          String filePath = uploadPath + newFileName;
          Log.i("Write uploaded file: " + filePath);
          part.write(filePath);
          sb.append(filePath).append("\n");
        }
      }
    }

    request.setAttribute("result", sb.toString());
    request.getRequestDispatcher("result.jsp").forward(request, response);
  }

  private String getFilename(Part part) {
    String contentDisposition = part.getHeader("Content-Disposition");
    if (contentDisposition == null) {
      return null;
    }

    String[] cds = contentDisposition.split(";");
    for (String cd : cds) {
      if (cd.trim().startsWith("filename")) {
        String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
        if (fileName.contains("\\")) {
          fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        return fileName;
      }
    }

    return null;
  }

}
