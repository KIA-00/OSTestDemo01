/**
 * 进程控制块
 */
public class PCB {
    private int pid; // 进程标识符,进程ID
    private String pName; // 进程名
    private int pState; // 进程状态
    private int pPC; // 程序计数器，进程上下文

//    一个页表基址指针/进程 放在 PCB 中
    private int pageTableBase;

    private int[] pgTable;

    public int getPageTableBase() {
        return pageTableBase;
    }

    public void setPageTableBase(int pageTableBase) {
        this.pageTableBase = pageTableBase;
    }

    public int[] getPgTable() {
        return pgTable;
    }

    public void setPgTable(int[] pgTable) {
        this.pgTable = pgTable;
    }

    private int pWaitTime; // 等待时间

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public int getpState() {
        return pState;
    }

    public void setpState(int pState) {
        this.pState = pState;
    }

    public int getpPC() {
        return pPC;
    }

    public void setpPC(int pPC) {
        this.pPC = pPC;
    }

    public int getpWaitTime() {return pWaitTime;}
    public void setpWaitTime(int i) {
        this.pWaitTime = i;
    }
}
