/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.dfs;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.dfs.DistributedFileSystem.DiskStatus;
import org.apache.hadoop.dfs.FSConstants.UpgradeAction;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.shell.Command;
import org.apache.hadoop.fs.shell.CommandFormat;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.util.ToolRunner;

/**
 * This class provides some DFS administrative access.
 */
public class DFSAdmin extends FsShell {

  /**
   * An abstract class for the execution of a file system command
   */
  abstract private static class DFSAdminCommand extends Command {
    /** Constructor */
    public DFSAdminCommand(FileSystem fs) {
      super(fs);
      if (!(fs instanceof DistributedFileSystem)) {
        throw new IllegalArgumentException("FileSystem " + fs.getUri() + 
            " is not a distributed file system");
      }
    }
  }
  
  /** A class that supports command clearQuota */
  private static class ClearQuotaCommand extends DFSAdminCommand {
    private static final String NAME = "clrQuota";
    private static final String USAGE = "-"+NAME+" <dirname>...<dirname>";
    private static final String DESCRIPTION = USAGE + ": " +
    "\tClear the quota for each directory <dirName>.\n" +
    "\t\tBest effort for the directory. with fault reported if\n" +
    "\t\t1. the directory does not exist or is a file, or\n" +
    "\t\t2. user is not an administrator.\n" +
    "\t\tIt does not fault if the directory has no quota.";
    
    /** Constructor */
    ClearQuotaCommand(String[] args, int pos, FileSystem fs) {
      super(fs);
      CommandFormat c = new CommandFormat(NAME, 1, Integer.MAX_VALUE);
      List<String> parameters = c.parse(args, pos);
      this.args = parameters.toArray(new String[parameters.size()]);
    }
    
    /** Check if a command is the clrQuota command
     * 
     * @param cmd A string representation of a command starting with "-"
     * @return true if this is a clrQuota command; false otherwise
     */
    public static boolean matches(String cmd) {
      return ("-"+NAME).equals(cmd); 
    }

    @Override
    public String getCommandName() {
      return NAME;
    }

    @Override
    public void run(Path path) throws IOException {
      ((DistributedFileSystem)fs).clearQuota(path);
    }
  }
  
  /** A class that supports command setQuota */
  private static class SetQuotaCommand extends DFSAdminCommand {
    private static final String NAME = "setQuota";
    private static final String USAGE =
      "-"+NAME+" <quota> <dirname>...<dirname>";
    private static final String DESCRIPTION = 
      "-setQuota <quota> <dirname>...<dirname>: " +
      "\tSet the quota <quota> for each directory <dirName>.\n" + 
      "\t\tThe directory quota is a long integer that puts a hard limit " +
      "on the number of names in the directory tree\n" +
      "\t\tBest effort for the directory, with faults reported if\n" +
      "\t\t1. N is not a positive integer, or\n" +
      "\t\t2. user is not an administrator, or\n" +
      "\t\t3. the directory does not exist or is a file, or\n" +
      "\t\t4. the directory would immediately exceed the new quota.";
    
    private final long quota; // the quota to be set
    
    /** Constructor */
    SetQuotaCommand(String[] args, int pos, FileSystem fs) {
      super(fs);
      CommandFormat c = new CommandFormat(NAME, 2, Integer.MAX_VALUE);
      List<String> parameters = c.parse(args, pos);
      this.quota = Long.parseLong(parameters.remove(0));
      this.args = parameters.toArray(new String[parameters.size()]);
    }
    
    /** Check if a command is the setQuota command
     * 
     * @param cmd A string representation of a command starting with "-"
     * @return true if this is a count command; false otherwise
     */
    public static boolean matches(String cmd) {
      return ("-"+NAME).equals(cmd); 
    }

    @Override
    public String getCommandName() {
      return NAME;
    }

    @Override
    public void run(Path path) throws IOException {
      ((DistributedFileSystem)fs).setQuota(path, quota);
    }
  }
  
  /**
   * Construct a DFSAdmin object.
   */
  public DFSAdmin() {
    this(null);
  }

