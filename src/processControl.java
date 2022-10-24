import java.io.*;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;
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


    /*1) 页内偏移的位数（例如：12 位）
      2) 虚拟地址的位数（例如：16 位）
      3) 物理地址的位数（例如：18 位）
      4) 进程的大小范围（例如：最小页数 min_size=3，最大页数 max_size=7）
      5) 生成一个合法虚拟地址（即不会导致处理机异常的虚拟地址）的概率 p_validAddr（例如：0.8）*/
    //页内偏移
    public static final int PAGE_OFFSET = 12;
    //虚拟地址
    public static final int VIRTUAL_ADDRESS = 16;
    //物理地址
    public static final int PHYSICAL_ADDRESS = 18;
    //进程的大小范围
    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 7;
    //生成一个合法虚拟地址的概率
    public static final double P_VALIDADDR = 0.8;

    public static final double P_AVAILABLEPG = 0.85;

    //虚拟地址
    public static String virtual;
    //物理地址
    public static String physical;


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

            //虚拟空间大小
            int virtualSpaceSize = (int) Math.pow(2, (VIRTUAL_ADDRESS - PAGE_OFFSET));
            //进程大小
            int size = (int) (Math.random() * (MAX_SIZE - MIN_SIZE + 1) + MIN_SIZE);

            pw.print(size);
            //在虚拟空间中随机生成size个不重复的页，并从小到大排序
            int[] pages = new int[size];
            for (int j = 0; j < size; j++) {
                pages[j] = (int) (Math.random() * virtualSpaceSize);
                for (int k1 = 0; k1 < j; k1++) {
                    if (pages[j] == pages[k1]) {
                        j--;
                        break;
                    }
                }
            }
            //冒泡排序
            for (int j = 0; j < size - 1; j++) {
                for (int k1 = 0; k1 < size - 1 - j; k1++) {
                    if (pages[k1] > pages[k1 + 1]) {
                        int temp = pages[k1];
                        pages[k1] = pages[k1 + 1];
                        pages[k1 + 1] = temp;
                    }
                }
            }
            //将pages数组存到文件中
            for (int j = 0; j < size; j++) {
                pw.print(" " + pages[j]);
            }
            /*for (int j = 0; j < size; j++) {
                int page = (int) (Math.random() * virtualSpaceSize);
                pages[j] = page;
                pw.print(" " + page);
            }*/
            pw.println();
            //将页写入文件
//            System.out.println("数组元素："+Arrays.toString(pages));

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
//                pw.println(instruction[j]);
                pw.print(instruction[j] + " ");

                /*指令类型后面添加上一个 16进制（或2进制）的随机的内存引用。该地址是一个有效的内存引用的概率为P_VALIDADDR*/
                if (Math.random() < P_VALIDADDR) {
                    //生成一个合法虚拟地址
                    int page = pages[(int) (Math.random() * size)];
                    int offset = (int) (Math.random() * (Math.pow(2, PAGE_OFFSET)));
                    int address = (int) ((int) page * (Math.pow(2, PAGE_OFFSET)) + offset);
                    //写入文件
                    // 写入到缓冲区
                    pw.println(Integer.toHexString(address));
                } else {
                    //生成一个非法虚拟地址
                    int address = (int) (Math.random() * Math.pow(2, VIRTUAL_ADDRESS));
                    //写入文件
                    // 写入到缓冲区
                    pw.println(Integer.toHexString(address));
                }

            }

            //初始化上下文

            // 关闭写入流,同时会把缓冲区内容写入文件,所以上面的注释掉
            pw.close();
            // 关闭输出流,释放系统资源
            os.close();
        }
//        随机生成位数组，生成 0 的概率为 P_AVAILABLEPG,输出位示图
        for (int i = 0; i < bitmap.length; i++) {
            if (Math.random() < P_AVAILABLEPG) {
                bitmap[i] = 0;
            } else {
                bitmap[i] = 1;
            }
        }
        //输出位示图
        System.out.print("位示图：");
        System.out.println(Arrays.toString(bitmap));
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
            int[] pgTable = {-1, 0, 0};
            pcb.setPgTable(pgTable);
            PCBS1.add(pcb);
            pcbs[i] = pcb;

        }
        //            1) 创建进程时：分配内存、建立页表 （模拟操作系统）


        System.out.print("time\t\t");
        for (int i = 0; i < k; i++) {
            System.out.print("PID:" + i + "\t\t\t");
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

        //输出位视图
        System.out.println("位示图：" + Arrays.toString(bitmap));

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
                System.out.print("READY\t\t\t");
            } else if (state == RUNCPU) {
                System.out.print("RUN:CPU"+virtual+" "+physical);
                cpuTime++;
                cs++;
            } else if (state == RUNIO) {
                System.out.print("RUN:IO "+virtual+" "+physical+"\t");
                cpuTime++;
                pcbs[j].setpState(WAITING);
                cs++;
            } else if (state == WAITING) {
                System.out.print("WAITING\t\t\t");
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
                System.out.print("DONE\t\t\t");
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
            //有效页
            String[] significantBit = br.readLine().split(" ");
            while ((line = br.readLine()) != null) {
                String[] lines = line.split(" ");
                //从程序计数器的位置开始执行
                if (i > pcb.getpPC()) {

//                    检查访问地址是否合法，如果不合法，则终止进程的执行，将其加入完成队列
                    //计算有效位
                    int value = new BigInteger(lines[1], 16).intValue();
                    int value2= (int) (value/Math.pow(2,PAGE_OFFSET));
                    //判断value是否存在significantBit中
                    boolean isExist = false;
                    for (int j = 1; j < significantBit.length; j++) {
                        if (value2 == Integer.parseInt(significantBit[j])) {
                            isExist = true;
                            break;
                        }
                    }
//                    1) 检查访问地址是否合法，如果不合法，则终止进程的执行，将其加入完成队列
//                    2) 否则输出地址转换：将 run:cpu/io 改成（run:cpu/io, 16 进制的虚拟地址，16 进制的物理地址）
                    if (!isExist) {
                        PCBS2.remove(0);
                        pcb.setpState(DONE);
                        PCBS4.add(pcb);
                        break;
                    }else{
                        //虚拟地址
                        virtual=lines[1];
                        //遍历位数组，找到对应的物理地址，将0置为1，返回位置下表，即为物理地址
                        for (int j = 0; j < bitmap.length; j++) {
                            if (bitmap[j]==0){
                                bitmap[j]=1;
                                pageTable[pcb.getPid()][0]=j;
                                if (value2==0){
                                    physical= Integer.toHexString(value%((int)Math.pow(2,PAGE_OFFSET))+j*(int)Math.pow(2,PAGE_OFFSET));
                                }else
                                    physical= Integer.toHexString(value%(value2*(int)Math.pow(2,PAGE_OFFSET))+j*(int)Math.pow(2,PAGE_OFFSET));
                                break;
                            }

                        }

                    }

                    //执行指令
                    if (lines[0].equals("io")) {
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
