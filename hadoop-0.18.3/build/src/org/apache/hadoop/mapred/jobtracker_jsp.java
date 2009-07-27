package org.apache.hadoop.mapred;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.dfs.JspHelper;

public final class jobtracker_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {


  private static DecimalFormat percentFormat = new DecimalFormat("##0.00");

  public void generateJobTable(JspWriter out, String label, Vector jobs, int refresh) throws IOException {
      out.print("<center>\n");
      out.print("<table border=\"2\" cellpadding=\"5\" cellspacing=\"2\">\n");
      out.print("<tr><td align=\"center\" colspan=\"9\"><b>" + label + " Jobs </b></td></tr>\n");
      if (jobs.size() > 0) {
        out.print("<tr><td><b>Jobid</b></td><td><b>User</b></td>");
        out.print("<td><b>Name</b></td>");
        out.print("<td><b>Map % Complete</b></td>");
        out.print("<td><b>Map Total</b></td>");
        out.print("<td><b>Maps Completed</b></td>");
        out.print("<td><b>Reduce % Complete</b></td>");
        out.print("<td><b>Reduce Total</b></td>");
        out.print("<td><b>Reduces Completed</b></td></tr>\n");
        for (Iterator it = jobs.iterator(); it.hasNext(); ) {
          JobInProgress job = (JobInProgress) it.next();
          JobProfile profile = job.getProfile();
          JobStatus status = job.getStatus();
          JobID jobid = profile.getJobID();

          int desiredMaps = job.desiredMaps();
          int desiredReduces = job.desiredReduces();
          int completedMaps = job.finishedMaps();
          int completedReduces = job.finishedReduces();
          String name = profile.getJobName();

          out.print("<tr><td><a href=\"jobdetails.jsp?jobid=" + jobid + 
                     "&refresh=" + refresh + "\">" +
                     jobid + "</a></td>" +
                  "<td>" + profile.getUser() + "</td>" 
                    + "<td>" + ("".equals(name) ? "&nbsp;" : name) + "</td>" + 
                    "<td>" + 
                    StringUtils.formatPercent(status.mapProgress(),2) +
                    JspHelper.percentageGraph(status.mapProgress()  * 100, 80) +
                    "</td><td>" + 
                    desiredMaps + "</td><td>" + completedMaps + "</td><td>" + 
                    StringUtils.formatPercent(status.reduceProgress(),2) +
                    JspHelper.percentageGraph(status.reduceProgress() * 100, 80) +
                    "</td><td>" + 
                    desiredReduces + "</td><td> " + completedReduces + 
                    "</td></tr>\n");
        }
      } else {
        out.print("<tr><td align=\"center\" colspan=\"8\"><i>none</i></td></tr>\n");
      }
      out.print("</table>\n");
      out.print("</center>\n");
  }

  public void generateSummaryTable(JspWriter out,
                                   JobTracker tracker) throws IOException {
    ClusterStatus status = tracker.getClusterStatus();
    String tasksPerNode = status.getTaskTrackers() > 0 ?
      percentFormat.format(((double)(status.getMaxMapTasks() +
                      status.getMaxReduceTasks())) / status.getTaskTrackers()):
      "-";
    out.print("<table border=\"2\" cellpadding=\"5\" cellspacing=\"2\">\n"+
              "<tr><th>Maps</th><th>Reduces</th>" + 
              "<th>Total Submissions</th>" +
              "<th>Nodes</th><th>Map Task Capacity</th>" +
              "<th>Reduce Task Capacity</th><th>Avg. Tasks/Node</th></tr>\n");
    out.print("<tr><td>" + status.getMapTasks() + "</td><td>" +
              status.getReduceTasks() + "</td><td>" + 
              tracker.getTotalSubmissions() +
              "</td><td><a href=\"machines.jsp\">" +
              status.getTaskTrackers() +
              "</a></td><td>" + status.getMaxMapTasks() +
              "</td><td>" + status.getMaxReduceTasks() +
              "</td><td>" + tasksPerNode +
              "</td></tr></table>\n");
  }
  private static java.util.Vector _jspx_dependants;

  public java.util.List getDependants() {
    return _jspx_dependants;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    JspFactory _jspxFactory = null;
    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      _jspxFactory = JspFactory.getDefaultFactory();
      response.setContentType("text/html; charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write('\n');

  JobTracker tracker = (JobTracker) application.getAttribute("job.tracker");
  String trackerName = 
           StringUtils.simpleHostname(tracker.getJobTrackerMachine());

      out.write('\n');
      out.write("\n\n\n<html>\n<head>\n<title>");
      out.print( trackerName );
      out.write(" Hadoop Map/Reduce Administration</title>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/hadoop.css\">\n</head>\n<body>\n<h1>");
      out.print( trackerName );
      out.write(" Hadoop Map/Reduce Administration</h1>\n\n<b>State:</b> ");
      out.print( tracker.getClusterStatus().getJobTrackerState() );
      out.write("<br>\n<b>Started:</b> ");
      out.print( new Date(tracker.getStartTime()));
      out.write("<br>\n<b>Version:</b> ");
      out.print( VersionInfo.getVersion());
      out.write(",\n                r");
      out.print( VersionInfo.getRevision());
      out.write("<br>\n<b>Compiled:</b> ");
      out.print( VersionInfo.getDate());
      out.write(" by \n                 ");
      out.print( VersionInfo.getUser());
      out.write("<br>\n<b>Identifier:</b> ");
      out.print( tracker.getTrackerIdentifier());
      out.write("<br>                 \n                   \n<hr>\n<h2>Cluster Summary</h2>\n<center>\n");
 
   generateSummaryTable(out, tracker); 

      out.write("\n</center>\n<hr>\n\n<h2>Running Jobs</h2>\n");

    generateJobTable(out, "Running", tracker.runningJobs(), 30);

      out.write("\n<hr>\n\n<h2>Completed Jobs</h2>\n");

    generateJobTable(out, "Completed", tracker.completedJobs(), 0);

      out.write("\n\n<hr>\n\n<h2>Failed Jobs</h2>\n");

    generateJobTable(out, "Failed", tracker.failedJobs(), 0);

      out.write("\n\n<hr>\n\n<h2>Local logs</h2>\n<a href=\"logs/\">Log</a> directory, <a href=\"jobhistory.jsp\">\nJob Tracker History</a>\n\n");

out.println(ServletUtil.htmlFooter());

      out.write('\n');
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