  /**
   * Construct a DFSAdmin object.
   */
  public DFSAdmin(Configuration conf) {
    super(conf);
  }
  
  /**
   * Gives a report on how the FileSystem is doing.
   * @exception IOException if the filesystem does not exist.
   */
  public void report() throws IOException {
    if (fs instanceof DistributedFileSystem) {
      DistributedFileSystem dfs = (DistributedFileSystem) fs;
      DiskStatus ds = dfs.getDiskStatus();
      long raw = ds.getCapacity();
      long rawUsed = ds.getDfsUsed();
      long remaining = ds.getRemaining();
      long used = dfs.getUsed();
      boolean mode = dfs.setSafeMode(FSConstants.SafeModeAction.SAFEMODE_GET);
      UpgradeStatusReport status = 
                      dfs.distributedUpgradeProgress(UpgradeAction.GET_STATUS);

      if (mode) {
        System.out.println("Safe mode is ON");
      }
      if (status != null) {
        System.out.println(status.getStatusText(false));
      }
      System.out.println("Total raw bytes: " + raw
                         + " (" + byteDesc(raw) + ")");
      System.out.println("Remaining raw bytes: " + remaining
          + " (" + byteDesc(remaining) + ")");
      System.out.println("Used raw bytes: " + rawUsed
                         + " (" + byteDesc(rawUsed) + ")");
      System.out.println("% used: "
                         + limitDecimalTo2(((1.0 * rawUsed) / raw) * 100)
                         + "%");
      System.out.println();
      System.out.println("Total effective bytes: " + used
                         + " (" + byteDesc(used) + ")");
      System.out.println("Effective replication multiplier: "
                         + (1.0 * rawUsed / used));

      System.out.println("-------------------------------------------------");
      DatanodeInfo[] info = dfs.getDataNodeStats();
      System.out.println("Datanodes available: " + info.length);
      System.out.println();
      for (int i = 0; i < info.length; i++) {
        System.out.println(info[i].getDatanodeReport());
        System.out.println();
      }
    }
  }

  /**
   * Safe mode maintenance command.
   * Usage: java DFSAdmin -safemode [enter | leave | get]
   * @param argv List of of command line parameters.
   * @param idx The index of the command that is being processed.
   * @exception IOException if the filesystem does not exist.
   */
  public void setSafeMode(String[] argv, int idx) throws IOException {
    if (!(fs instanceof DistributedFileSystem)) {
      System.err.println("FileSystem is " + fs.getUri());
      return;
    }
    if (idx != argv.length - 1) {
      printUsage("-safemode");
      return;
    }
    FSConstants.SafeModeAction action;
    Boolean waitExitSafe = false;

    if ("leave".equalsIgnoreCase(argv[idx])) {
      action = FSConstants.SafeModeAction.SAFEMODE_LEAVE;
    } else if ("enter".equalsIgnoreCase(argv[idx])) {
      action = FSConstants.SafeModeAction.SAFEMODE_ENTER;
    } else if ("get".equalsIgnoreCase(argv[idx])) {
      action = FSConstants.SafeModeAction.SAFEMODE_GET;
    } else if ("wait".equalsIgnoreCase(argv[idx])) {
      action = FSConstants.SafeModeAction.SAFEMODE_GET;
      waitExitSafe = true;
    } else {
      printUsage("-safemode");
      return;
    }
    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    boolean inSafeMode = dfs.setSafeMode(action);

    //
    // If we are waiting for safemode to exit, then poll and
    // sleep till we are out of safemode.
    //
    if (waitExitSafe) {
      while (inSafeMode) {
        try {
          Thread.sleep(5000);
        } catch (java.lang.InterruptedException e) {
          throw new IOException("Wait Interrupted");
        }
        inSafeMode = dfs.setSafeMode(action);
      }
    }

    System.out.println("Safe mode is " + (inSafeMode ? "ON" : "OFF"));
  }

