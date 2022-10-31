import java.util.LinkedList;
import java.util.List;

/**
 * 进程控制块
 */
public class PCB {
    private int pid; // 进程标识符,进程ID
    private String pName; // 进程名
    private int pState; // 进程状态
    private int pPC; // 程序计数器，进程上下文

//    一个页表基址指针/进程 放在 PCB 中
    private List<Integer> PTBR = new LinkedList<>();

    private int[] pgTable;

    private int pAlloc;
    private int pMaxAlloc;

    /**
     * 命中次数
     */
    public static int hit;
    /**
     * 执行次数
     */
    public static int execute;

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        PCB.hit = hit;
    }

    public int getExecute() {
        return execute;
    }

    public void setExecute(int execute) {
        PCB.execute = execute;
    }

    public int getpAlloc() {
        return pAlloc;
    }

    public void setpAlloc(int pAlloc) {
        this.pAlloc = pAlloc;
    }

    public int getpMaxAlloc() {
        return pMaxAlloc;
    }

    public void setpMaxAlloc(int pMaxAlloc) {
        this.pMaxAlloc = pMaxAlloc;
    }

    public List<Integer> getPTBR() {
        return PTBR;
    }

    public void setPTBR(List<Integer> PTBR) {
        this.PTBR = PTBR;
    }

    public int[] getPgTable() {
        return pgTable;
    }

    public void setPgTable(int[] pgTable) {
        this.pgTable = pgTable;
    }

    private int pWaitTime; // 等待时间

    //虚拟地址对应的物理地址
    private int[] pVirtAddr = new int[16];

    public PCB() {
        //将虚拟地址初始化为-1
        for (int i = 0; i < pVirtAddr.length; i++) {
            pVirtAddr[i] = -1;
        }
    }

    public int[] getpVirtAddr() {
        return pVirtAddr;
    }

    public void setpVirtAddr(int[] pVirtAddr) {
        this.pVirtAddr = pVirtAddr;
    }

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
