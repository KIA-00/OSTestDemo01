import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class processControl {
    //进程状态
    public static final int READY = 0;//就绪
    public static final int RUNCPU = 1;//运行态RUN：CPU
    public static final int RUNIO = 2;//运行态RUN：IO
    public static final int WAITING = 3;//等待态
    public static final int DONE = 4;//完成态
    public static final Scanner SC = new Scanner(System.in);
    public static final int FINISH = 999;//结束标志

    //就绪队列
    public static List<PCB> PCBS1 = new LinkedList<>();
    //运行队列
    public static List<PCB> PCBS2 = new LinkedList<>();
    //等待队列
    public static List<PCB> PCBS3 = new LinkedList<>();
    //完成队列
    public static List<PCB> PCBS4 = new LinkedList<>();

    public static int time = 0;
    public static int cpuTime = 0;
    public static int ioTime = 0;


/*    一个全局的位示图 bitmap（位数组），用于物理页帧的分配和回收。第 i
    位为 0 表示第 i 个物理页帧空闲，为 1 则表示页帧被占用*/
    public static int[] bitmap = new int[32];

    /*一个页表/进程，用于支持每个进程的地址转换。页表项一定包括：物理
    页帧号、有效位、存在位、(访问权限*)。关于页表项的表示，可以用结
    构体，也可以用位数组*/
    public static int[][] pageTable = new int[32][4];

    public static void main(String[] args) throws Exception {
        //程序文件个数
        int k;
        //cpu指令所占百分比
        int percent;
        //指令个数范围
        int min, max;

        System.out.print("请输入程序文件个数：");
        k = SC.nextInt();
        System.out.print("请输入cpu指令所占百分比：");
        percent = SC.nextInt();
        System.out.print("请输入指令个数范围：");
        min = SC.nextInt();
        max = SC.nextInt();

        //生成k个程序文件
        CreateProcess(k, percent, min, max);

        Scheduler(k);
    }

    /**
     * 新进程的创建函数:分配 PCB，读取代码、初始化上下文，初始化 PCB
     *
     * @param k       程序文件个数
     * @param percent cpu指令所占百分比
     * @param min     指令个数范围
     * @param max     指令个数范围
     */
    public static void CreateProcess(int k, int percent, int min, int max) throws Exception {
        //生成k个程序文件
        for (int i = 0; i < k; i++) {
            /*生成程序文件 P1.txt、P2.txt、...、Pk.txt。
             每个程序文件 Pi.txt 的格式如
            下(每一行表示一条指令的类型)
            cpu
            cpu
            io
            cpu
            io
            cpu*/

            //如果文件存在则覆盖重写
            File file = new File("P" + i + ".txt");
            if (file.exists()) {
                file.delete();
            }
            OutputStream os = new FileOutputStream("./P" + i + ".txt");
            PrintWriter pw = new PrintWriter(os);


            //生成指令个数
            int n = (int) (Math.random() * (max - min + 1) + min);
            //生成指令
            String[] instruction = new String[n];
            for (int j = 0; j < n; j++) {
                //生成指令
                int m = (int) (Math.random() * 100);
                if (m < percent) {
                    //cpu指令
                    instruction[j] = "cpu";
                } else {
                    //io指令
                    instruction[j] = "io";
                }
                //写入文件
                //file.writ(instruction[j]);
                // 写入到缓冲区
                pw.println(instruction[j]);
            }

            //初始化上下文

            // 关闭写入流,同时会把缓冲区内容写入文件,所以上面的注释掉
            pw.close();
            // 关闭输出流,释放系统资源
            os.close();
        }
    }

    /**
     * 进程调度函数：从就绪队列选择一个进程运行
     *
     * @param k 程序文件个数
     */
    public static void Scheduler(int k) {
        /*状态的切换（）
        a) 从就绪态到运行态：被 Scheduler 选中执行，从就绪队列到运行队列
        b) 从运行态到完成态：最后一条指令被执行后，从运行队列到完成队列
        c) 从运行态到等待态：当前执行的指令是一个 io 指令，从运行队列到等待队列，设置好等待的计时器
        d) 从等待态到就绪态：io 指令执行所需要等待的时间片到，还有指令未执行完，从等待队列到就绪队列
        e) 从等待态到完成态：io 指令执行所需要等待的时间片到，没有指令需要执行，从等待队列到完成队列*/
        boolean wait = false;
        PCB[] pcbs = new PCB[k];
        for (int i = 0; i < k; i++) {
            //生成进程控制块
            PCB pcb = new PCB();
            //初始化 PCB
            pcb.setPid(i);
            pcb.setpName("P" + i);
            pcb.setpState(READY);
            pcb.setpPC(0);
            PCBS1.add(pcb);
            pcbs[i] = pcb;

        }
        //            1) 创建进程时：分配内存、建立页表 （模拟操作系统）


        System.out.print("time\t\t");
        for (int i = 0; i < k; i++) {
            System.out.print("PID:" + i + "\t\t");
        }
        System.out.println("CPU\t\tIOs");
        boolean unDone = true;
        while (unDone) {
            //就绪队列不为空
            if (!PCBS1.isEmpty()) {
                //从就绪队列选择一个进程运行
                PCB pcb = PCBS1.get(0);
                //从就绪队列到运行队列
                PCBS1.remove(0);
                PCBS2.add(pcb);
                //执行进程
                pcbs = ExecuteProcess(pcbs, k);
            } else if (!PCBS3.isEmpty()) {
                printTable(k, pcbs);
            }
            //判断是否所有进程都执行完毕
            if (PCBS4.size() == k) {
                unDone = false;
            }
        }
        printTable(k, pcbs);

        DecimalFormat df2 = new DecimalFormat("0.00");
        System.err.println("所有进程执行完毕");
        System.err.println("程序运行" + time + "个时间片");
        System.err.println("CPU执行了" + cpuTime + "个时间片,占比：" + df2.format((double) cpuTime / time));
        System.err.println("IO执行了" + ioTime + "个时间片,占比：" + df2.format((double) ioTime / time));
    }

    public static PCB[] printTable(int k, PCB[] pcbs) {
        time++;
        System.out.print("\t" + time + "\t\t");
        int cs = 0;
        int ios = 0;
        boolean ioTimeAdd = false;
        for (int j = 0; j < k; j++) {
            int state = pcbs[j].getpState();
            if (state == READY) {
                System.out.print("READY\t\t");
            } else if (state == RUNCPU) {
                System.out.print("RUN:CPU\t\t");
                cpuTime++;
                cs++;
            } else if (state == RUNIO) {
                System.out.print("RUN:IO\t\t");
                cpuTime++;
                pcbs[j].setpState(WAITING);
                cs++;
            } else if (state == WAITING) {
                System.out.print("WAITING\t\t");
                pcbs[j].setpWaitTime(pcbs[j].getpWaitTime() - 1);
                if (pcbs[j].getpWaitTime() == 0) {
                    PCBS3.remove(0);
                    if (pcbs[j].getpPC() == FINISH) {
                        //从等待队列到完成队列
                        PCBS4.add(pcbs[j]);
                        pcbs[j].setpState(DONE);
                    } else {
                        //从等待队列到就绪队列
                        pcbs[j].setpState(READY);
                        PCBS1.add(pcbs[j]);
                    }
                }
                if (!ioTimeAdd) ioTime++;
                ioTimeAdd = true;
                ios++;
            } else {
                System.out.print("DONE\t\t");
            }
        }
        System.out.println(cs + "\t\t" + ios);
        return pcbs;
    }

    public static PCB[] ExecuteProcess(PCB[] pcbs, int k) {
        PCB pcb = PCBS2.get(0);
        //读取文件
        String fileName = pcb.getpName() + ".txt";
        File file = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                //从程序计数器的位置开始执行
                if (i > pcb.getpPC()) {
                    //执行指令
                    if (line.equals("io")) {
                        //设置等待时间
                        pcb.setpWaitTime(4);
                        //设置状态
                        pcb.setpState(RUNIO);//RUN:IO
                        //设置程序计数器
                        pcb.setpPC(i);
                        //从运行队列到等待队列
                        PCBS2.remove(0);
                        PCBS3.add(pcb);
                        break;
                    } else {
                        pcb.setpState(RUNCPU);//RUN:CPU
                        //设置程序计数器
                        pcb.setpPC(i);
                        //设置进程控制块
                        pcbs[pcb.getPid()] = pcb;
                        printTable(k, pcbs);
                    }
                }
                i++;
            }
            if (line == null) {//文件读取完毕
                pcb.setpState(DONE);//DONE
                //设置程序计数器
                pcb.setpPC(FINISH);//结束
                //从运行队列到完成队列
                PCBS2.remove(0);
                PCBS4.add(pcb);
                //设置进程控制块
                pcbs[pcb.getPid()] = pcb;
            } else if (br.readLine() == null) {//IO指令结束,文件读取完毕
                pcb.setpPC(FINISH);//结束
                //设置进程控制块
                pcbs[pcb.getPid()] = pcb;
                printTable(k, pcbs);
            } else {
                pcbs[pcb.getPid()] = pcb;
                printTable(k, pcbs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pcbs;
    }
}