  /**
   * Command to ask the namenode to reread the hosts and excluded hosts 
   * file.
   * Usage: java DFSAdmin -refreshNodes
   * @exception IOException 
   */
  public int refreshNodes() throws IOException {
    int exitCode = -1;

    if (!(fs instanceof DistributedFileSystem)) {
      System.err.println("FileSystem is " + fs.getName());
      return exitCode;
    }

    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    dfs.refreshNodes();
    exitCode = 0;
   
    return exitCode;
  }

  private void printHelp(String cmd) {
    String summary = "hadoop dfsadmin is the command to execute DFS administrative commands.\n" +
      "The full syntax is: \n\n" +
      "hadoop dfsadmin [-report] [-safemode <enter | leave | get | wait>]\n" +
      "\t[-refreshNodes]\n" +
      "\t[" + SetQuotaCommand.USAGE + "]\n" +
      "\t[" + ClearQuotaCommand.USAGE +"]\n" +
      "\t[-help [cmd]]\n";

    String report ="-report: \tReports basic filesystem information and statistics.\n";
        
    String safemode = "-safemode <enter|leave|get|wait>:  Safe mode maintenance command.\n" + 
      "\t\tSafe mode is a Namenode state in which it\n" +
      "\t\t\t1.  does not accept changes to the name space (read-only)\n" +
      "\t\t\t2.  does not replicate or delete blocks.\n" +
      "\t\tSafe mode is entered automatically at Namenode startup, and\n" +
      "\t\tleaves safe mode automatically when the configured minimum\n" +
      "\t\tpercentage of blocks satisfies the minimum replication\n" +
      "\t\tcondition.  Safe mode can also be entered manually, but then\n" +
      "\t\tit can only be turned off manually as well.\n";

    String refreshNodes = "-refreshNodes: \tRe-read the hosts and exclude files to update the set\n" +
      "\t\tof Datanodes that are allowed to connect to the Namenode\n" +
      "\t\tand those that should be decommissioned of recommissioned.\n";

    String finalizeUpgrade = "-finalizeUpgrade: Finalize upgrade of DFS.\n" +
      "\t\tDatanodes delete their previous version working directories,\n" +
      "\t\tfollowed by Namenode doing the same.\n" + 
      "\t\tThis completes the upgrade process.\n";

    String upgradeProgress = "-upgradeProgress <status|details|force>: \n" +
      "\t\trequest current distributed upgrade status, \n" +
      "\t\ta detailed status or force the upgrade to proceed.\n";

    String metaSave = "-metasave <filename>: \tSave Namenode's primary data structures\n" +
      "\t\tto <filename> in the directory specified by hadoop.log.dir property.\n" +
      "\t\t<filename> will contain one line for each of the following\n" +
      "\t\t\t1. Datanodes heart beating with Namenode\n" +
      "\t\t\t2. Blocks waiting to be replicated\n" +
      "\t\t\t3. Blocks currrently being replicated\n" +
      "\t\t\t4. Blocks waiting to be deleted\n";

    String help = "-help [cmd]: \tDisplays help for the given command or all commands if none\n" +
      "\t\tis specified.\n";

    if ("report".equals(cmd)) {
      System.out.println(report);
    } else if ("safemode".equals(cmd)) {
      System.out.println(safemode);
    } else if ("refreshNodes".equals(cmd)) {
      System.out.println(refreshNodes);
    } else if ("finalizeUpgrade".equals(cmd)) {
      System.out.println(finalizeUpgrade);
    } else if ("upgradeProgress".equals(cmd)) {
      System.out.println(upgradeProgress);
    } else if ("metasave".equals(cmd)) {
      System.out.println(metaSave);
    } else if (SetQuotaCommand.matches(cmd)) {
      System.out.println(SetQuotaCommand.DESCRIPTION);
    } else if (ClearQuotaCommand.matches(cmd)) {
      System.out.println(ClearQuotaCommand.DESCRIPTION);
    } else if ("help".equals(cmd)) {
      System.out.println(help);
    } else {
      System.out.println(summary);
      System.out.println(report);
      System.out.println(safemode);
      System.out.println(refreshNodes);
      System.out.println(finalizeUpgrade);
      System.out.println(upgradeProgress);
      System.out.println(metaSave);
      System.out.println(SetQuotaCommand.DESCRIPTION);
      System.out.println(ClearQuotaCommand.DESCRIPTION);
      System.out.println(help);
      System.out.println();
      ToolRunner.printGenericCommandUsage(System.out);
    }

  }


  /**
   * Command to ask the namenode to finalize previously performed upgrade.
   * Usage: java DFSAdmin -finalizeUpgrade
   * @exception IOException 
   */
  public int finalizeUpgrade() throws IOException {
    int exitCode = -1;

    if (!(fs instanceof DistributedFileSystem)) {
      System.out.println("FileSystem is " + fs.getUri());
      return exitCode;
    }

    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    dfs.finalizeUpgrade();
    exitCode = 0;
   
    return exitCode;
  }

  /**
   * Command to request current distributed upgrade status, 
   * a detailed status, or to force the upgrade to proceed.
   * 
   * Usage: java DFSAdmin -upgradeProgress [status | details | force]
   * @exception IOException 
   */
  public int upgradeProgress(String[] argv, int idx) throws IOException {
    if (!(fs instanceof DistributedFileSystem)) {
      System.out.println("FileSystem is " + fs.getUri());
      return -1;
    }
    if (idx != argv.length - 1) {
      printUsage("-upgradeProgress");
      return -1;
    }

    UpgradeAction action;
    if ("status".equalsIgnoreCase(argv[idx])) {
      action = UpgradeAction.GET_STATUS;
    } else if ("details".equalsIgnoreCase(argv[idx])) {
      action = UpgradeAction.DETAILED_STATUS;
    } else if ("force".equalsIgnoreCase(argv[idx])) {
      action = UpgradeAction.FORCE_PROCEED;
    } else {
      printUsage("-upgradeProgress");
      return -1;
    }

    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    UpgradeStatusReport status = dfs.distributedUpgradeProgress(action);
    String statusText = (status == null ? 
        "There are no upgrades in progress." :
          status.getStatusText(action == UpgradeAction.DETAILED_STATUS));
    System.out.println(statusText);
    return 0;
  }

  /**
   * Dumps DFS data structures into specified file.
   * Usage: java DFSAdmin -metasave filename
   * @param argv List of of command line parameters.
   * @param idx The index of the command that is being processed.
   * @exception IOException if an error accoured wile accessing
   *            the file or path.
   */
  public int metaSave(String[] argv, int idx) throws IOException {
    String pathname = argv[idx];
    DistributedFileSystem dfs = (DistributedFileSystem) fs;
    dfs.metaSave(pathname);
    System.out.println("Created file " + pathname + " on server " +
                       dfs.getUri());
    return 0;
  }

  /**
   * Displays format of commands.
   * @param cmd The command that is being executed.
   */
  private static void printUsage(String cmd) {
    if ("-report".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-report]");
    } else if ("-safemode".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-safemode enter | leave | get | wait]");
    } else if ("-refreshNodes".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-refreshNodes]");
    } else if ("-finalizeUpgrade".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-finalizeUpgrade]");
    } else if ("-upgradeProgress".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-upgradeProgress status | details | force]");
    } else if ("-metasave".equals(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [-metasave filename]");
    } else if (SetQuotaCommand.matches(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " [" + SetQuotaCommand.USAGE+"]");
    } else if (ClearQuotaCommand.matches(cmd)) {
      System.err.println("Usage: java DFSAdmin"
                         + " ["+ClearQuotaCommand.USAGE+"]");
    } else {
      System.err.println("Usage: java DFSAdmin");
      System.err.println("           [-report]");
      System.err.println("           [-safemode enter | leave | get | wait]");
      System.err.println("           [-refreshNodes]");
      System.err.println("           [-finalizeUpgrade]");
      System.err.println("           [-upgradeProgress status | details | force]");
      System.err.println("           [-metasave filename]");
      System.err.println("           ["+SetQuotaCommand.USAGE+"]");
      System.err.println("           ["+ClearQuotaCommand.USAGE+"]");
      System.err.println("           [-help [cmd]]");
      System.err.println();
      ToolRunner.printGenericCommandUsage(System.err);
    }
  }

  /**
   * @param argv The parameters passed to this program.
   * @exception Exception if the filesystem does not exist.
   * @return 0 on success, non zero on error.
   */
  @Override
  public int run(String[] argv) throws Exception {

    if (argv.length < 1) {
      printUsage("");
      return -1;
    }

    int exitCode = -1;
    int i = 0;
    String cmd = argv[i++];

    //
    // verify that we have enough command line parameters
    //
    if ("-safemode".equals(cmd)) {
      if (argv.length != 2) {
        printUsage(cmd);
        return exitCode;
      }
    } else if ("-report".equals(cmd)) {
      if (argv.length != 1) {
        printUsage(cmd);
        return exitCode;
      }
    } else if ("-refreshNodes".equals(cmd)) {
      if (argv.length != 1) {
        printUsage(cmd);
        return exitCode;
      }
    } else if ("-finalizeUpgrade".equals(cmd)) {
      if (argv.length != 1) {
        printUsage(cmd);
        return exitCode;
      }
    } else if ("-upgradeProgress".equals(cmd)) {
        if (argv.length != 2) {
          printUsage(cmd);
          return exitCode;
        }
    } else if ("-metasave".equals(cmd)) {
      if (argv.length != 2) {
        printUsage(cmd);
        return exitCode;
      }
    }
    
    // initialize DFSAdmin
    try {
      init();
    } catch (RPC.VersionMismatch v) {
      System.err.println("Version Mismatch between client and server"
                         + "... command aborted.");
      return exitCode;
    } catch (IOException e) {
      System.err.println("Bad connection to DFS... command aborted.");
      return exitCode;
    }

    exitCode = 0;
    try {
      if ("-report".equals(cmd)) {
        report();
      } else if ("-safemode".equals(cmd)) {
        setSafeMode(argv, i);
      } else if ("-refreshNodes".equals(cmd)) {
        exitCode = refreshNodes();
      } else if ("-finalizeUpgrade".equals(cmd)) {
        exitCode = finalizeUpgrade();
      } else if ("-upgradeProgress".equals(cmd)) {
        exitCode = upgradeProgress(argv, i);
      } else if ("-metasave".equals(cmd)) {
        exitCode = metaSave(argv, i);
      } else if (ClearQuotaCommand.matches(cmd)) {
        exitCode = new ClearQuotaCommand(argv, i, fs).runAll();
      } else if (SetQuotaCommand.matches(cmd)) {
        exitCode = new SetQuotaCommand(argv, i, fs).runAll();
      } else if ("-help".equals(cmd)) {
        if (i < argv.length) {
          printHelp(argv[i]);
        } else {
          printHelp("");
        }
      } else {
        exitCode = -1;
        System.err.println(cmd.substring(1) + ": Unknown command");
        printUsage("");
      }
    } catch (IllegalArgumentException arge) {
      exitCode = -1;
      System.err.println(cmd.substring(1) + ": " + arge.getLocalizedMessage());
      printUsage(cmd);
    } catch (RemoteException e) {
      //
      // This is a error returned by hadoop server. Print
      // out the first line of the error mesage, ignore the stack trace.
      exitCode = -1;
      try {
        String[] content;
        content = e.getLocalizedMessage().split("\n");
        System.err.println(cmd.substring(1) + ": "
                           + content[0]);
      } catch (Exception ex) {
        System.err.println(cmd.substring(1) + ": "
                           + ex.getLocalizedMessage());
      }
    } catch (Exception e) {
      exitCode = -1;
      System.err.println(cmd.substring(1) + ": "
                         + e.getLocalizedMessage());
    } 
    return exitCode;
  }

  /**
   * main() has some simple utility methods.
   * @param argv Command line parameters.
   * @exception Exception if the filesystem does not exist.
   */
  public static void main(String[] argv) throws Exception {
    int res = ToolRunner.run(new DFSAdmin(), argv);
    System.exit(res);
  }
}
